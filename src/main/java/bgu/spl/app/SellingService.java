package bgu.spl.app;

import java.util.concurrent.CountDownLatch;

import bgu.spl.mics.MicroService;

/**
 * Handles purchase order request.
 */
public class SellingService extends MicroService {
	private int currentTick;
	private CountDownLatch countDownLatch;
	/**
	 * Handles purchase order request.
	 * @param name
	 */
	public SellingService(String name) {
		super(name);
		currentTick = 0;
	}

	public void setCountDownLatch(CountDownLatch countDownLatch)
	{
		this.countDownLatch = countDownLatch;
	}

	@Override
	protected void initialize() {

		subscribeBroadcast(TickBroadcast.class, tickBroadcast -> currentTick = tickBroadcast.getCurrentTick());

		subscribeBroadcast(TerminateBroadcast.class, terminateBroadcast -> 
		{
			ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": is terminating"); 
			terminate();
		});



		subscribeRequest(PurchaseOrderRequest.class, purchaseOrderRequest -> {
			// try to take shoes from the store
			Store.BuyResult buyResult = Store.getInstance().take(purchaseOrderRequest.getShoeType(), purchaseOrderRequest.isDiscount());
			ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": customer " + purchaseOrderRequest.getRequesterName() + " asked for " + purchaseOrderRequest.getShoeType());
			switch (buyResult) {
			case REGULAR_PRICE:
				// the order has been successful
				ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": the sell of " + purchaseOrderRequest.getShoeType() + " was successful, giving the receipt to " + purchaseOrderRequest.getRequesterName());
				Receipt receipt = new Receipt(
						getName(), 
						purchaseOrderRequest.getRequesterName(), 
						purchaseOrderRequest.getShoeType(), 
						purchaseOrderRequest.isDiscount(), 
						currentTick, 
						purchaseOrderRequest.getRequestedTick(), 
						1);
				complete(purchaseOrderRequest, receipt);
				Store.getInstance().file(receipt);
				break;
			case DISCOUNTED_PRICE:
				// the order has been successful
				ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": the sell of " + purchaseOrderRequest.getShoeType() + " was successful, giving the receipt to " + purchaseOrderRequest.getRequesterName());
				Receipt discountedReceipt = new Receipt(
						getName(), 
						purchaseOrderRequest.getRequesterName(), 
						purchaseOrderRequest.getShoeType(), 
						true, 
						currentTick, 
						purchaseOrderRequest.getRequestedTick(), 
						1);
				complete(purchaseOrderRequest, discountedReceipt);
				Store.getInstance().file(discountedReceipt);
				break;
			case NOT_IN_STOCK:
				// there is no shoes of that kind, need to tell the manager
				ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": the shoes " + purchaseOrderRequest.getShoeType() + " are not in stock, sending request to manager");
				RestockRequest r = new RestockRequest(purchaseOrderRequest.getShoeType());
				if(!sendRequest(r, restockRequestCompleted -> {
					if(restockRequestCompleted){
						// restock was successful, and the manager reserved the shoes for my customer.
						ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": the sell of " + purchaseOrderRequest.getShoeType() + " was successful, giving the receipt to " + purchaseOrderRequest.getRequesterName());
						Receipt receiptNotInStock = new Receipt(
								getName(), 
								purchaseOrderRequest.getRequesterName(), 
								purchaseOrderRequest.getShoeType(), 
								purchaseOrderRequest.isDiscount(), 
								currentTick, 
								purchaseOrderRequest.getRequestedTick(), 
								1);
						complete(purchaseOrderRequest, receiptNotInStock);
						Store.getInstance().file(receiptNotInStock);
					}
					// cannot restock the chosen shoes
					else
					{
						ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": there is no restock available for " + purchaseOrderRequest.getShoeType());
						complete(purchaseOrderRequest, null);
					}

				}))
				{
					ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": there is no manager to talk to");
					complete(purchaseOrderRequest, null);
				}

				break;
			case NOT_ON_DISCOUNT:
				ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": the chosen shoes " + purchaseOrderRequest.getShoeType() + " are not on discount");
				complete(purchaseOrderRequest, null);
				break;
			default:
				break;
			}

		});
		ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": is ready to work");
		countDownLatch.countDown();
	}
}