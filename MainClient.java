import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Logger;

public class MainClient {
  private Socket clientSocket;
  private ObjectOutputStream outputStream;
  private ObjectInputStream inputStream;

  // Logger for this class
  private static final Logger LOGGER = Logger.getLogger(Manager.class.getName());
  
  public void startConnection(String ip, int port) {
    try {
      clientSocket = new Socket(ip, port);
      System.out.println("Connected to " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

      outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
      inputStream = new ObjectInputStream(clientSocket.getInputStream());
    } catch (IOException e) {
      System.out.println("Connection failed");
      e.printStackTrace();
    } catch (Exception e) {
      System.out.println(e);
      e.printStackTrace();
    }
  }

  public Integer[] receiveData() {
    try {
      return (Integer[]) inputStream.readObject();
    } catch (IOException e) {
      System.out.println("Receiving data failed");
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      System.out.println("Receiving data failed");
      e.printStackTrace();
    } catch (Exception e) {
      System.out.println(e);
      e.printStackTrace();
    }
    return null;
  }

  public Matrix receiveDataMatrixes() {
    try {
      return (Matrix) inputStream.readObject();
    } catch (IOException e) {
      System.out.println("Receiving data failed");
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      System.out.println("Receiving data failed");
      e.printStackTrace();
    } catch (Exception e) {
      System.out.println(e);
      e.printStackTrace();
    }
    return null;
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

  public void sendData(Matrix[] data) {
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
      inputStream.close();
      outputStream.close();
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
    // Integer[] ints = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };
    // Create an integer array of 1 million elements and fill it with random numbers
    // Integer[] ints = new Integer[1000000];
    // for (int i = 0; i < ints.length; i++) {
      // ints[i] = (int) (Math.random() * 100);
    // }

    Matrix matrixA = Matrix.random(4, 4);
    Matrix matrixB = Matrix.random(4, 4);

    Matrix[] matrices = { matrixA, matrixB };

    MainClient mainClient = new MainClient();
    mainClient.startConnection("localhost", 6666);
    LOGGER.info("Sending matrices to manager...");
    matrixA.show("A");
    matrixB.show("B");
    mainClient.sendData(matrices);

    // receive data
    Matrix receivedData = mainClient.receiveDataMatrixes();
    LOGGER.info("Received final output from manager.");
    receivedData.show("Final output");

    mainClient.stopConnection();
  }
}