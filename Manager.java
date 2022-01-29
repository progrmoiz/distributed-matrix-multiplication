import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

// servers
// localhost:8080
// localhost:8081

// chunks
// Job 1 [0, 1, 2, 3, 4]
// Job 2 [5, 6, 7, 8, 9]
// Job 3 [10, 11, 12, 13, 14]
// Job 4 [15, 16, 17, 18, 19]

// Create jobs from chunks (jobs = chunks)

// Enqueue all jobs
// Get list of all free server for example if two are free then send job to both (dequeue and execute)

// Private attribute host and port
public class Manager {
  // Server socket for accepting client connections
  private ServerSocket serverSocket;

  // Logger for this class
  private static final Logger LOGGER = Logger.getLogger(Manager.class.getName());

  // List of InetSocketAddress
  // Host address is string and port number is integer
  // Example: "localhost", 1234
  private InetSocketAddress[] workerAddresses = {};

  // Key value pair of busy INetSocketAddress and busy boolean
  // Example: {new InetSocketAddress("localhost", 1234), false}
  private static Map<String, Boolean> workerStatus = new HashMap<>();

  private int partitionSize;

  // Add to workerAddresses
  public void addWorker(InetSocketAddress workerAddress) {
    workerAddresses = Arrays.copyOf(workerAddresses, workerAddresses.length + 1);
    workerAddresses[workerAddresses.length - 1] = workerAddress;
  }

  // Manager constructor
  public Manager(int partitionSize) {
    this.partitionSize = partitionSize;
  }

  public Manager() {
    this.partitionSize = 2;
  }

  /**
   * Start a server socket and wait for a connection.
   *
   * @param port the port number to listen on
   */
  public void start(int port) {
    try {
      // Check if workerAddresses is empty, terminate immediately
      if (workerAddresses.length == 0) {
        LOGGER.info("No worker available");
        return;
      }

      for (InetSocketAddress workerAddress : workerAddresses) {
        workerStatus.put(Helper.inetSocketAddressToString(workerAddress), false);
      }

      // Create a server socket
      serverSocket = new ServerSocket(port);
      // Print the IP address and port number
      LOGGER.info("Server started on " + serverSocket.getInetAddress() + ":" + serverSocket.getLocalPort());

      // Print listening message
      LOGGER.info("Listening for connections...");

      // Wait for a client to connect
      while (true) {
        // Accept client connection and create a new thread for it
        new ManagerClientHandler(serverSocket.accept()).start();
      }

    } catch (IOException e) {
      LOGGER.info("Connection failed");
      e.printStackTrace();
    }
  }

