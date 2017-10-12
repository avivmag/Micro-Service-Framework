package bgu.spl.app;

/**
 * Describes a schedule of a single client - purchase at a specific tick.
 */
public class PurchaseSchedule {
	private String shoeType;
	private int tick;
	
	/**
	 * Describes a schedule of a single client - purchase at a specific tick.
	 * @param shoeType
	 * @param tick
	 */
	public PurchaseSchedule(String shoeType, int tick) {
		this.shoeType = shoeType;
		this.tick = tick;
	}

	public String getShoeType() {
		return shoeType;
	}

	public int getTick() {
		return tick;
	}
}
