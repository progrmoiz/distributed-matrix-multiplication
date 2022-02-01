import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

// make a worker class multi-threaded

public class Worker {
  // Server socket for accepting client connections
  private ServerSocket serverSocket;

  // Logger for this class
  private static final Logger LOGGER = Logger.getLogger(Worker.class.getName());

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
      LOGGER.info("Server started on " + serverSocket.getInetAddress() + ":" + serverSocket.getLocalPort());

      // Print listening message
      LOGGER.info("Listening for connections...");

      // Wait for a client to connect
      while (true) {
        // Accept client connection and create a new thread for it, This thread will
        // handle the client separately
        // It is a multi client because this accept multiple request from multiple
        // client
        new WorkerClientHandler(serverSocket.accept()).start();
      }

    } catch (IOException e) {
      LOGGER.info("Connection failed");
      e.printStackTrace();
    }
  }

  public void stop() {
    try {
      serverSocket.close();
    } catch (IOException e) {
      LOGGER.info("Closing connection failed");
      e.printStackTrace();
    } catch (Exception e) {
      LOGGER.info(e.getMessage());
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

    public static class RowMultiply implements Runnable {
      private Matrix mat1;
      private Matrix mat2;
      private Matrix finalResult;

      public RowMultiply(Matrix finalResult, Matrix mat1, Matrix mat2) {
        this.finalResult = finalResult;
        this.mat1 = mat1;
        this.mat2 = mat2;
      }

      @Override
      public void run() {
        // this.result.set(i, j, value);
        // multiply mat1 by mat2 and add it to finalResult
        // System.out.println("Inside thread run and multipying:");
        // this.mat1.show();
        // this.mat2.show();

        this.finalResult.plusInPlace(mat1.times(mat2));
      }
    }

    public static class ThreadCreation {

      public static void multiply(Matrix[] mat1, Matrix[] mat2, Matrix finalResult) {
        List<Thread> threads = new ArrayList<>();

        int length = mat1.length;

        for (int i = 0; i < length; i++) {
          RowMultiply task = new RowMultiply(finalResult, mat1[i], mat2[i]);
          Thread thread = new Thread(task);
          thread.start();
          threads.add(thread);
          if (threads.size() % 50 == 0) {
            waitForThreads(threads);
          }
        }
      }

      private static void waitForThreads(List<Thread> threads) {
        threads.forEach(thread -> {
          try {
            thread.join();
          } catch (InterruptedException ex) {
            ex.printStackTrace();
          }
        });
        threads.clear();
      }

    }

    // Non-recursive conventional matrix multiplication
    // public Matrix multiply1(Matrix[] matrixAChunks, Matrix[] matrixBChunks) {
    // int n = matrixAChunks[0].getM();
    // // int C[][] = new int[n][n];
    // // create a new matrix C
    // Matrix result = new Matrix(n, n);

    // // implement the conventional matrix multiplication
    // if (n < 512) {
    // result = Matrix.dot(matrixAChunks, matrixBChunks);
    // } else {
    // ThreadCreation.multiply(matrixAChunks, matrixBChunks, result);
    // }
    // return result;

    // }

    public void run() {
      try {
        outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        inputStream = new ObjectInputStream(clientSocket.getInputStream());

        Matrix[][] data = (Matrix[][]) inputStream.readObject();
        Matrix[] matrixAChunks = data[0];
        Matrix[] matrixBChunks = data[1];
        LOGGER.info("Received data from " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

        LOGGER.info("Starting computation...");

        // Matrix result = new Matrix(A[0].getM(), A[0].getN());

        Matrix result = new Matrix(matrixAChunks[0].getM(), matrixAChunks[0].getN());
        int n = matrixAChunks[0].getM();

        if (n < 2) { // Let's check if we have 30 chunks in each row and in each column
          LOGGER.info("Calling matrix multiplication without threads. Give a bigger challenge to use threads. :p");
          result = Matrix.dot(matrixAChunks, matrixBChunks);
        } else {
          LOGGER.info("Invoking threaded multiplication...");
          ThreadCreation.multiply(matrixAChunks, matrixBChunks, result);
        }

        result.show("Computed result");

        outputStream.writeObject(result);
        outputStream.flush();

        inputStream.close();
        outputStream.close();
        clientSocket.close();

      } catch (IOException e) {
        LOGGER.info("Connection failed" + e.getMessage());
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        // e.printStackTrace();
      }
    }
  }

  public static void main(String[] args) {
    // Accept the port number from the command line
    int port = Integer.parseInt(args[0]);

    // Print help message if no port number is given
    if (port == 0) {
      LOGGER.info("Usage: java Worker <port>");
      System.exit(0);
    }

    // Start the server
    Worker worker = new Worker();
    worker.start(port);
  }
}