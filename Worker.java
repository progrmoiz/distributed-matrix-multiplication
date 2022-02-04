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
  /**
   * Accept a client connection and create a new thread for it
   *
   * @param port The port number to which the server socket is bound.
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

  /**
   * It creates a server socket and listens for incoming connections
   */
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
      private Matrix result;
      private int row;

      // The RowMultiply class is a subclass of Thread.
      // It has three instance variables: result, mat1, and mat2.
      // It has one constructor that takes four parameters: result, mat1, mat2, and
      // row.
      // The constructor initializes the three instance variables to the values of the
      // four parameters.
      public RowMultiply(Matrix result, Matrix mat1, Matrix mat2, int row) {
        this.result = result;
        this.mat1 = mat1;
        this.mat2 = mat2;
        this.row = row;
      }

      // For each row in mat1, multiply that row by mat2 and store the result in
      // result.
      @Override
      public void run() {
        for (int i = 0; i < mat2.getN(); i++) {
          // result[row][i] = 0;
          result.set(row, i, 0);
          for (int j = 0; j < mat1.getN(); j++) {
            // result[row][i] += mat1[row][j] * mat2[j][i];
            result.set(row, i, result.get(row, i) + mat1.get(row, j) * mat2.get(j, i));
          }
        }
      }
    }

    public static class DotProduct implements Runnable {
      private Matrix mat1;
      private Matrix mat2;
      private Matrix finalResult;
      private int threshold;
      private int bigThreshold;

      // The constructor takes in the final result matrix, the two matrices that will
      // be multiplied,
      // and a threshold value.
      public DotProduct(Matrix finalResult, Matrix mat1, Matrix mat2) {
        this.finalResult = finalResult;
        this.mat1 = mat1;
        this.mat2 = mat2;
        this.threshold = 100;
        this.bigThreshold = 2000;

      }

      // The `RowMultiply` class is a thread that multiplies a row of `mat1` by a row
      // of `mat2` and adds
      // it to `finalResult`.
      //
      // The `run` method of the `RowMultiply` class creates a new thread and starts
      // it.
      //
      // The `run` method also adds the thread to a list of threads.
      //
      // The `run` method checks if the number of threads is greater than 50.
      //
      // If it is, it waits for the threads to finish.
      @Override
      public void run() {
        List<Thread> threads = new ArrayList<>();
        Matrix temp = new Matrix(mat1.getM(), mat1.getN());

        // this.result.set(i, j, value);
        // multiply mat1 by mat2 and add it to finalResult
        // System.out.println("Inside thread run and multipying:");
        // this.mat1.show();
        // this.mat2.show();
        // if mat1 size is actually BIG, then don't multiply traditionally - make some
        // genius move
        // A genius move could be: making more chunks out of them and recursively
        // if (mat1.getM() > bigThreshold) {
        // Chunking
        // Matrix[] matrixAChunks = mat1.divide(Math.pow(4, ));
        // Matrix[] matrixBChunks = mat2.divide(chunkSize);

        int rows = mat1.getM();

        for (int i = 0; i < rows; i++) {
          RowMultiply task = new RowMultiply(temp, mat1, mat2, i);
          Thread thread = new Thread(task);
          thread.start();
          threads.add(thread);
          if (threads.size() % 50 == 0) {
            // if (threads.size() > rows*0.25) { //Wait if 25% rows are already in process
            Helper.waitForThreads(threads);
          }
        }
        Helper.waitForThreads(threads);
        finalResult.plusInPlace(temp);
        // } else {
        // finalResult.plusInPlace(mat1.times(mat2));
        // }
      }
    }

    public static class ThreadCreation {

      /**
       * Given two matrices, create a thread for each matrix and run the dot product
       * on each thread
       *
       * @param mat1        The first matrix to be multiplied.
       * @param mat2        The matrix that is being multiplied by mat1.
       * @param finalResult the result matrix
       */
      public static void multiply(Matrix[] mat1, Matrix[] mat2, Matrix finalResult) {
        List<Thread> threads = new ArrayList<>();

        int numChunks = mat1.length;

        for (int i = 0; i < numChunks; i++) {
          DotProduct task = new DotProduct(finalResult, mat1[i], mat2[i]);
          Thread thread = new Thread(task);
          thread.start();
          threads.add(thread);
          if (threads.size() % 50 == 0) {
            Helper.waitForThreads(threads);
          }
        }
        Helper.waitForThreads(threads);
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

        // Read the input stream into a 2D array of matrices.
        Matrix[][] data = (Matrix[][]) inputStream.readObject();
        Matrix[] matrixAChunks = data[0];
        Matrix[] matrixBChunks = data[1];
        LOGGER.info("Received data from " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

        LOGGER.info("Starting computation...");

        // Matrix result = new Matrix(A[0].getM(), A[0].getN());

        Matrix result = new Matrix(matrixAChunks[0].getM(), matrixAChunks[0].getN());
        int rowsInChunk = matrixAChunks[0].getM();

        if (rowsInChunk < 2) { // Let's check if we have 30 chunks in each row and in each column
          LOGGER.info("Calling matrix multiplication without threads. Give a bigger challenge to use threads. :p");
          // Doing matrix multiplication.
          result = Matrix.dot(matrixAChunks, matrixBChunks);
        } else {
          LOGGER.info("Invoking threaded multiplication...");
          // The code is creating threads to multiply matrices.
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