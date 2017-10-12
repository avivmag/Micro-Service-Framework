# Micro Service Framework

The program is composed of two main parts:
1. A simple, yet, powerful micro-service framework.
2. Implementation of a simple shoe store application on top of the framework.

The development of this simulation was done in pairs as part of an assignment in "Systems Programming" course at Ben-Gurion University in the begining of 2016.

A detailed description of the framework and the implementation can be found in the assignment desciption attached - section 2 and 3.

## Java Concurrency and Synchronization
This assignment is a great example for handling threads and the shared memory between them.

Threads can be powerful yet unpredictable. When dealing with threads, one must always act cautiously.
In this assignment we've made sure that side effects from parallelizing threads would never occur, all the while trying to reach the best performance available.

We also used the 'synchronized-while-wait' mechanism so thread which has no current job will sleep until there is a reason to awake them.

## Callback
Another notable mechanism we used is callbacks. We used callbacks when we had chunks of code we wanted to be ran when an event was triggered,
for instance - a request for buying shoes has been completed.

## Getting Started
### Prerequisites

1. Java SE Runtime Environment 8 (at least), can be found: 
	http://www.oracle.com/technetwork/java/javase/downloads/index.html
	
To run the shoe-store simulation, a json file is needed to be configured.
The structure of this json file can be seen in the `example.json` file attached.
This structure is composed of the next elements:
1. initialStorage: this element holds records for all the available shoes when on the first tick when the store is opened.
	attributes:
	`shoeType` - string which declares the type of the shoes,
	`amount`.
2. services: declares the services which are running and their attributes.
	a. time: `speed` - number of milliseconds each clock tick takes,
			 `duration` - number of ticks before termination.
	b. manager: `discountSchedule` - an array of discount schedules. every record consists from the following attributes:
					`shoeType` - string which declares the type of the shoes,
					`amount` - amount of shoes on sale,
					`tick` - the tick number in which the sale starts.
	c. factories: number of factories which are waiting for shoe requests.
	d. sellers: number of sellers available to server.
	e. customers: holds customer records. 
		attributes: 
			`name` - name of the customer,
			`wishList` - contains name of shoe types that the client will buy only when there is a discount on them (and immidiatly when he found out of such discount),
			`purchaseSchedule` - contains purchases that the client needs to make (every purchase has a corresponding time tick to send the PurchaseRequest).
