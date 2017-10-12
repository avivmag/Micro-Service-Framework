package bgu.spl.app.tests;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import bgu.spl.app.Store;
import bgu.spl.app.Store.BuyResult;
import bgu.spl.app.store_objects.ShoeStorageInfo;

public class StoreTest {
	
	@Before
	public void setUp() throws Exception {
		ShoeStorageInfo[] info = new ShoeStorageInfo[] { new ShoeStorageInfo("alibaba", 3), new ShoeStorageInfo("thunder", 3)};
		Store.getInstance().load(info);
	}

	@After
	public void tearDown() throws Exception {
		
	}

	@Test
	public void testGetInstance() {
		Store.BuyResult buyResult = Store.getInstance().take("alibaba", false);
		assertEquals(buyResult, Store.BuyResult.REGULAR_PRICE);
	}

	@Test
	public void testLoad() {
		ShoeStorageInfo[] info = new ShoeStorageInfo[] { new ShoeStorageInfo("testLoad", 1)};
		Store.getInstance().load(info);
		
		Store.BuyResult buyResult = Store.getInstance().take("testLoad", false);
		assertEquals(buyResult, Store.BuyResult.REGULAR_PRICE);
	}

	@Test
	public void testTake() {
		Store.BuyResult buyResult = Store.getInstance().take("thunder", false);
		assertEquals(buyResult, Store.BuyResult.REGULAR_PRICE);
	}

	@Test
	public void testAdd() {		
		Store.getInstance().add("testAdd", 1);
		
		Store.BuyResult buyResult = Store.getInstance().take("testAdd", false);
		assertEquals(buyResult, Store.BuyResult.REGULAR_PRICE);
	}

	@Test
	public void testAddDiscount() {
		Store.getInstance().add("testAddDiscount", 1);
		Store.getInstance().addDiscount("testAddDiscount", 1);
		
		Store.BuyResult buyResult = Store.getInstance().take("testAddDiscount", true);
		assertEquals(buyResult, Store.BuyResult.DISCOUNTED_PRICE);

	}

}
