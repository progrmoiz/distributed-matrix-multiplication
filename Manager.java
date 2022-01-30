import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

// servers
// localhost:8080
// localhost:8081

// chunks
// Job 1 [0, 1, 2, 3, 4]
// Job 2 [5, 6, 7, 8, 9]
// Job 3 [10, 11, 12, 13, 14]
// Job 4 [15, 16, 17, 18, 19]

// Create jobs from chunks (jobs = chunks)

// Enqueue all jobs
// Get list of all free server for example if two are free then send job to both (dequeue and execute)

// Private attribute host and port
public class Manager {
  // Server socket for accepting client connections
  private ServerSocket serverSocket;

  // Logger for this class
  private static final Logger LOGGER = Logger.getLogger(Manager.class.getName());

  // List of InetSocketAddress
  // Host address is string and port number is integer
  // Example: "localhost", 1234
  private InetSocketAddress[] workerAddresses = {};

  // Key value pair of busy INetSocketAddress and busy boolean
  // Example: {new InetSocketAddress("localhost", 1234), false}
  private static Map<String, Boolean> workerStatus = new HashMap<>();

  private int partitionSize;

  // Add to workerAddresses
  public void addWorker(InetSocketAddress workerAddress) {
    workerAddresses = Arrays.copyOf(workerAddresses, workerAddresses.length + 1);
    workerAddresses[workerAddresses.length - 1] = workerAddress;
  }

  // Manager constructor
  public Manager(int partitionSize) {
    // partitionSize must be a power of 4
    if (partitionSize < 4 || (partitionSize & (partitionSize - 1)) != 0) {
      throw new IllegalArgumentException("partitionSize must be a power of 4");
    }

    this.partitionSize = partitionSize;
  }

  public Manager() {
    this.partitionSize = 4;
  }

  /**
   * Start a server socket and wait for a connection.
   *
   * @param port the port number to listen on
   */
  public void start(int port) {
    try {
      // Check if workerAddresses is empty, terminate immediately
      if (workerAddresses.length == 0) {
        LOGGER.info("No worker available");
        return;
      }

      for (InetSocketAddress workerAddress : workerAddresses) {
        workerStatus.put(Helper.inetSocketAddressToString(workerAddress), false);
      }

      // Create a server socket
      serverSocket = new ServerSocket(port);
      // Print the IP address and port number
      LOGGER.info("Server started on " + serverSocket.getInetAddress() + ":" + serverSocket.getLocalPort());

      // Print listening message
      LOGGER.info("Listening for connections...");

      // Wait for a client to connect
      while (true) {
        // Accept client connection and create a new thread for it
        new ManagerClientHandler(serverSocket.accept()).start();
      }

    } catch (IOException e) {
      LOGGER.severe("Connection failed");
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
      e.printStackTrace();
    }
  }


  /**
   * This is our manager job handler.
   * 1. Send a input to the worker
   * 2. Let the worker handle the job
   * 3. Receive a output from the worker
   *
   * @param Socket    clientSocket - The socket to the worker
   * @param Matrix[][] chunk - The data to send to the worker
   *
   * @return Matrix - The data received from the worker
   */
  public static Matrix job(Socket clientSocket, Matrix[][] chunk) throws IOException, ClassNotFoundException {
    // Write the chunk to the socket
    ObjectOutputStream oos2 = new ObjectOutputStream(clientSocket.getOutputStream());
    oos2.writeObject(chunk);
    oos2.flush();

    // Read the result from the socket
    ObjectInputStream ois2 = new ObjectInputStream(clientSocket.getInputStream());
    Matrix result = (Matrix) ois2.readObject();

    // Close the socket
    ois2.close();
    oos2.close();
    // clientSocket.close();

    return result;
  }


  // Get all free servers
  private InetSocketAddress[] getFreeWorkers() {
    // Return empty array if no servers are free
    if (workerStatus.isEmpty()) {
      return new InetSocketAddress[0];
    }

    return Arrays.stream(workerAddresses)
        .filter((InetSocketAddress workerAddress) -> !workerStatus.get(Helper.inetSocketAddressToString(workerAddress)))
        .toArray(InetSocketAddress[]::new);
  }

