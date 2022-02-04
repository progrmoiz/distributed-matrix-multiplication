import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Logger;

public class MainClient {
  private Socket clientSocket;
  private ObjectOutputStream outputStream;
  private ObjectInputStream inputStream;

  // Logger for this class
  private static final Logger LOGGER = Logger.getLogger(Manager.class.getName());

  /**
   * Create a socket connection to the server
   *
   * @param ip   The IP address of the server.
   * @param port The port to connect to.
   */
  public void startConnection(String ip, int port) throws IOException {
    clientSocket = new Socket(ip, port);
    LOGGER.info("Connected to " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

    outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
    inputStream = new ObjectInputStream(clientSocket.getInputStream());
  }

  /**
   * It reads the data from the input stream and returns it as a matrix
   *
   * @return Nothing is being returned.
   */
  public Matrix receiveData() {
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

  /**
   * Send the data to the server
   *
   * @param data The data to send.
   */
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

  /**
   * It closes the connection.
   */
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
    Matrix matrixA = Matrix.random(100, 100);
    Matrix matrixB = Matrix.random(100, 100);

    for (int i = 0; i < 100; i++) {
      for (int j = 0; j < 100; j++) {
        matrixA.set(i, j, 5);
        matrixB.set(i, j, 5);
      }
    }

    Matrix[] matrices = { matrixA, matrixB };

    // Get host and port from command line
    // String ip = args[0];
    // int port = Integer.parseInt(args[1]);

    // if (args.length != 2) {
    // System.out.println("Usage: java MainClient <host> <port>");
    // System.exit(1);
    // }

    // if (port == 0) {
    // System.out.println("Usage: java MainClient <host> <port>");
    // System.exit(1);
    // }

    try {
      MainClient mainClient = new MainClient();
      mainClient.startConnection("localhost", 6666);
      LOGGER.info("Sending following matrices to manager...");
      // matrixA.show("A");
      // matrixB.show("B");
      mainClient.sendData(matrices);

      // receive data
      Matrix receivedData = mainClient.receiveData();
      LOGGER.info("Received final output from manager.");
      receivedData.show("A x B");

      mainClient.stopConnection();
    } catch (IOException e) {
      LOGGER.severe("Connection failed");
      LOGGER.severe("Exiting...");
    }
  }
}