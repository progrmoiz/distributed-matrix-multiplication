import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;

public class WorkerClient {
  // Computation function for the worker
  // Add n to each element of the array
  public static Integer[] compute(Integer[] ints, int n) {
    for (int i = 0; i < ints.length; i++) {
      ints[i] += n;
    }
    return ints;
  }

  public static void main(String[] args) {
    try {

      // Connect to the server
      Socket s = new Socket("localhost", 6666);

      // Read the integers array from the server
      ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
      Integer[] ints = (Integer[]) ois.readObject();
      System.out.println("ints= " + Arrays.toString(ints));

      // Do some work
      ints = compute(ints, 1);

      // Print the integers array
      System.out.println("ints= " + Arrays.toString(ints));

      // Send the result back to the server
      ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
      oos.writeObject(ints);
      oos.flush();
      // oos.close();

      s.close();

    } catch (Exception e) {
      System.out.println(e);
    }
  }
}