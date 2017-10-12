package bgu.spl.app.store_objects;

import bgu.spl.app.ShoeStoreRunner;

/**
 * An object which represents information about a single type of shoe in the store.
 */
public class ShoeStorageInfo {
	private String shoeType;
	private int amount;
	private int discountedAmount;
	
	/**
	 * An object which represents information about a single type of shoe in the store.
	 * @param shoeType
	 * @param amountOnStorage
	 * @param discountedAmount
	 */
	public ShoeStorageInfo(String shoeType, int amountOnStorage) {
		this.shoeType = shoeType;
		this.amount = amountOnStorage;
		discountedAmount = 0;
	}
	
	public String getShoeType() {
		return shoeType;
	}
	public void setShoeType(String shoeType) {
		this.shoeType = shoeType;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public int getDiscountedAmount() {
		return discountedAmount;
	}
	public void setDiscountedAmount(int discountedAmount) {
		this.discountedAmount = discountedAmount;
	}
	public void print()
	{
		ShoeStoreRunner.logger.info("Name: " + shoeType + 
				", amount: " + amount + 
				", discounted amount: " + discountedAmount);
	}
}
