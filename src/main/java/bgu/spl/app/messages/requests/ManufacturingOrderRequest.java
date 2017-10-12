package bgu.spl.app.messages.requests;

import bgu.spl.app.store_objects.Receipt;
import bgu.spl.mics.Request;

/**
 * A request that is sent when the store manager want that a shoe factory will manufacture a shoe for the store.
 */
public class ManufacturingOrderRequest implements Request<Receipt> {
	private String shoeType;
	private int amount;
	private int tick;

	/**
	 * A request that is sent when the store manager want that a shoe factory will manufacture a shoe for the store.
	 * @param shoeType
	 * @param amount
	 * @param tick
	 */
	public ManufacturingOrderRequest(String shoeType, int amount, int tick) {
		this.shoeType = shoeType;
		this.amount = amount;
		this.tick = tick;
	}

	public String getShoeType() {
		return shoeType;
	}

	public int getAmount() {
		return amount;
	}
	
	public int getTick() {
		return tick;
	}
}
