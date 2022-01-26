import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Logger;

public class Manager {
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

  public static void main(String[] args) {
    try {
      ServerSocket ss = new ServerSocket(6666, 100);
      log("Server started");

      Socket mainClientSocket = ss.accept();
      log("Connected to " + mainClientSocket.getInetAddress() + ":" + mainClientSocket.getPort());

      // Accept Object from client
      ObjectInputStream ois = new ObjectInputStream(mainClientSocket.getInputStream());
      Integer[] ints = (Integer[]) ois.readObject();
      System.out.println("ints= " + Arrays.toString(ints));

      // DataInputStream dis = new DataInputStream(s.getInputStream());
      // String str = (String) dis.readUTF();
      // System.out.println("message= " + str);

      // if (str.equals("bye")) {
      // break;
      // }

      // int numChunks = ints.length / 10;
      // ints = compute(ints, numChunks);

      // Print the integers array
      // System.out.println("ints= " + Arrays.toString(ints));

      // Send the result back to the client
      // ObjectOutputStream oos = new
      // ObjectOutputStream(mainClientSocket.getOutputStream());
      // oos.writeObject(ints);
      // oos.flush();
      // oos.close();

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
        Socket clientSocket = ss.accept();
        ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
        oos.writeObject(chunks[i]);
        oos.flush();
        // oos.close();


        // Wait for the client to send back the result
        ObjectInputStream ois2 = new ObjectInputStream(clientSocket.getInputStream());
        Integer[] result = (Integer[]) ois2.readObject();
        System.out.println("result= " + Arrays.toString(result));
        // ois2.close();

        clientSocket.close();
      }

      mainClientSocket.close();
      ss.close();
    } catch (Exception e) {
      System.out.println(e);
    }
  }
}