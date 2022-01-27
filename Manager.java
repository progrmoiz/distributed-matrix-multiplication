import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Logger;

// Private attribute host and port
public class Manager {

  // List of InetSocketAddress
  // Host address is string and port number is integer
  // Example: "localhost", 1234
  private static InetSocketAddress[] serverAddresses = {
      new InetSocketAddress("localhost", 1234),
      new InetSocketAddress("localhost", 5678),
  };

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

  public static void main(String[] args) {
    try {
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
        ObjectOutputStream oos2 = new ObjectOutputStream(clientSocket.getOutputStream());
        oos2.writeObject(chunks[i]);
        oos2.flush();

        // Wait for the client to send back the result
        ObjectInputStream ois2 = new ObjectInputStream(clientSocket.getInputStream());
        Integer[] result = (Integer[]) ois2.readObject();
        System.out.println("result= " + Arrays.toString(result));

        // Update the chunks
        chunks[i] = result;

        ois2.close();
        oos2.close();
        clientSocket.close();
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