# Distributed Matrix Multiplication

This project shows how to use the distributed system to perform some computations, in our case, we will use the matrix multiplication.

## Artitecture diagram
![Distributed Matrix Multiplication Artitecture](/images/architecture.jpg)

## How does it work?

So, we have three roles:

* Client - the client is the one who will send the data to the server.
* Manager - the manager is responsible for communicating with the client and workers.
* Worker - the worker is the one who will perform the computation.

Client will send the two matrix (AxB) to our Manager (server) and the Manager will divide those two matrix into chunks and distribute them to the workers. Then the workers will perform the multiplication and send the result back to the Manager. The Manager will merge the result and send it back to the Client.

## How to install the project?

First of all, clone the repository:
```
$ git clone https://github.com/progrmoiz/distributed-matrix-multiplication
```

## How to run the project?

First of all, let's compile and run the Worker:
```java
$ javac Worker.java
$ java Worker 9001
```

You can run as many workers as you like and workers can be run on the same network or on different network.

> In the case of different network, you can also use Ngrok to expose the port.

Then, we have to first define our worker's IP address and port in the Manager. You can check the Manager's `main` function code to see how to do that. Then, we can compile and run the Manager:
```java
$ javac Manager.java
$ java Manager
```

> Manager is by default listening on port 6666.

Then, we can run the Client. You can change the input matrix in the Client's `main` function. So, let's now compile and run the Client:
```java
$ javac Client.java
$ java Client
```

Hurray! You can see the result in the console.

## Performance
We will be running the code with randomly generated inputs for matrix dimensions of 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192.

We tested the performance with randomly generated inputs for matrix dimensions of 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192.

We work out two examples:
1. Single System
2. Distributed System (three separate workers)

### System specs:
|     |     |
| --- | --- |
| **Processor** | Intel® Core™ i7-9700 CPU @ 3.00GHz × 8 |
| **RAM** | 31.3 GiB |
| **System type** | 64-bit Operating System, x64-based processor |

### 1. Single System
#### Result:
![Performance outputs when using a single machine](/images/single-machine-performance.jpeg)

### 2. Distributed System (three separate workers)
Workers are running separate machines with the same system specs. We are using three workers for this test.

#### Result:
![Performance outputs when using distributed systems](/images/distributed-machine-performance.jpeg)

### Final results
Comparing single vs distributed in tabular data.
![Comparing single vs distributed](/images/comparing-single-vs-distributed.jpeg)

Single vs Distributed System comparison
![Single vs Distributed System comparison](/images/single-vs-distributed-comparision.jpeg)