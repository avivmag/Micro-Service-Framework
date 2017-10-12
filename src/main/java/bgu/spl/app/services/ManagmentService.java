package bgu.spl.app.services;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

import bgu.spl.app.ShoeStoreRunner;
import bgu.spl.app.Store;
import bgu.spl.app.messages.broadcasts.NewDiscountBroadcast;
import bgu.spl.app.messages.broadcasts.TerminateBroadcast;
import bgu.spl.app.messages.broadcasts.TickBroadcast;
import bgu.spl.app.messages.requests.ManufacturingOrderRequest;
import bgu.spl.app.messages.requests.RestockRequest;
import bgu.spl.app.store_objects.DiscountSchedule;
import bgu.spl.mics.MicroService;

/**
 * Add discount to shoes in the store and send NewDiscountBroadcast to notify clients about them.
 */
public class ManagmentService extends MicroService {
	private List<DiscountSchedule> discountSchedule;
	private int currentTick;
	private CountDownLatch countDownLatch; 
	/**
	 * map the ordered shoe type to the available amount of shoes that can be  
	 */ 
	private Map<String, Integer> shoeTypesToNumberOfOrderedShoesMap;
	/**
	 * shoe types to Ordered restock queue.
	 */
	private Map<String, Queue<RestockRequest>> shoeTypesToRestockQueue;

	public ManagmentService(List<DiscountSchedule> discountedScheduleList) {
		super("manager");

		currentTick = 0;
		this.discountSchedule = discountedScheduleList;
		shoeTypesToNumberOfOrderedShoesMap = new HashMap<String, Integer>();
		shoeTypesToRestockQueue = new HashMap<String, Queue<RestockRequest>>();
	}

	public ManagmentService()
	{
		super("manager");
		currentTick = 0;
		shoeTypesToNumberOfOrderedShoesMap = new HashMap<String, Integer>();
		shoeTypesToRestockQueue = new HashMap<String, Queue<RestockRequest>>();
	}

	public void setCountDownLatch(CountDownLatch countDownLatch)
	{
		this.countDownLatch = countDownLatch;
	}

	@Override
	protected void initialize() {
		discountSchedule.sort((discountedSchedule1, discountedSchedule2) -> discountedSchedule1.getTick() - discountedSchedule2.getTick());

		subscribeBroadcast(TerminateBroadcast.class, terminateBroadcast -> 
		{
			ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": is terminating"); 
			terminate();
		});

		subscribeBroadcast(TickBroadcast.class, tickBroadcast -> 
		{
			currentTick = tickBroadcast.getCurrentTick();

			while(discountSchedule.size() != 0 && discountSchedule.get(0).getTick() == currentTick) 
			{
				Store.getInstance().addDiscount(discountSchedule.get(0).getShoeType(), discountSchedule.get(0).getAmount());
				
				ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": A discount on " + discountSchedule.get(0).getShoeType() + " is now on!");
				sendBroadcast(new NewDiscountBroadcast(discountSchedule.get(0).getShoeType()));
				discountSchedule.remove(0);
			}
		});

		subscribeRequest(RestockRequest.class, restockRequest -> 
		{
			ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": restock request received, shoe type is " + restockRequest.getShoeType());
			
			// create a record of the shoe type in the private hashmaps.
			if(!shoeTypesToRestockQueue.containsKey(restockRequest.getShoeType()))
			{
				shoeTypesToRestockQueue.put(restockRequest.getShoeType(), new LinkedList<RestockRequest>());
				shoeTypesToNumberOfOrderedShoesMap.put(restockRequest.getShoeType(), 0);
			}

			// adds the restockRequest to the queue
			shoeTypesToRestockQueue.get(restockRequest.getShoeType()).add(restockRequest);
			
			// if we did not ordered enough shoes
			if(shoeTypesToNumberOfOrderedShoesMap.get(restockRequest.getShoeType()) < shoeTypesToRestockQueue.get(restockRequest.getShoeType()).size())
			{
				// renew the amount of shoes we ordered.
				shoeTypesToNumberOfOrderedShoesMap.put(restockRequest.getShoeType(),  shoeTypesToNumberOfOrderedShoesMap.get( restockRequest.getShoeType()) + currentTick % 5 + 1);
				ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": asking for factory restock, shoe type: " + restockRequest.getShoeType());
				
				if(!sendRequest(new ManufacturingOrderRequest(restockRequest.getShoeType(), currentTick % 5 + 1, currentTick), receipt -> 
				{
					ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": " + receipt.getSeller() + " order restock accepted, shoe type - " + receipt.getShoeType() + ", amount - " + receipt.getAmountSold());
					// number of restock requests we are going to complete now.
					int requestsNumTaken = Math.min(receipt.getAmountSold(), shoeTypesToRestockQueue.get(restockRequest.getShoeType()).size());

					// gives the store the remaining shoes.
					if(requestsNumTaken < receipt.getAmountSold())
					{
						Store.getInstance().add(restockRequest.getShoeType(), receipt.getAmountSold() - requestsNumTaken);
						ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": " + (receipt.getAmountSold() - requestsNumTaken) + " shoes of type " + restockRequest.getShoeType() + " has been added to the store");
					}
					
					// files the receipt.
					Store.getInstance().file(receipt);

					// 'sells' the reserved shoes back to the sellers
					for (int i = requestsNumTaken; i > 0; i--) {
						complete(shoeTypesToRestockQueue.get(restockRequest.getShoeType()).remove(), true);
					}					
					
					// reduce the number of shoes that has been requested by the seller.
					shoeTypesToNumberOfOrderedShoesMap.put(restockRequest.getShoeType(), shoeTypesToNumberOfOrderedShoesMap.get(restockRequest.getShoeType()) - requestsNumTaken);
				}))
				{ // if there is no factory to handle the ManufacturingOrderRequest.
					ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": there is no factories out there to place orders");
					while (shoeTypesToRestockQueue.get(restockRequest.getShoeType()).size() != 0)
						complete(shoeTypesToRestockQueue.get(restockRequest.getShoeType()).remove(), false);
				}

				// removes the queue from the map, and removes the shoe type from the shoeTypesToNumberOfOrderedShoesMap.
				if(shoeTypesToRestockQueue.get(restockRequest.getShoeType()).size() == 0)
				{
					shoeTypesToRestockQueue.remove(restockRequest.getShoeType());
					shoeTypesToNumberOfOrderedShoesMap.remove(restockRequest.getShoeType());
				}
			}
			else
				ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": waiting for an older manufacturing order request to complete for " + restockRequest.getShoeType());
		});
		ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": is ready to work");
		countDownLatch.countDown();
	}
}
