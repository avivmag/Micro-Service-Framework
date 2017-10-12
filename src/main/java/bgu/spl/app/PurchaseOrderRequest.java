package bgu.spl.app;

import bgu.spl.mics.Request;

/**
 * A request that is sent when a store client wish to buy a shoe.
 */
public class PurchaseOrderRequest implements Request<Receipt> {
	private String shoeType;
	private boolean discount;
	private String requesterName;
	private int requestedTick;
	
	/**
	 * A request that is sent when a store client wish to buy a shoe.
	 * @param requesterName
	 * @param shoeType
	 * @param discount
	 * @param requestedTick
	 */
	public PurchaseOrderRequest(String requesterName, String shoeType, boolean discount, int requestedTick) {
		this.requesterName = requesterName;
		this.shoeType = shoeType;
		this.discount = discount;
		this.requestedTick = requestedTick;
	}

	public String getShoeType() {
		return shoeType;
	}

	public boolean isDiscount() {
		return discount;
	}

	public String getRequesterName() {
		return requesterName;
	}

	public int getRequestedTick() {
		return requestedTick;
	}
}
