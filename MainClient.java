import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;

public class MainClient {
  private Socket clientSocket;
  private ObjectOutputStream outputStream;
  private ObjectInputStream inputStream;

  public void startConnection(String ip, int port) {
    try {
      clientSocket = new Socket(ip, port);
      System.out.println("Connected to " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

      outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
      // inputStream = new ObjectInputStream(clientSocket.getInputStream());
    } catch (IOException e) {
      System.out.println("Connection failed");
      e.printStackTrace();
    } catch (Exception e) {
      System.out.println(e);
      e.printStackTrace();
    }
  }

  public void sendData(Integer[] data) {
    try {
      outputStream.writeObject(data);
      outputStream.flush();
    } catch (IOException e) {
      System.out.println("Sending data failed");
      e.printStackTrace();
    } catch (Exception e) {
      System.out.println(e);
      e.printStackTrace();
    }
  }

  public void stopConnection() {
    try {
      outputStream.close();
      // inputStream.close();
      clientSocket.close();
    } catch (IOException e) {
      System.out.println("Closing connection failed");
      e.printStackTrace();
    } catch (Exception e) {
      System.out.println(e);
      e.printStackTrace();
    }
  }


  public static void main(String[] args) {
      // integer collection
      Integer[] ints = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
      
      MainClient mainClient = new MainClient();
      mainClient.startConnection("localhost", 6666);
      mainClient.sendData(ints);

      mainClient.stopConnection();
    
    // try {
    //   // integer collection
    //   Integer[] ints = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

    //   // Print the integers array
    //   System.out.println("ints= " + Arrays.toString(ints));

    //   Socket s = new Socket("localhost", 6666);
    //   System.out.println("Connected to " + s.getInetAddress() + ":" + s.getPort());
    //   ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
      
      
    //   oos.writeObject(ints);
    //   oos.flush();


    //   oos.close();

    //   // Recieve the result from the server
    //   // ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
    //   // ints = (Integer[]) ois.readObject();
    //   // System.out.println("ints= " + Arrays.toString(ints)); 

    //   // DataOutputStream dos = new DataOutputStream(s.getOutputStream());
    //   // dos.writeUTF("bye");
    //   // dos.flush();
    //   // dos.close();
    //   s.close();
    // } catch (Exception e) {
    //   System.out.println(e);
    // }
  }
}