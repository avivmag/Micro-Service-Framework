package bgu.spl.app.store_objects;

import bgu.spl.app.ShoeStoreRunner;

/**
 * An object representing a receipt that should be sent to a client after buying a shoe.
 */
public class Receipt {
	private String seller;
	private String customer;
	private String shoeType;
	private boolean discount;
	private int issuedTick;
	private int requestTick;
	private int amountSold;
	
	/**
	 * An object representing a receipt that should be sent to a client after buying a shoe.
	 * @param seller
	 * @param customer
	 * @param shoeType
	 * @param discount
	 * @param issuedTick
	 * @param requestTick
	 * @param amountSold
	 */
	public Receipt(String seller, String customer, String shoeType, boolean discount, int issuedTick, int requestTick,
			int amountSold) {
		this.seller = seller;
		this.customer = customer;
		this.shoeType = shoeType;
		this.discount = discount;
		this.issuedTick = issuedTick;
		this.requestTick = requestTick;
		this.amountSold = amountSold;
	}

	public String getSeller() {
		return seller;
	}

	public String getCustomer() {
		return customer;
	}

	public String getShoeType() {
		return shoeType;
	}

	public boolean isDiscount() {
		return discount;
	}

	public int getIssuedTick() {
		return issuedTick;
	}

	public int getRequestTick() {
		return requestTick;
	}

	public int getAmountSold() {
		return amountSold;
	}
	public void print()
	{
		ShoeStoreRunner.logger.info(
				"Receipt: Seller: " + seller + 
				", customer: " + customer + 
				", Shoes: " + shoeType + 
				(discount ? " - on discount" : "") +  
				", Amount sold: " + amountSold + 
				", Customer asked at tick: " + requestTick + 
				", Request issued at tick: " + issuedTick);
	}
}
