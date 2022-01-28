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

  // List of InetSocketAddress
  // Host address is string and port number is integer
  // Example: "localhost", 1234
  private final static InetSocketAddress[] SERVER_ADDRESSES = {
      new InetSocketAddress("localhost", 1234),
      new InetSocketAddress("192.168.1.105", 1234),
  };

  // Key value pair of busy INetSocketAddress and busy boolean
  // Example: {new InetSocketAddress("localhost", 1234), false}
  private static Map<String, Boolean> serverStatus = new HashMap<>();

  // Get all free servers
  private static InetSocketAddress[] getFreeServers() {
    return Arrays.stream(SERVER_ADDRESSES)
        .filter((InetSocketAddress serverAddress) -> !serverStatus.get(inetSocketAddressToString(serverAddress)))
        .toArray(InetSocketAddress[]::new);
  }

  // Inet socket address to string
  private static String inetSocketAddressToString(InetSocketAddress inetSocketAddress) {
    return inetSocketAddress.getHostString() + ":" + inetSocketAddress.getPort();
  }

  // Logger function
  public static void log(String msg) {
    Logger.getLogger("Manager").info(msg);
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

  public static Integer[] compute(Integer[] ints, int n) {
    for (int i = 0; i < ints.length; i++) {
      ints[i] += n;
    }
    return ints;
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

  // Create a custom task function
  public static Integer[] task(Socket clientSocket, Integer[] chunk) throws IOException, ClassNotFoundException {
    // Write the chunk to the socket
    ObjectOutputStream oos2 = new ObjectOutputStream(clientSocket.getOutputStream());
    oos2.writeObject(chunk);
    oos2.flush();

    // Read the result from the socket
    ObjectInputStream ois2 = new ObjectInputStream(clientSocket.getInputStream());
    Integer[] result = (Integer[]) ois2.readObject();
    System.out.println("result= " + Arrays.toString(result));

    ois2.close();
    oos2.close();
    clientSocket.close();

    // Print the server toString
    System.out.println("Server: " + clientSocket.getInetAddress().toString());

    return result;
  }

  public static void main(String[] args) {
    // Add all servers to serverStatus with false
    for (InetSocketAddress serverAddress : SERVER_ADDRESSES) {
      serverStatus.put(inetSocketAddressToString(serverAddress), false);
    }

    try {
      System.out.println("FrRE servers: " + Arrays.toString(getFreeServers()));

      ServerSocket ss = new ServerSocket(6666, 100);
      log("Server started");

      Socket mainClientSocket = ss.accept();
      log("Connected to " + mainClientSocket.getInetAddress() + ":" + mainClientSocket.getPort());

      // Accept Object from client
      ObjectOutputStream oos = new ObjectOutputStream(mainClientSocket.getOutputStream());
      ObjectInputStream ois = new ObjectInputStream(mainClientSocket.getInputStream());

      Integer[] ints = (Integer[]) ois.readObject();
      System.out.println("ints= " + Arrays.toString(ints));

      // Divide the integers array into chunks of size n
      int partitions = 4;
      int chunkSize = ints.length / partitions;
      Integer[][] chunks = divide(ints, chunkSize);

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
          InetSocketAddress[] serverAddresses = {};

          try {
            serverAddresses = getFreeServers(); // 0 servers
          } catch (Exception e) {
            System.out.println("No servers available");
            Thread.sleep(1000);
            continue;
          }

          // log the servers
          log("Free servers: " + Arrays.toString(serverAddresses));

          // if (serverAddresses.length == 0) {
          //   log("No free servers at the moment. Waiting for free servers...");
          //   Thread.sleep(1000);
          //   continue;
          // }

          // log free servers
          log("Free servers: " + Arrays.toString(serverAddresses));

          // Iterate over the free servers
          for (InetSocketAddress serverAddress : serverAddresses) { // 1st server
            // Fix: Local variable chunkIndex defined in an enclosing scope must be final or
            // effectively final
            final int chunkIndexFinal = chunkIndex;

            // Set the server status to busy
            serverStatus.put(inetSocketAddressToString(serverAddress), true);

            Thread thread = new Thread(() -> {
              try {
                // Create a new socket
                Socket clientSocket = new Socket(serverAddress.getHostName(), serverAddress.getPort());
                log("Connected to " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

                // Send the chunk to the server
                Integer[] result = task(clientSocket, chunks[chunkIndexFinal]);

                // Merge the result chunks
                resultChunks[chunkIndexFinal] = result;

                // Print the result
                System.out.println("result= " + Arrays.toString(result));

                // Close the socket
                clientSocket.close();

                // Free the server
                log("Freeing server: " + serverAddress.toString());
                serverStatus.put(inetSocketAddressToString(serverAddress), false);
                // Print hashmap serverStatus
                log("serverStatus: " + serverStatus.toString());
              } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
              }
            });
            chunkIndex++; // 4th chunk

            threads.add(thread);
            thread.start();
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

      System.out.println("FREE servers: " + Arrays.toString(getFreeServers()));

      // log running this after joining the threads
      log("Running this after joining the threads");

      // Merge the results from the workers
      System.out.println("Merging results");
      Integer[] merged = merge(resultChunks);
      System.out.println("merged= " + Arrays.toString(merged));

      // Send the merged result to the client
      oos.writeObject(merged);
      oos.flush();

      ois.close();
      oos.close();
      mainClientSocket.close();
      ss.close();
    } catch (Exception e) {
      System.out.println(e);
    }
  }
}