  /**
   * The following method arrangeTasks takes chunks of both matrices A and B,
   * and the dimension of any of the matrix (since both are equal). Then it creates
   * number of workers by dividing number of elements in a matrix with number of
   * elements in each chunk. Then for each worker, it takes all the chunks from a
   * specific row of  A and from a specific column of B. Those specific column and
   * row are selected based on the ordering of workers as follows:
   * [
   *   worker#1, worker#2, worker#3, worker#4,
   *   worker#5, worker#6, worker#7, worker#8,
   *   worker#9, worker#10, worker#11, worker#12,
   *   worker#13, worker#14, worker#15, worker16,
   * ]
   * So, the row from A must be the row number in which the specific worker number lies
   * and the column from B must be column number in which the specific worker number
   * lies.
   *
   * After selecting chunks from A and B, the function stores them in an array for
   * each worker. It stores all such arrays, representing the task the workers, in an
   * array with the length equal to the number of workers.
   */
  public static Matrix[][][] arrangeTasks(Matrix[] aChunks, Matrix[] bChunks, int dimensionOfMatrix) {
    int chunkSize = aChunks[0].getM(); //keep in mind that this is dimension of chunk
    int elementsInChunks = (int) Math.pow(chunkSize, 2);
    int numWorkers = (int) Math.pow(dimensionOfMatrix, 2) / elementsInChunks; //8*8 = num elements in each matrix
    int gameChanger = (int) Math.sqrt(numWorkers);

    Matrix[][][] resultMatrices = new Matrix[numWorkers][2][gameChanger];

    for (int i = 0; i < numWorkers; i++) {
      // System.out.println("Worker# " + i);
      int startA = (i / gameChanger) * gameChanger;
      int endA = startA + gameChanger;
      // System.out.println("We will feed these chunk numbers of A to this worker:");
      Matrix[] aChunksToWorker = new Matrix[gameChanger];

      int aChunkCounter = 0;
      for (int x = startA; x < endA; x++) {
        // System.out.print(x + " ");
        aChunksToWorker[aChunkCounter++] = aChunks[x];
      }
      // System.out.println();
      // System.out.println("We will feed these chunk numbers of B to this worker:");
      int startB = (i % gameChanger);
      int numElementsInB = 0;

      Matrix[] bChunksToWorker = new Matrix[gameChanger];

      int bChunkCounter = 0;
      for (int x = startB;; x += gameChanger) {
        // System.out.print(x + " ");
        bChunksToWorker[bChunkCounter++] = bChunks[x];

        numElementsInB++;
        if (numElementsInB == gameChanger) {
          break;
        }
      }
      Matrix[][] temp = { aChunksToWorker, bChunksToWorker };
      resultMatrices[i] = temp;

      // System.out.println();
    }

    return resultMatrices;
  }

  private class ManagerClientHandler extends Thread {
    private Socket clientSocket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    public ManagerClientHandler(Socket clientSocket) {
      this.clientSocket = clientSocket;
    }

