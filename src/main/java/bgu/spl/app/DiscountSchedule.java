package bgu.spl.app;

/**
 * Describes a schedule of a single discount that the manager will add to a specific shoe at a specific tick.
 */
public class DiscountSchedule {
	private String shoeType;
	private int tick;
	private int amount;
	
	/**
	 * Describes a schedule of a single discount that the manager will add to a specific shoe at a specific tick.
	 * @param shoeType
	 * @param tick
	 * @param amount
	 */
	public DiscountSchedule(String shoeType, int tick, int amount) {
		this.shoeType = shoeType;
		this.tick = tick;
		this.amount = amount;
	}

	public String getShoeType() {
		return shoeType;
	}

	public int getTick() {
		return tick;
	}

	public int getAmount() {
		return amount;
	}
}
