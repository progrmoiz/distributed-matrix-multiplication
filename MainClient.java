import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * It connects to the manager and sends the data to the manager and receives the
 * result from the manager.
 */
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
    // LOGGER.info("Connected to " + clientSocket.getInetAddress() + ":" +
    // clientSocket.getPort());

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

    // Testing time for these matrices
    // int[] matrixDimensions = { 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768 };
    int[] matrixDimensions = { 16, 32};

    for (int d = 0; d < matrixDimensions.length; d++) {

      // long startTime = System.currentTimeMillis(); // for measuring time
      int dim = matrixDimensions[d];

      // Create the random matrices
      Matrix matrixA = Matrix.random(dim, dim);
      Matrix matrixB = Matrix.random(dim, dim);

      Matrix[] matrices = { matrixA, matrixB };

      try {
        MainClient mainClient = new MainClient();
        mainClient.startConnection("localhost", 6666);
        LOGGER.info("Sending following " + dim + "x" + dim + " matrices to manager...");
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

      // long endTime = System.currentTimeMillis();
      // long duration = endTime - startTime;

      // duration in seconds
      // double durationInSeconds = duration / 1000.0;

      // print "Time taken for n = dim is
      // System.out.println("Time taken for " + dim + "x" + dim + " matrix is " + durationInSeconds + " seconds");
    }
  }
}