  public void stop() {
    try {
      serverSocket.close();
    } catch (IOException e) {
      LOGGER.info("Closing connection failed");
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Divide integer array into chunks of size n
  public static Integer[][] divide(Integer[] ints, int n) {
    int numChunks = ints.length / n;
    Integer[][] chunks = new Integer[numChunks][n];
    for (int i = 0; i < numChunks; i++) {
      for (int j = 0; j < n; j++) {
        chunks[i][j] = ints[i * n + j];
      }
    }
    return chunks;
  }

  // Merge all the integer arrays into one
  public static Integer[] merge(Integer[][] ints) {
    int totalLength = 0;
    for (Integer[] chunk : ints) {
      totalLength += chunk.length;
    }
    Integer[] merged = new Integer[totalLength];
    int index = 0;
    for (Integer[] chunk : ints) {
      for (Integer i : chunk) {
        merged[index++] = i;
      }
    }
    return merged;
  }

  /**
   * This is our manager job handler.
   * 1. Send a input to the worker
   * 2. Let the worker handle the job
   * 3. Receive a output from the worker
   *
   * @param Socket    clientSocket - The socket to the worker
   * @param Integer[] chunk - The data to send to the worker
   *
   * @return Integer[] - The data received from the worker
   */
  public static Integer[] job(Socket clientSocket, Integer[] chunk) throws IOException, ClassNotFoundException {
    // Write the chunk to the socket
    ObjectOutputStream oos2 = new ObjectOutputStream(clientSocket.getOutputStream());
    oos2.writeObject(chunk);
    oos2.flush();
    LOGGER.info("Sent chunk to worker, chunk: " + Arrays.toString(chunk));

    // Read the result from the socket
    ObjectInputStream ois2 = new ObjectInputStream(clientSocket.getInputStream());
    Integer[] result = (Integer[]) ois2.readObject();
    LOGGER.info("Received result: " + Arrays.toString(result));

    // Close the socket
    ois2.close();
    oos2.close();
    clientSocket.close();

    return result;
  }

  // Get all free servers
  private InetSocketAddress[] getFreeWorkers() {
    // Return empty array if no servers are free
    if (workerStatus.isEmpty()) {
      return new InetSocketAddress[0];
    }

    return Arrays.stream(workerAddresses)
        .filter((InetSocketAddress workerAddress) -> !workerStatus.get(Helper.inetSocketAddressToString(workerAddress)))
        .toArray(InetSocketAddress[]::new);
  }

  private class ManagerClientHandler extends Thread {
    private Socket clientSocket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    public ManagerClientHandler(Socket clientSocket) {
      this.clientSocket = clientSocket;
    }

    public void run() {
      try {
        outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        inputStream = new ObjectInputStream(clientSocket.getInputStream());

        Integer[] data = (Integer[]) inputStream.readObject();
        LOGGER.info("Received data: " + Arrays.toString(data));

        // Integer[] result = compute(data, 1);
        // LOGGER.info("Computed data: " + Arrays.toString(result));

        // Divide the integers array into chunks of size n
        int chunkSize = data.length / partitionSize;
        Integer[][] chunks = divide(data, chunkSize);

        // Create a copy of the chunks
        Integer[][] resultChunks = new Integer[chunks.length][chunks[0].length];

        // Print the chunks
        for (Integer[] chunk : chunks) {
          System.out.println("chunk= " + Arrays.toString(chunk));
        }

        List<Thread> threads = new ArrayList<>();
        // send the chunks to servers clients

        // Iterate over the chunks (4 chunks)
        // Get the free servers (2 free servers)
        // Create a new thread for each chunk depending on the free servers
        // - 2 chunks will be sent to 2 servers

        // send the chunks to servers clients in threads
        try {
          int chunkIndex = 0;

          while (true) { // 4 chunks
            InetSocketAddress[] workerAddresses = getFreeWorkers();

            // log all free servers
            LOGGER.info("Free servers: " + Arrays.toString(workerAddresses));

            if (workerAddresses.length == 0) {
              LOGGER.info("No free servers at the moment. Waiting for free servers...");
              Thread.sleep(1000);
              continue;
            }

            // calculate the remaining chunks
            int remainingChunks = chunks.length - chunkIndex;

            // If remaining chunks is less than the number of free servers,
            // assign the remaining chunks to the free servers
            if (workerAddresses.length > remainingChunks) {
              workerAddresses = Arrays.copyOfRange(workerAddresses, 0, remainingChunks);
            }

            // Iterate over the free servers
            for (InetSocketAddress workerAddress : workerAddresses) { // 1st server
              // Fix: Local variable chunkIndex defined in an enclosing scope must be final or
              // effectively final
              final int chunkIndexFinal = chunkIndex;

              // Set the server status to busy
              workerStatus.put(Helper.inetSocketAddressToString(workerAddress), true);

              Thread thread = new Thread(() -> {
                try {
                  // Log chunk index
                  LOGGER.info("Sending chunk " + chunkIndexFinal + " to worker " + Helper.inetSocketAddressToString(workerAddress));

                  // Create a new socket
                  Socket workerClientSocket = new Socket(workerAddress.getHostName(), workerAddress.getPort());
                  LOGGER.info("Connected to " + Helper.inetSocketAddressToString(workerAddress));

                  // Send the chunk to the server
                  Integer[] result = job(workerClientSocket, chunks[chunkIndexFinal]);

                  // Merge the result chunks
                  resultChunks[chunkIndexFinal] = result;

                  // Print the result
                  LOGGER.info("Result we got from worker: " + Arrays.toString(result));

                  // Close the socket
                  workerClientSocket.close();

                  // Free the server
                  LOGGER.info("Freeing worker: " + Helper.inetSocketAddressToString(workerAddress));
                  workerStatus.put(Helper.inetSocketAddressToString(workerAddress), false);

                  // Print hashmap workerStatus
                  // LOGGER.info("workerStatus: " + workerStatus.toString());
                } catch (IOException | ClassNotFoundException e) {
                  e.printStackTrace();
                }
              });
              threads.add(thread);
              thread.start();

              chunkIndex++; // 4th chunk

            }
 
            if (chunkIndex == chunks.length) {
              break;
            }

          }

        } catch (Exception e) {
          e.printStackTrace();
        }

        // join all the threads
        try {
          for (Thread thread : threads) {
            thread.join();
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        // Merge the results from the workers
        LOGGER.info("Merging results...");
        Integer[] merged = merge(resultChunks);
        LOGGER.info("Merged results: " + Arrays.toString(merged));

        // Send the merged result to the client
        outputStream.writeObject(merged);
        outputStream.flush();

        inputStream.close();
        outputStream.close();
        clientSocket.close();

      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  public static void main(String[] args) {
    Manager manager = new Manager(2);
    manager.addWorker(new InetSocketAddress("localhost", 9001));
    manager.addWorker(new InetSocketAddress("localhost", 9002));
    // manager.addWorker(new InetSocketAddress("localhost", 9003));
    manager.start(6666);
  }
}