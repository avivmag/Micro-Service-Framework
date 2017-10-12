package bgu.spl.app;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.google.gson.Gson;

/**
 * The main program which will start all the threads.
 */
public class ShoeStoreRunner {
	static Logger logger = Logger.getLogger("MyLog");

	public static void main(String[] args) {	

		/**
		 * A class which we use so the logger would only have the wanted data and no more.
		 */
		class BriefFormatter extends Formatter 
		{   
			public BriefFormatter() { super(); }

			@Override
			public String format(LogRecord record) {
				return record.getMessage() + "\n" ;
			}   
		}

		// Logger
		FileHandler fh;
		try {
			// This block configure the logger with handler and formatter  
			fh = new FileHandler("../LogFile.log");  
			logger.addHandler(fh);
			fh.setFormatter(new BriefFormatter());
		} catch (SecurityException e) {  
			throw new SecurityException("There was a security issue when trying to work with the logger: " + e.getMessage());  
		} catch (IOException e) {  
			throw new SecurityException("There was a input-output issue when trying to work with the logger: " + e.getMessage());  
		}

		BufferedReader br = null;
		try { br = new BufferedReader(new FileReader(args[0])); } 
		catch (FileNotFoundException e) { try {throw new FileNotFoundException("There json file could not be found.");} catch (FileNotFoundException e1) {}}
		catch (ArrayIndexOutOfBoundsException e) {
			boolean tryAgain = true;
			ShoeStoreRunner runner = null;
			while (tryAgain){
				// ask for the json name.
				System.out.println("Please Enter a json path:");
				Scanner sc = new Scanner(System.in);
				String address = sc.nextLine();
				try {
					br = new BufferedReader(new FileReader(address));
					tryAgain = false;
				} catch (FileNotFoundException e1) {
					System.out.println("json not found, please try again.\n");
				}	
			}
		}
		Gson gson = new Gson();
		ClassInstanceHolder classInstanceHolder = gson.fromJson(br, ClassInstanceHolder.class);

		// the count down latch we wait for to start running the program.
		CountDownLatch countDownLatch = new CountDownLatch(classInstanceHolder.getNumOfRunnables() - 1);
		classInstanceHolder.initialize(countDownLatch);		


		//Run all threads.
		List<Thread> microServiceThreadList = new ArrayList<Thread>();

		for (Runnable runnable : classInstanceHolder.getShoeFactoryServiceList()) {
			microServiceThreadList.add(new Thread(runnable));
		}
		for (Runnable runnable : classInstanceHolder.getSellingServiceList()) {
			microServiceThreadList.add(new Thread(runnable));
		}
		microServiceThreadList.add(new Thread(classInstanceHolder.getManagmentService()));
		for (int i = 0; i < classInstanceHolder.getCustomers().length; i++) {
			microServiceThreadList.add(new Thread(classInstanceHolder.getCustomers()[i]));
		}

		for (Thread thread : microServiceThreadList) {
			thread.start();
		}

		// wait until all threads are ready
		try { countDownLatch.await(); } catch (InterruptedException e) { }

		microServiceThreadList.add(new Thread(classInstanceHolder.getTimeService()));
		microServiceThreadList.get(microServiceThreadList.size() - 1).start();

		// wait for all threads to finish.
		for (Thread thread : microServiceThreadList) {
			try {thread.join();} catch (InterruptedException e) {}
		}
		logger.info("Main: all threads are terminated, bye!");
		Store.getInstance().print();
	}
}
