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
The structure of this json file can be seen in the `simple_example.json` file attached.
This structure is composed of the next elements:
1. initialStorage: this element holds records for all the available shoes when on the first tick when the store is opened.
	attributes:</br>
	`shoeType` - string which declares the type of the shoes,</br>
	`amount`.</br>
2. services: declares the services which are running and their attributes.</br>
	a. time: `speed` - number of milliseconds each clock tick takes,</br>
			 `duration` - number of ticks before termination.</br>
	b. manager: `discountSchedule` - an array of discount schedules. every record consists from the following attributes:</br>
					`shoeType` - string which declares the type of the shoes,</br>
					`amount` - amount of shoes on sale,</br>
					`tick` - the tick number in which the sale starts.</br>
	c. factories: number of factories which are waiting for shoe requests.</br>
	d. sellers: number of sellers available to server.</br>
	e. customers: holds customer records. </br>
		attributes: </br>
			`name` - name of the customer,</br>
			`wishList` - contains name of shoe types that the client will buy only when there is a discount on them (and immidiatly when he found out of such discount),</br>
			`purchaseSchedule` - contains purchases that the client needs to make (every purchase has a corresponding time tick to send the PurchaseRequest).</br>

## Running shoe store simulation

From Terminal/cmd type:
```
java -jar path_of_clone/shoe_store.jar
```
insert the path of the json and enjoy :).
Note: be aware that a log file is generated.

## Built With

* [Maven](https://maven.apache.org/) - Software project management which manage project's build.
* [Gson](https://github.com/google/gson) - A Java serialization/deserialization library to convert Java Objects into JSON and back.

## Useful links

* The original source of the assignment: https://www.cs.bgu.ac.il/~spl161/wiki.files/SPL-Assignment2.pdf.
* https://en.wikipedia.org/wiki/Thread_(computing)
* https://en.wikipedia.org/wiki/Callback_(computer_programming)
