package bgu.spl.app;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Holds a collections of ShoeStorageInfo and a list if receipts issued by the store.
 */
public class Store {
	private List<Receipt> recieptList;
	private Map<String, ShoeStorageInfo> typeToShoeMap;
	private Object lockForNewShoes;

	private static class StoreHolder
	{
		private static Store instance = new Store();
	}

	/**
	 * Holds a collections of ShoeStorageInfo and a list if receipts issued by the store.
	 * @return An instance of the store.
	 */
	public static Store getInstance()
	{
		return StoreHolder.instance;
	}

	private Store()
	{
		recieptList = new CopyOnWriteArrayList<Receipt>();
		typeToShoeMap = new ConcurrentHashMap<String, ShoeStorageInfo>();
		lockForNewShoes = new Object();
	}

	/**
	 * load ShoeStorageInfo on initialize.
	 * @param storage
	 */
	public void load(ShoeStorageInfo[] storage)
	{
		for (ShoeStorageInfo shoeStorageInfo : storage) {
			typeToShoeMap.put(shoeStorageInfo.getShoeType(), shoeStorageInfo);
			ShoeStoreRunner.logger.info("Tick 0: " + shoeStorageInfo.getAmount() + " " + shoeStorageInfo.getShoeType() + " has been added to the store");
		}
	}

	public enum BuyResult { NOT_IN_STOCK, NOT_ON_DISCOUNT, REGULAR_PRICE, DISCOUNTED_PRICE }
	/**
	 * Try to take one pair of shoes from the store
	 * @param shoeType 
	 * @param onlyDiscount If the customer want only discounted pair of shoes.
	 * @return
	 */
	public BuyResult take(String shoeType, boolean onlyDiscount)
	{
		checkAndCreateIfNeedShoeStorageInfo(shoeType);
		
		synchronized (typeToShoeMap.get(shoeType)) {
			if(onlyDiscount && typeToShoeMap.get(shoeType).getDiscountedAmount() == 0)
				return BuyResult.NOT_ON_DISCOUNT;
			if(typeToShoeMap.get(shoeType).getAmount() == 0)
				return BuyResult.NOT_IN_STOCK;
			if(typeToShoeMap.get(shoeType).getDiscountedAmount() > 0)
			{
				typeToShoeMap.get(shoeType).setDiscountedAmount(typeToShoeMap.get(shoeType).getDiscountedAmount() - 1);
				typeToShoeMap.get(shoeType).setAmount(typeToShoeMap.get(shoeType).getAmount() - 1);
				return BuyResult.DISCOUNTED_PRICE;
			}
			if(!onlyDiscount && typeToShoeMap.get(shoeType).getAmount() > 0)
			{
				typeToShoeMap.get(shoeType).setAmount(typeToShoeMap.get(shoeType).getAmount() - 1);
				return BuyResult.REGULAR_PRICE;
			}

			return null;	
		}
	}

	/**
	 * Add a amount of shoes of shoeType to the store.
	 * @param shoeType
	 * @param amount
	 */
	public void add(String shoeType, int amount)
	{
		checkAndCreateIfNeedShoeStorageInfo(shoeType);

		synchronized (typeToShoeMap.get(shoeType)) {
			typeToShoeMap.get(shoeType).setAmount(typeToShoeMap.get(shoeType).getAmount() + amount);
		}
	}

	/**
	 * Set some amount of the shoeTypes to discount
	 * @param shoeType
	 * @param amount
	 */
	public void addDiscount(String shoeType, int amount)
	{
		checkAndCreateIfNeedShoeStorageInfo(shoeType);

		synchronized (typeToShoeMap.get(shoeType)) {
			typeToShoeMap.get(shoeType).setDiscountedAmount(Math.min(typeToShoeMap.get(shoeType).getDiscountedAmount() + amount, typeToShoeMap.get(shoeType).getAmount()));
		}
	}
	
	/**
	 * if the shoe has never been existed before, we should add a storage type info of that kind.
	 * @param shoeType
	 */
	private void checkAndCreateIfNeedShoeStorageInfo(String shoeType)
	{
		if(!typeToShoeMap.containsKey(shoeType))
			synchronized (lockForNewShoes) {
				if(!typeToShoeMap.containsKey(shoeType))
					typeToShoeMap.put(shoeType, new ShoeStorageInfo(shoeType, 0));
			}
	}

	/**
	 * File the receipt for further examine.
	 * @param receipt
	 */
	public void file(Receipt receipt)
	{
		recieptList.add(receipt);
	}

	/**
	 * Print all the shoes and receipts currently in store 
	 */
	public void print()
	{
		ShoeStoreRunner.logger.info("Shoes in store: ");
		for (ShoeStorageInfo item : typeToShoeMap.values()) {
			item.print();
		}
		ShoeStoreRunner.logger.info("\nReciepts taken: ");
		for (Receipt receipt : recieptList) {
			receipt.print();
		}		
	}
}
