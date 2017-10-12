package bgu.spl.mics.impl;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.RequestCompleted;

public class MessageBusImplTest {

	TestMicroService customer;
	TestMicroService seller;


	@Before
	public void setUp() throws Exception {
		customer = new TestMicroService("testCustomer");
		seller = new TestMicroService("testSeller");
		MessageBusImpl.MessageBus().register(customer);
		MessageBusImpl.MessageBus().register(seller);
	}

	@After
	public void tearDown() throws Exception {
		MessageBusImpl.MessageBus().unregister(customer);
		MessageBusImpl.MessageBus().unregister(seller);
	}

	@Test
	public void testRequests() {
		MessageBusImpl.MessageBus().subscribeRequest(TestRequest.class, seller);
		MessageBusImpl.MessageBus().sendRequest(new TestRequest("Hi its the customer, HELP!!"), customer);
		TestRequest request = null;
		try { request = (TestRequest) MessageBusImpl.MessageBus().awaitMessage(seller); } catch (InterruptedException e) { }
		assertEquals(request.getS(), "Hi its the customer, HELP!!");
		MessageBusImpl.MessageBus().complete(request, "Got that nudnik");
		RequestCompleted<String> requestCompleted = null;
		try { requestCompleted = (RequestCompleted<String>) MessageBusImpl.MessageBus().awaitMessage(customer); } catch (InterruptedException e) { }
		assertEquals(requestCompleted.getResult(), "Got that nudnik");
	}

	@Test
	public void testBroadcasts() {
		MessageBusImpl.MessageBus().subscribeBroadcast(TestBroadcast.class, customer);
		MessageBusImpl.MessageBus().sendBroadcast(new TestBroadcast("GO TO SLEEP!"));
		TestBroadcast broadcast = null;
		try { broadcast = (TestBroadcast) MessageBusImpl.MessageBus().awaitMessage(customer); } catch (InterruptedException e) { }
		assertEquals(broadcast.getS(), "GO TO SLEEP!");
	}
	
	private class TestMicroService extends MicroService
	{
		public TestMicroService(String name) {
			super(name);
		}

		@Override
		protected void initialize() {

		}
	}

	private class TestRequest implements Request<String>
	{
		private String s;

		public TestRequest(String s)
		{
			this.s = s;
		}

		public String getS() {
			return s;
		}
	}

	private class TestBroadcast implements Broadcast
	{
		private String s;

		public TestBroadcast(String s)
		{
			this.s = s;
		}

		public String getS() {
			return s;
		}
	}
}
