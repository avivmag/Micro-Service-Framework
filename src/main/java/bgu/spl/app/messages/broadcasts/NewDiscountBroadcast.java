package bgu.spl.app.messages.broadcasts;

import bgu.spl.mics.Broadcast;

/**
 * A broadcast message that is sent when the manager of the store decides to have a sell on a specific shoe.
 */
public class NewDiscountBroadcast implements Broadcast {
	private String shoeType;

	/**
	 * A broadcast message that is sent when the manager of the store decides to have a sell on a specific shoe.
	 * @param shoeType
	 */
	public NewDiscountBroadcast(String shoeType) {
		this.shoeType = shoeType;
	}

	public String getShoeType() {
		return shoeType;
	}
}
