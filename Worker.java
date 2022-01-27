import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class Worker {
  // Server socket for accepting client connections
  private ServerSocket serverSocket;

  /**
   * Start a server socket and wait for a connection.
   *
   * @param port the port number to listen on
   */
  public void start(int port) {
    try {
      // Create a server socket
      serverSocket = new ServerSocket(port);
      // Print the IP address and port number
      System.out.println("Server started on " + serverSocket.getInetAddress() + ":" + serverSocket.getLocalPort());

      // Print listening message
      System.out.println("Listening for connections...");

      // Wait for a client to connect
      while (true) {
        // Accept client connection and create a new thread for it
        new WorkerClientHandler(serverSocket.accept()).start();
      }

    } catch (IOException e) {
      System.out.println("Connection failed");
      e.printStackTrace();
    }
  }

  public void stop() {
    try {
      serverSocket.close();
    } catch (IOException e) {
      System.out.println("Closing connection failed");
      e.printStackTrace();
    } catch (Exception e) {
      System.out.println(e);
      e.printStackTrace();
    }
  }

  private static class WorkerClientHandler extends Thread {
    private Socket clientSocket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    public WorkerClientHandler(Socket clientSocket) {
      this.clientSocket = clientSocket;
    }

    public static Integer[] compute(Integer[] ints, int n) {
      for (int i = 0; i < ints.length; i++) {
        ints[i] += n;
      }
      return ints;
    }

    public void run() {
      try {
        outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        inputStream = new ObjectInputStream(clientSocket.getInputStream());

        Integer[] data = (Integer[]) inputStream.readObject();
        System.out.println("Received data: " + Arrays.toString(data));

        Integer[] result = compute(data, 1);
        System.out.println("Computed data: " + Arrays.toString(result));

        outputStream.writeObject(result);
        outputStream.flush();

        inputStream.close();
        outputStream.close();
        clientSocket.close();

      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  public static void main(String[] args) {
    // Accept the port number from the command line
    int port = Integer.parseInt(args[0]);

    // Print help message if no port number is given
    if (port == 0) {
      System.out.println("Usage: java Worker <port>");
      System.exit(0);
    }

    // Start the server
    Worker worker = new Worker();
    worker.start(port);
  }
}