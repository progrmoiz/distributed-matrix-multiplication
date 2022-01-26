import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
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

  private Socket clientSocket;
  private ObjectOutputStream outputStream;
  private ObjectInputStream inputStream;

  public void startConnection(Socket clientSocket) {
    try {
      this.clientSocket = clientSocket;
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

  public void stopConnection() {
    try {
      outputStream.close();
      inputStream.close();
      clientSocket.close();
    } catch (IOException e) {
      System.out.println("Closing connection failed");
      e.printStackTrace();
    } catch (Exception e) {
      System.out.println(e);
      e.printStackTrace();
    }
  }

  public static Socket pingServer(String ip, int port, int timeout) {
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress(ip, port), timeout);
      return socket;
    } catch (IOException e) {
      return null; // Either timeout or unreachable or failed DNS lookup.
    }
  }

  public static void main(String[] args) {
    // Socket socket;

    // // Ping the server localhost 6666 to keep the connection alive
    // while((socket = pingServer("localhost", 6666, 1000)) == null) {
    //   System.out.println("Server not reachable, retrying in 1 second");
    //   try {
    //     Thread.sleep(1000);
    //   } catch (InterruptedException e) {
    //     e.printStackTrace();
    //   }
    // }

    WorkerClient workerClient = new WorkerClient();
    workerClient.startConnection("localhost", 6666);

    // Receive data from the server
    Integer[] ints = workerClient.receiveData();
    System.out.println("Received data: " + Arrays.toString(ints));

    // Compute the result
    ints = compute(ints, 1);
    System.out.println("Computed data: " + Arrays.toString(ints));

    // Send the computed data back to the main client
    workerClient.sendData(ints);

    // Close the connection
    workerClient.stopConnection();
  }
}