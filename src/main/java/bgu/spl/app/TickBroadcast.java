package bgu.spl.app;

import bgu.spl.mics.Broadcast;

/**
 * A broadcast message that is sent at every passed clock tick.
 */
public class TickBroadcast implements Broadcast{
	private int currentTick;

	/**
	 * A broadcast message that is sent at every passed clock tick.
	 * @param currentTick
	 */
	public TickBroadcast(int currentTick) {
		this.currentTick = currentTick;
	}

	public int getCurrentTick() {
		return currentTick;
	}
}
