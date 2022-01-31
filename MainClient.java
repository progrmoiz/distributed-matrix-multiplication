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

  public void startConnection(String ip, int port) throws IOException {
    clientSocket = new Socket(ip, port);
    LOGGER.info("Connected to " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

    outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
    inputStream = new ObjectInputStream(clientSocket.getInputStream());
  }

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
    Matrix matrixA = Matrix.random(4, 4);
    Matrix matrixB = Matrix.random(4, 4);

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
      LOGGER.info("Sending matrices to manager...");
      matrixA.show("A");
      matrixB.show("B");
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