import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;

public class MainClient {
  public static void main(String[] args) {
    try {
      // integer collection
      Integer[] ints = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

      // Print the integers array
      System.out.println("ints= " + Arrays.toString(ints));

      Socket s = new Socket("localhost", 6666);
      ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
      oos.writeObject(ints);
      oos.flush();
      oos.close();

      // Recieve the result from the server
      // ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
      // ints = (Integer[]) ois.readObject();
      // System.out.println("ints= " + Arrays.toString(ints)); 

      // DataOutputStream dos = new DataOutputStream(s.getOutputStream());
      // dos.writeUTF("bye");
      // dos.flush();
      // dos.close();
      s.close();
    } catch (Exception e) {
      System.out.println(e);
    }
  }
}