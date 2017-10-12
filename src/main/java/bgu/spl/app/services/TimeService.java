package bgu.spl.app.services;

import java.util.Timer;
import java.util.TimerTask;

import bgu.spl.app.ShoeStoreRunner;
import bgu.spl.app.messages.broadcasts.TerminateBroadcast;
import bgu.spl.app.messages.broadcasts.TickBroadcast;
import bgu.spl.mics.MicroService;

/** 
 * Responsible for counting how much clock ticks passed since the beginning of its execution and notifying every other micro-service about it.
 */
public class TimeService extends MicroService {
	private int speed;
	private int duration;

	/**
	 * Responsible for counting how much clock ticks passed since the beginning of its execution and notifying every other micro-service about it.
	 * @param speed
	 * @param duration
	 */
	public TimeService(int speed, int duration) {
		super("timer");
		this.speed = speed;
		this.duration = duration;
	}

	public TimeService()
	{
		super("timer");
	}

	@Override
	protected void initialize() {
		Timer timer = new Timer();
		
		subscribeBroadcast(TerminateBroadcast.class, terminateBroadcast -> 
		{
			ShoeStoreRunner.logger.info("reached duration, " + getName() + ": is terminating"); 
			timer.cancel();
			terminate();
		});
		
		ShoeStoreRunner.logger.info(getName() + ": start ticking");
		
		// schedules a timer for fixed amount of time duration.
		TickSchedule tickSchedule = new TickSchedule(duration);
		timer.schedule(tickSchedule, 0, speed);
	}

	/**
	 * A timer task which responsible for ticking every duration miliseconds.
	 */
	private class TickSchedule extends TimerTask
	{
		private int currentTick;
		private int duration;
		public TickSchedule(int duration) {
			this.duration = duration;
			currentTick = 0;
		}

		@Override
		public void run() {
			currentTick++;
			if(currentTick <= duration)
			{
				ShoeStoreRunner.logger.info("tick " + currentTick);
				sendBroadcast(new TickBroadcast(currentTick));
			}
			else if(currentTick == duration + 1)
			{
				sendBroadcast(new TerminateBroadcast());
				cancel();
			}
		}
	}
}
