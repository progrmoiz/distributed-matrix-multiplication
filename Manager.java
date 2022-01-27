import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
  private static InetSocketAddress[] serverAddresses = {
      new InetSocketAddress("localhost", 1234),
      new InetSocketAddress("localhost", 5678),
  };

  // Key value pair of busy INetSocketAddress and busy boolean
  // Example: {new InetSocketAddress("localhost", 1234), false}
  private static Map<String, Boolean> busyServers = new HashMap<>();

  // Get all servers
  private static String[] getAllServers() {
    return Arrays.stream(serverAddresses)
        .map((InetSocketAddress serverAddress) -> serverAddress.getHostName() + ":" + serverAddress.getPort())
        .toArray(String[]::new);
  }

  // Get all busy servers

  // Get all free servers
  private static InetSocketAddress[] getFreeServers() {
    // serverAddresses - busyServers
    return new ArrayList<>(Arrays.asList(serverAddresses)).stream()
        .filter(server -> !busyServers.containsKey(server.toString()))
        .toArray(InetSocketAddress[]::new);
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
    try {
      System.out.println("All servers: " + Arrays.toString(getAllServers()));

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
      int chunkSize = ints.length / 2;
      Integer[][] chunks = divide(ints, chunkSize);

      // Print the chunks
      for (Integer[] chunk : chunks) {
        System.out.println("chunk= " + Arrays.toString(chunk));
      }

      // Send the chunks to the server clients
      for (int i = 0; i < chunks.length; i++) {
        log("Sending chunk " + i + " to worker " + i);

        Socket clientSocket = new Socket(serverAddresses[i].getHostName(), serverAddresses[i].getPort());
        // Add this server to busy servers list
        // busyServers.put(serverAddresses[i], true);
        System.out.println("busyServers= " + serverAddresses[i].toString());

        log("Connected to " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

        task(clientSocket, chunks[i]);

        System.out.println("Reachable: " + clientSocket.getInetAddress().isReachable(1000));
      }

      // Merge the results from the workers
      System.out.println("Merging results");
      Integer[] merged = merge(chunks);
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