    public void run() {
      try {
        outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        inputStream = new ObjectInputStream(clientSocket.getInputStream());

        Matrix[] data = (Matrix[]) inputStream.readObject();

        Matrix matrixA = data[0];
        Matrix matrixB = data[1];

        LOGGER.info("Received matrices from client: ");
        matrixA.show("A");
        matrixB.show("B");

        // Divide the integers array into chunks of size n
        int chunkSize = (int) Math.sqrt(Math.pow(matrixA.getM(), 2) / partitionSize);

        // MatrixAChunks[
        //   Matrix[
        //    1  2 <- ChunkSize = 2
        //    3  4
        //   ],
        //   Matrix[
        //    5  6
        //    7  8
        //   ]
        // ]
        Matrix[] matrixAChunks = matrixA.divide(chunkSize);
        Matrix[] matrixBChunks = matrixB.divide(chunkSize);

        Matrix[][][] chunks = arrangeTasks(matrixAChunks, matrixBChunks, matrixA.getM());

        // Create a copy of the chunks
        Matrix[] resultChunks = new Matrix[chunks.length];

        // We will keep track of our threads so later we can pause execution
        // to wait for all threads to finish
        List<Thread> threads = new ArrayList<>();

        // Example
        // Iterate over the chunks (4 chunks)
        // Get the free servers (2 free servers)
        // Create a new thread for each chunk depending on the free servers
        // - 2 chunks will be sent to 2 servers

        // send the chunks to servers clients in threads
        try {
          int chunkIndex = 0;

          while (true) { // 4 chunks
            InetSocketAddress[] workerAddresses = getFreeWorkers();

            // log all free servers
            LOGGER.info("Free servers: " + Arrays.toString(workerAddresses));

            if (workerAddresses.length == 0) {
              LOGGER.info("No free servers at the moment. Waiting for free servers...");
              Thread.sleep(1000);
              continue;
            }

            // calculate the remaining chunks
            int remainingChunks = chunks.length - chunkIndex;

            // If remaining chunks is less than the number of free servers,
            // assign the remaining chunks to the free servers
            if (workerAddresses.length > remainingChunks) {
              workerAddresses = Arrays.copyOfRange(workerAddresses, 0, remainingChunks);
            }

            // Iterate over the free servers
            for (InetSocketAddress workerAddress : workerAddresses) { // 1st server
              // Fix: Local variable chunkIndex defined in an enclosing scope must be final or
              // effectively final
              final int chunkIndexFinal = chunkIndex;

              // Set the server status to busy
              workerStatus.put(Helper.inetSocketAddressToString(workerAddress), true);

              Thread thread = new Thread(() -> {
                try {
                  // Log chunk index
                  LOGGER.info("Sending chunk " + chunkIndexFinal + " to worker " + Helper.inetSocketAddressToString(workerAddress));

                  // Create a new socket
                  Socket workerClientSocket = new Socket(workerAddress.getHostName(), workerAddress.getPort());
                  LOGGER.info("Connected to " + Helper.inetSocketAddressToString(workerAddress));

                   // Send the chunk to the server
                  Matrix result = job(workerClientSocket, chunks[chunkIndexFinal]);

                  // Merge the result chunks
                  resultChunks[chunkIndexFinal] = result;

                  // Close the socket
                  workerClientSocket.close();

                  // Free the server
                  LOGGER.info("Freeing worker: " + Helper.inetSocketAddressToString(workerAddress));
                  workerStatus.put(Helper.inetSocketAddressToString(workerAddress), false);

                  // Print hashmap workerStatus
                  // LOGGER.info("workerStatus: " + workerStatus.toString());
                } catch (IOException e) {
                  e.printStackTrace();
                } catch (ClassNotFoundException e) {
                  e.printStackTrace();
                }
              });
              threads.add(thread);
              thread.start();

              chunkIndex++; // 4th chunk

            }

            if (chunkIndex == chunks.length) {
              break;
            }

          }

        } catch (Exception e) {
          e.printStackTrace();
        }

        // join all the threads
        try {
          for (Thread thread : threads) {
            thread.join();
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        // Merge the results from the workers
        LOGGER.info("Merging results...");
        Matrix merged = new Matrix(matrixA.getM(), matrixA.getN());

        // TODO: If filled with zeros, remove extra zeros from the output
        merged.joinAll(resultChunks);
        merged.show();

        // Send the merged result to the client
        outputStream.writeObject(merged);
        outputStream.flush();

        inputStream.close();
        outputStream.close();
        clientSocket.close();

      } catch (IOException e) {
        LOGGER.severe("Error while handling client: " + e.getMessage());
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    }
  }

  public static void main(String[] args) {
    Manager manager = new Manager((int) Math.pow(4, 1));
    manager.addWorker(new InetSocketAddress("localhost", 9001));
    manager.addWorker(new InetSocketAddress("localhost", 9002));
    // manager.addWorker(new InetSocketAddress("localhost", 9003));
    manager.start(6666);
  }
}