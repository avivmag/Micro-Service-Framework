package bgu.spl.app.services;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import bgu.spl.app.ShoeStoreRunner;
import bgu.spl.app.messages.broadcasts.TerminateBroadcast;
import bgu.spl.app.messages.broadcasts.TickBroadcast;
import bgu.spl.app.messages.requests.ManufacturingOrderRequest;
import bgu.spl.app.store_objects.Receipt;
import bgu.spl.mics.MicroService;
/**
 * This micro-service describes a shoe factory that manufacture shoes for the store.
 */
public class ShoeFactoryService extends MicroService {
	private int currentTick;
	private Queue<ManufacturingOrderRequest> manufacturingOrderRequestQueue;
	private ManufacturingOrderRequest currentManufacturingOrderRequest;
	private int remainingNumOfShoesToProduce;
	private CountDownLatch countDownLatch;

	/**
	 * This micro-service describes a shoe factory that manufacture shoes for the store.
	 * @param name
	 */
	public ShoeFactoryService(String name) {
		super(name);
		currentTick = 0; 
		currentManufacturingOrderRequest = null;
		manufacturingOrderRequestQueue = new LinkedList<ManufacturingOrderRequest>();
	}

	public void setCountDownLatch(CountDownLatch countDownLatch)
	{
		this.countDownLatch = countDownLatch;
	}

	@Override
	protected void initialize() {
		remainingNumOfShoesToProduce = -1;

		subscribeBroadcast(TerminateBroadcast.class, terminateBroadcast -> 
		{
			ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": is terminating"); 
			terminate();
		});

		subscribeBroadcast(TickBroadcast.class, tickBroadcast -> 
		{
			currentTick = tickBroadcast.getCurrentTick();

			// if there is no shoe type to work on
			if (currentManufacturingOrderRequest == null){
				// if there is a request to create some type of shoes 
				if (manufacturingOrderRequestQueue.size() != 0){
					currentManufacturingOrderRequest = manufacturingOrderRequestQueue.remove();
					remainingNumOfShoesToProduce = currentManufacturingOrderRequest.getAmount();
				}
			}

			// if we still need to create the shoe
			if (remainingNumOfShoesToProduce > 0)
			{
				remainingNumOfShoesToProduce--;
				ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": 1 " + currentManufacturingOrderRequest.getShoeType() + " has been created");
			}
			// the last shoe of the requested kind has been completed yesterday 
			else if (remainingNumOfShoesToProduce == 0)
			{
				ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": We have completed the request for making " + currentManufacturingOrderRequest.getAmount() + " " + currentManufacturingOrderRequest.getShoeType() + " kind of shoes, sending receipt to store");
				Receipt receipt = new Receipt(getName(), "store", currentManufacturingOrderRequest.getShoeType(), false, currentTick, currentManufacturingOrderRequest.getTick(), currentManufacturingOrderRequest.getAmount());
				complete(currentManufacturingOrderRequest, receipt);

				// if there are more request to create more shoes
				if (manufacturingOrderRequestQueue.size() != 0){
					currentManufacturingOrderRequest = manufacturingOrderRequestQueue.remove();
					remainingNumOfShoesToProduce = currentManufacturingOrderRequest.getAmount();
					ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": order request for " + remainingNumOfShoesToProduce + " " + currentManufacturingOrderRequest.getShoeType() + " is starting");
				}
				// there are no more request to make any shoes.. for now..
				else 
				{
					currentManufacturingOrderRequest = null;
					remainingNumOfShoesToProduce = -1;
				}

			}

		});

		subscribeRequest(ManufacturingOrderRequest.class, manufacturingOrderRequest -> {
			manufacturingOrderRequestQueue.add(manufacturingOrderRequest);
			
			ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": order request for " + manufacturingOrderRequest.getAmount() + " " + manufacturingOrderRequest.getShoeType() + " has been received.");
		});

		ShoeStoreRunner.logger.info("Tick " + currentTick + ", " + getName() + ": is ready to work");
		countDownLatch.countDown();
	}
}
