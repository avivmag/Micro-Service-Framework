# Micro Service Framework

The program is composed of two main parts:
1. A simple, yet, powerful micro-service framework.
2. Implementation of a simple shoe store application on top of the framework.

The development of this simulation was done in pairs as part of an assignment in "Systems Programming" course at Ben-Gurion University in the begining of 2016.

A detailed description of the framework and the implementation can be found in the assignment desciption attached - section 2 and 3.

## Java Concurrency and Synchronization
This assignment is a great example for handling threads and the shared memory between them.

Threads can be powerful, yet, very dangerous. When dealing with threads you must always act cautiously.
```
In this assignment we've ensured that best performance is always reached through parallelizing threads, all of that while unwanted behavior would never occurs.
```

We did so by targeting specifically the shared resources and synchronize them so only one thread can manipulate them simultaneously.

We also used the 'synchronized-while-wait' mechanism so thread which has no current job will sleep until there is a reason to awake them.

## Callback
Another important mechanism we used are callbacks. We used those for times we have code we wanted to be ran only for times when trigger occurs,
for instance - a request for buying shoes has been completed.

## Getting Started
### Prerequisites

1. Java SE Runtime Environment 8 (at least), can be found: 
	http://www.oracle.com/technetwork/java/javase/downloads/index.html
	
To run the shoe-store simulation, a json file is needed to be configured.
The structure of this json file can be seen in the `example.json` file attached.
This structure is composed of the next elements:
1. "initialStorage": this element stores records of all 
