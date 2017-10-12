package bgu.spl.mics.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import bgu.spl.app.TerminateBroadcast;
import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.RequestCompleted;
import bgu.spl.mics.RoundRobinADT;

public class MessageBusImpl implements MessageBus {
	/**
	 * micro service individual inbox
	 */
	private Map<MicroService, Queue<Message>> messageMap;
	/** 
	 * address to return to (who asked the request)
	 */
	private Map<Request<?>, MicroService> requestAndRequester;
	/**
	 * list of micro services who can handle request of given kind.
	 */
	private Map<Class<? extends Request>, RoundRobinADT<MicroService>> requestSubscribeMap;
	/**
	 * list of micro services who can handle broadcast of given kind.
	 */
	private Map<Class<? extends Broadcast>, CopyOnWriteArrayList<MicroService>> broadcastSubscribeMap;
	/**
	 * used for unregistering the micro services by knowing which Requests types it assigned to. 
	 */
	private Map<MicroService, List<Class<? extends Request>>> microServicesRequests;
	/**
	 * used for unregistering the micro services by knowing which Broadcasts types it assigned to.
	 */
	private Map<MicroService, List<Class<? extends Broadcast>>> microServicesBroadcast;

	private static class MessageBusHolder{
		private static MessageBus instance = new MessageBusImpl();
	}

	public static MessageBus MessageBus(){
		return MessageBusHolder.instance;
	}

	private MessageBusImpl() 
	{
		messageMap = new ConcurrentHashMap<MicroService, Queue<Message>>();
		requestAndRequester = new ConcurrentHashMap <Request<?>, MicroService>();

		requestSubscribeMap = new ConcurrentHashMap<Class<? extends Request>, RoundRobinADT<MicroService>>();
		broadcastSubscribeMap = new ConcurrentHashMap<Class<? extends Broadcast>, CopyOnWriteArrayList<MicroService>>();

		microServicesRequests = new ConcurrentHashMap<MicroService, List<Class<? extends Request>>>();
		microServicesBroadcast = new ConcurrentHashMap<MicroService, List<Class<? extends Broadcast>>>();
	}

	@Override
	public void subscribeRequest(Class<? extends Request> type, MicroService m) {
		// insert the micro-service to the request round robin queue.
		synchronized (requestSubscribeMap) {
			requestSubscribeMap.putIfAbsent(type, new RoundRobinADT<MicroService>());
			requestSubscribeMap.get(type).add(m);
		}
		// saves what request the micro-service can handle (no need to synchronize microServicesRequests because the only process who use it is the one it belongs to).  
		microServicesRequests.putIfAbsent(m, new CopyOnWriteArrayList<Class<? extends Request>>());
		microServicesRequests.get(m).add(type);
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		// insert the micro-service to the broadcast round robin queue.
		synchronized (broadcastSubscribeMap) {
			broadcastSubscribeMap.putIfAbsent(type, new CopyOnWriteArrayList<MicroService>());
			broadcastSubscribeMap.get(type).add(m);
		}
		// saves what broadcast the micro-service can handle (no need to synchronize microServicesBroadcast because the only process who use it is the one it belongs to)
		microServicesBroadcast.putIfAbsent(m, new CopyOnWriteArrayList<Class<? extends Broadcast>>());
		microServicesBroadcast.get(m).add(type);
	}

	@Override
	public <T> void complete(Request<T> r, T result) {
		// lock the micro service so things will not change while using the connected adt's.
		synchronized (requestAndRequester.get(r)) { 
			if(messageMap.containsKey(requestAndRequester.get(r)))
			{
				messageMap.get(requestAndRequester.get(r)).add(new RequestCompleted<T>(r, result));
				requestAndRequester.get(r).notifyAll();
				requestAndRequester.remove(r);
			}
		}
	}

	@Override
	public boolean sendRequest(Request<?> r, MicroService requester) {
		// the micro-service we are going to send the request to.
		MicroService ms;

		// if there is someone who can handle the request
		synchronized (requestSubscribeMap) {
			if(!requestSubscribeMap.containsKey(r.getClass()))
				return false;
			synchronized (requestSubscribeMap.get(r.getClass())) {
				// get the service so we could return it to the queue in round robin fashion and
				// add the request to the inbox of the service.
				ms = requestSubscribeMap.get(r.getClass()).getNext();
				synchronized (ms) {
					// to know where to return to when calling the complete method
					requestAndRequester.put(r, requester);

					messageMap.get(ms).add(r);
					// wakes up the service which has been waited to a new mail.
					ms.notifyAll();
				}
			}
		}

		return true;
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		// synchronized this so no one would change the map while we are working on it.
		synchronized (broadcastSubscribeMap) {
			if(broadcastSubscribeMap.containsKey(b.getClass()))

				// synchronized this so no one would change this list while we are working on it.
				synchronized (broadcastSubscribeMap.get(b.getClass())) {

					// sends the broadcast to all the listeners.
					for (int i = 0; i < broadcastSubscribeMap.get(b.getClass()).size(); i++) {

						synchronized (broadcastSubscribeMap.get(b.getClass()).get(i)) { // lock for messageMap
							if(messageMap.containsKey(broadcastSubscribeMap.get(b.getClass()).get(i)))
							{
								messageMap.get(broadcastSubscribeMap.get(b.getClass()).get(i)).add(b);
								broadcastSubscribeMap.get(b.getClass()).get(i).notifyAll();
							}
						}
					}
				}
		}	
	}

	@Override
	public void register(MicroService m) {
		// adds a 'message box' for the micro-service.
		messageMap.put(m, new LinkedBlockingQueue<Message>());	
	}

	@Override
	public void unregister(MicroService m) {
		// removes all the requests the micro service is subscribed to.
		if(microServicesRequests.containsKey(m))
			for (int i = 0; i < microServicesRequests.get(m).size(); i++) {
				synchronized (requestSubscribeMap) {
					synchronized (requestSubscribeMap.get(microServicesRequests.get(m).get(i))) {
						requestSubscribeMap.get(microServicesRequests.get(m).get(i)).remove(m);
						if(requestSubscribeMap.get(microServicesRequests.get(m).get(i)).isEmpty())
							requestSubscribeMap.remove(microServicesRequests.get(m).get(i));
					}
				}
			}

		// removes all broadcast the micro service is subscribed to.
		if(microServicesBroadcast.containsKey(m))
			for (int i = 0; i < microServicesBroadcast.get(m).size(); i++) {
				synchronized (broadcastSubscribeMap){
					synchronized (broadcastSubscribeMap.get(microServicesBroadcast.get(m).get(i))) {
						broadcastSubscribeMap.get(microServicesBroadcast.get(m).get(i)).remove(m);
						if(broadcastSubscribeMap.get(microServicesBroadcast.get(m).get(i)).isEmpty())
							broadcastSubscribeMap.remove(microServicesBroadcast.get(m).get(i));
					}
				}
			}

		// removes the inbox
		synchronized (m) {
			messageMap.remove(m);
		}
		// removes the list of requests the micro service has submitted to.
		if(microServicesRequests.containsKey(m))
			microServicesRequests.remove(m);
		// removes the list of broadcasts the micro service has submitted to.
		if(microServicesBroadcast.containsKey(m))
			microServicesBroadcast.remove(m);
	}
	
	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		synchronized (m) {
			if(messageMap.containsKey(m))
			{
				while(messageMap.get(m).isEmpty())
					m.wait();

				return messageMap.get(m).remove();
			}
			else throw new IllegalStateException();
		}
	}
}