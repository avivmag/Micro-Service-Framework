package bgu.spl.app;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import bgu.spl.mics.MicroService;

/**
 * Describes one client connected to the website.
 */
public class WebsiteClientServer extends MicroService {

	private List<PurchaseSchedule> purchaseSchedule;
	private Set<String> wishList;
	private int currentTick;
	private CountDownLatch countDownLatch;
	private int numberOfPendingRequests;

	/**
	 * Describes one client connected to the website.
	 */
	public WebsiteClientServer()
	{
		super("");
		currentTick = 0; 
		numberOfPendingRequests = 0;
	}

	public void setCountDownLatch(CountDownLatch countDownLatch)
	{
		this.countDownLatch = countDownLatch;
	}

	@Override
	protected void initialize() {
		purchaseSchedule.sort((purchaseSchedule1, purchaseSchedule2) -> purchaseSchedule1.getTick() - purchaseSchedule2.getTick());

		subscribeBroadcast(TerminateBroadcast.class, terminateBroadcast -> 
		{
			ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": is terminating"); 
			terminate();
		});

		subscribeBroadcast(TickBroadcast.class, tickBroadcast -> 
		{
			currentTick = tickBroadcast.getCurrentTick();

			while(purchaseSchedule.size() != 0 && purchaseSchedule.get(0).getTick() == currentTick) 
			{
				// trying to purchase some shoes
				ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": asking to purchase " + purchaseSchedule.get(0).getShoeType());
				
				numberOfPendingRequests++;
				// pass a request to buy shoes to sellers pool.
				if(!sendRequest(new PurchaseOrderRequest(getName(), purchaseSchedule.get(0).getShoeType(), false, currentTick), receipt -> {
					if(receipt != null)
					{
						// everything worked as planned
						if(receipt.isDiscount() && wishList.contains(receipt.getShoeType()))
							wishList.remove(receipt.getShoeType());
						ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": received the receipt");
					}
					else
					{
						// the seller told me there are  no shoes of my wanted type
						ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": the seller told me there are no shoes of my wanted type");
					}
					numberOfPendingRequests--;
				}))
				{
					// there is no seller to listen
					numberOfPendingRequests--;
					ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": purchase failed, there is no seller to listen to my needs");
				}
				purchaseSchedule.remove(0);
			}

			// if there is no action for the client.
			if(numberOfPendingRequests == 0 && purchaseSchedule.size() == 0 && wishList.size() == 0)
			{
				ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": I did all I wanted to do, there is no reason for me to live.. terminating.."); 
				terminate();
			}
		});

		subscribeBroadcast(NewDiscountBroadcast.class, newDiscountBroadcast -> 
		{
			// if the discounted shoe is in our wish list.
			if (wishList.contains(newDiscountBroadcast.getShoeType()))
			{
				ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": I received the discount call for " + newDiscountBroadcast.getShoeType() + " I wanted");
				numberOfPendingRequests++;
				if(!sendRequest(new PurchaseOrderRequest(getName(), newDiscountBroadcast.getShoeType(), true, currentTick), receipt -> {
					if(receipt != null)
					{
						// everything worked as planned
						if(wishList.contains(newDiscountBroadcast.getShoeType()))
							wishList.remove(newDiscountBroadcast.getShoeType());
						ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": received the receipt");						
					}
					else
						ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": the store don't have my wanted shoe type");
					numberOfPendingRequests--;
				}))
				{
					// there is no seller to listen
					numberOfPendingRequests--;
					ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": the seller told me there are no shoes of my wanted type");
				}

				// if there is no action for the client.
				if(numberOfPendingRequests == 0 && purchaseSchedule.size() == 0 && wishList.size() == 0)
				{
					ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": I did all I wanted to do, there is no reason for me to live.. terminating.."); 
					terminate();
				}
			}
		});
		
		ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": is ready to work");
		countDownLatch.countDown();		
	}
}