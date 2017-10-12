package bgu.spl.app;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.google.gson.annotations.SerializedName;

import bgu.spl.mics.MicroService;
/**
 * Holds instances to store and services and initialize them by the given json.
 */
public class ClassInstanceHolder {
	@SerializedName("initialStorage")
	private ShoeStorageInfo[] initialStorage;
	
	@SerializedName("services")
	private Services services = new Services();
	
	private List<ShoeFactoryService> shoeFactoryServiceList;
	private List<SellingService> sellingServiceList;
	
	public class Services
	{
		@SerializedName("time")
		private TimeService time;
		@SerializedName("manager")
		private ManagmentService manager;
		@SerializedName("factories")
		private int factories;
		@SerializedName("sellers")
		private int sellers;
		@SerializedName("customers")
		private WebsiteClientServer[] customers; 
		
		public TimeService getTime() {
			return time;
		}
		public ManagmentService getManager() {
			return manager;
		}
		public int getFactories() {
			return factories;
		}
		public int getSellers() {
			return sellers;
		}
		public WebsiteClientServer[] getCustomers() {
			return customers;
		}
	}
	/**
	 * Initialize the instances.
	 * @param countDownLatch 
	 */
	public void initialize(CountDownLatch countDownLatch)
	{
		shoeFactoryServiceList = new ArrayList<ShoeFactoryService>();
		sellingServiceList = new ArrayList<SellingService>();
		
		for (int i = 0; i < services.factories; i++) {
			shoeFactoryServiceList.add(new ShoeFactoryService("Factory " + i));
			shoeFactoryServiceList.get(i).setCountDownLatch(countDownLatch);
		}
		
		for (int i = 0; i < services.sellers; i++) {
			sellingServiceList.add(new SellingService("seller " + i));
			sellingServiceList.get(i).setCountDownLatch(countDownLatch);
		}
		
		for (WebsiteClientServer customer : services.getCustomers()) {
			customer.setCountDownLatch(countDownLatch);
		}
		
		services.manager.setCountDownLatch(countDownLatch);
		
		Store.getInstance().load(initialStorage);
	}

	public List<ShoeFactoryService> getShoeFactoryServiceList() {
		return shoeFactoryServiceList;
	}

	public List<SellingService> getSellingServiceList() {
		return sellingServiceList;
	}
	
	public TimeService getTimeService()
	{
		return services.getTime();
	}
	
	public ManagmentService getManagmentService()
	{
		return services.getManager();
	}
	
	public WebsiteClientServer[] getCustomers() {
		return services.getCustomers();
	}
	
	public int getNumOfRunnables(){
		return services.factories + services.sellers + services.customers.length + 2;
	}
}
