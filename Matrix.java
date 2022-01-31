import java.io.Serializable;
import java.util.Arrays;
import java.util.logging.Logger;

/******************************************************************************
 * Compilation: javac Matrix.java
 * Execution: java Matrix
 *
 * A bare-bones immutable data type for M-by-N matrices.
 *
 * Credit: The code is adapted from
 * https://introcs.cs.princeton.edu/java/95linear/Matrix.java.html
 ******************************************************************************/

final public class Matrix implements Serializable {
  private final int M; // number of rows
  private final int N; // number of columns
  private final double[][] data; // M-by-N array

  // Logger for this class
  private static final Logger LOGGER = Logger.getLogger(Manager.class.getName());

  // create M-by-N matrix of 0's
  public Matrix(int M, int N) {
    this.M = M;
    this.N = N;
    data = new double[M][N];
  }

  public int getM() {
    return M;
  }

  public int getN() {
    return N;
  }

  // create matrix based on 2d array
  public Matrix(double[][] data) {
    M = data.length;
    N = data[0].length;
    this.data = new double[M][N];
    for (int i = 0; i < M; i++)
      for (int j = 0; j < N; j++)
        this.data[i][j] = data[i][j];
  }

  // copy constructor
  private Matrix(Matrix A) {
    this(A.data);
  }

  // create and return a random M-by-N matrix with values between 0 and 1
  public static Matrix random(int M, int N) {
    Matrix A = new Matrix(M, N);
    for (int i = 0; i < M; i++)
      for (int j = 0; j < N; j++)
        A.data[i][j] = Math.random();
    return A;
  }

  // create and return a random M-by-N matrix with values between min and max
  public static Matrix random(int M, int N, double min, double max) {
    Matrix A = new Matrix(M, N);
    for (int i = 0; i < M; i++)
      for (int j = 0; j < N; j++)
        A.data[i][j] = min + (max - min) * Math.random();
    return A;
  }

  // create and return the N-by-N identity matrix
  public static Matrix identity(int N) {
    Matrix I = new Matrix(N, N);
    for (int i = 0; i < N; i++)
      I.data[i][i] = 1;
    return I;
  }

  // swap rows i and j
  private void swap(int i, int j) {
    double[] temp = data[i];
    data[i] = data[j];
    data[j] = temp;
  }

  // create and return the transpose of the invoking matrix
  public Matrix transpose() {
    Matrix A = new Matrix(N, M);
    for (int i = 0; i < M; i++)
      for (int j = 0; j < N; j++)
        A.data[j][i] = this.data[i][j];
    return A;
  }

  // return C = A + B
  public Matrix plus(Matrix B) {
    Matrix A = this;
    if (B.M != A.M || B.N != A.N)
      throw new RuntimeException("Illegal matrix dimensions.");
    Matrix C = new Matrix(M, N);
    for (int i = 0; i < M; i++)
      for (int j = 0; j < N; j++)
        C.data[i][j] = A.data[i][j] + B.data[i][j];
    return C;
  }

  // return C = A - B
  public Matrix minus(Matrix B) {
    Matrix A = this;
    if (B.M != A.M || B.N != A.N)
      throw new RuntimeException("Illegal matrix dimensions.");
    Matrix C = new Matrix(M, N);
    for (int i = 0; i < M; i++)
      for (int j = 0; j < N; j++)
        C.data[i][j] = A.data[i][j] - B.data[i][j];
    return C;
  }

  // does A = B exactly?
  public boolean eq(Matrix B) {
    Matrix A = this;
    if (B.M != A.M || B.N != A.N)
      throw new RuntimeException("Illegal matrix dimensions.");
    for (int i = 0; i < M; i++)
      for (int j = 0; j < N; j++)
        if (A.data[i][j] != B.data[i][j])
          return false;
    return true;
  }

  public Matrix quarterify(int index) {
    // Assuming this is a quarter matrix and M and N are both even or specifically
    // M and N should be equal and should be a power of 2
    int len = this.M / 2;
    Matrix quarter = new Matrix(this.M / 2, this.N / 2);
    for (int i = 0; i < len; i++) {
      for (int j = 0; j < len; j++) {
        switch (index) {
          case 0:
            quarter.data[i][j] = this.data[i][j];
            break;
          case 1:
            quarter.data[i][j] = this.data[i][j + len];
            break;
          case 2:
            quarter.data[i][j] = this.data[i + len][j];
            break;
          case 3:
            quarter.data[i][j] = this.data[i + len][j + len];
            break;
        }
      }
    }
    return quarter;
  }

  /* Function to check if x is power of 2 */
  static boolean isPowerOfTwo(int n) {
    return (int) (Math.ceil((Math.log(n) / Math.log(2)))) == (int) (Math.floor(((Math.log(n) / Math.log(2)))));
  }

  public static Matrix cut(Matrix a, int rows, int cols) {
    Matrix temp = new Matrix(rows, cols);

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        temp.data[i][j] = a.data[i][j];
      }
    }

    return temp;
  }

  // method to split parent matrix into child matrices
  public Matrix split(int childMatrixLength, int fromIndex, int toIndex) {
    Matrix child = new Matrix(childMatrixLength, childMatrixLength);

    for (int i1 = 0, i2 = fromIndex; i1 < childMatrixLength; i1++, i2++)
      for (int j1 = 0, j2 = toIndex; j1 < childMatrixLength; j1++, j2++)
        child.data[i1][j1] = data[i2][j2];

    return child;
  }

  // Use the divide strategy to split the matrix into n parts and return the array
  // of matrices
  public Matrix[] divide(int childMatrixLength) {
    // childMatrixLength should be less than or equal to M throw error
    if (childMatrixLength > M) {
      throw new IllegalArgumentException("childMatrixLength should be less than or equal to M");
    }

    // childMatrixLength should be a power of 2 throw error
    if (!isPowerOfTwo(childMatrixLength)) {
      throw new IllegalArgumentException("childMatrixLength should be a power of 2");
    }

    int len = M / childMatrixLength;

    Matrix[] groups = new Matrix[len * len];
    int multiplier = M / len;

    for (int i = 0; i < len; i++) {
      for (int j = 0; j < len; j++) {
        groups[i * len + j] = split(childMatrixLength, i * multiplier, j * multiplier);
      }
    }

    return groups;
  }

  public void join(Matrix P, int fromIndex, int toIndex) {
    for (int i1 = 0, i2 = fromIndex; i1 < P.getM(); i1++, i2++) {
      for (int j1 = 0, j2 = toIndex; j1 < P.getN(); j1++, j2++) {
        data[i2][j2] = P.data[i1][j1];
      }
    }
  }

  public void joinAll(Matrix[] matrices) {
    int childMatrixLength = matrices[0].getM();
    int len = M / childMatrixLength;
    int multiplier = M / len;

    for (int i = 0; i < len; i++) {
      for (int j = 0; j < len; j++) {
        join(matrices[i * len + j], i * multiplier, j * multiplier);
      }
    }
  }

  // Credit:
  // https://github.com/liangyue268/CPE-593/blob/00b590f46666f7bbf7a72af09dcb57d59f52c2d6/HW6/Strassen.java
  public static Matrix padding(Matrix a) {
    int length = (int) Math.pow(2, Math.ceil(Math.log(Math.max(a.getM(), a.getN())) / Math.log(2)));

    Matrix paddedMatrix = new Matrix(length, length);

    for (int i = 0; i < a.getM(); i++) {
      for (int j = 0; j < a.getN(); j++) {
        paddedMatrix.data[i][j] = a.data[i][j];
      }
      for (int j = a.getN(); j < length; j++) {
        paddedMatrix.data[i][j] = 0;
      }
    }

    for (int i = a.getM(); i < length; i++) {
      for (int j = 0; j < length; j++) {
        paddedMatrix.data[i][j] = 0;
      }
    }
    return paddedMatrix;
  }

  // send it whichever quarter it is and then use it to apply the elements from
  // that matrix.
  public static void resetMatrix(Matrix mtx, Matrix submtx, int index) {

    for (int i = 0; i < submtx.M; i++) {
      for (int j = 0; j < submtx.N; j++) {
        switch (index) {
          case 0:
            mtx.data[i][j] = submtx.data[i][j];
            break;
          case 1:
            mtx.data[i][j + submtx.N] = submtx.data[i][j];
            break;
          case 2:
            mtx.data[i + submtx.M][j] = submtx.data[i][j];
            break;
          case 3:
            mtx.data[i + submtx.M][j + submtx.N] = submtx.data[i][j];
            break;
        }
      }
    }
  }

  public static Matrix strassen(Matrix mtx1, Matrix mtx2) {
    // Assuming that by the time the matrices come here, we have these square
    // matrices: So, M == N
    int len = mtx1.M / 2;
    Matrix resultantMatrix = new Matrix(mtx1.M, mtx1.N);

    if (len == 0) {
      resultantMatrix.data[0][0] = mtx1.data[0][0] * mtx2.data[0][0];
    } else {
      for (int i = 0; i < mtx1.M; i++) {
        for (int j = 0; j < mtx2.N; j++) {
          resultantMatrix.data[i][j] = 0;
        }
      }

      Matrix a1 = mtx1.quarterify(0);
      Matrix a2 = mtx1.quarterify(1);
      Matrix a3 = mtx1.quarterify(2);
      Matrix a4 = mtx1.quarterify(3);
      Matrix b1 = mtx2.quarterify(0);
      Matrix b2 = mtx2.quarterify(1);
      Matrix b3 = mtx2.quarterify(2);
      Matrix b4 = mtx2.quarterify(3);

      Matrix m1 = Matrix.strassen(a1, b2.minus(b4));
      Matrix m2 = Matrix.strassen(a1.plus(a2), b4);
      Matrix m3 = Matrix.strassen(a3.plus(a4), b1);
      Matrix m4 = Matrix.strassen(a4, b3.minus(b1));
      Matrix m5 = Matrix.strassen(a1.plus(a4), b1.plus(b4));
      Matrix m6 = Matrix.strassen(a2.minus(a4), b3.plus(b4));
      Matrix m7 = Matrix.strassen(a1.minus(a3), b1.plus(b2));

      // create the four new quadrants of the resultantMatrix.
      /**
       * Q1 = M5 + M4 - M2 + M6
       * Q2 = M1 + M2
       * Q3 = M3 + M4
       * Q4 = M1 + M5 - M3 - M7
       **/

      Matrix Q1 = ((m5.plus(m4)).minus(m2)).plus(m6);
      Matrix Q2 = m1.plus(m2);
      Matrix Q3 = m3.plus(m4);
      Matrix Q4 = ((m1.plus(m5)).minus(m3)).minus(m7);

      resetMatrix(resultantMatrix, Q1, 0);
      resetMatrix(resultantMatrix, Q2, 1);
      resetMatrix(resultantMatrix, Q3, 2);
      resetMatrix(resultantMatrix, Q4, 3);

    }

    return resultantMatrix;
  }

  // if n is odd,we fill remaining rows and columns with zeros to make it even
  // order matrix
  public Matrix fillZeros(int n) {
    Matrix A = new Matrix(n, n);
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        A.data[i][j] = data[i][j];
      }
    }
    return A;
  }

  // return C = A * B
  public Matrix times(Matrix B) {
    Matrix A = this;
    if (A.N != B.M)
      throw new RuntimeException("Illegal matrix dimensions.");
    Matrix C = new Matrix(A.M, B.N);
    for (int i = 0; i < C.M; i++)
      for (int j = 0; j < C.N; j++)
        for (int k = 0; k < A.N; k++)
          C.data[i][j] += (A.data[i][k] * B.data[k][j]);
    return C;
  }

  public static Matrix dot(Matrix[] A, Matrix[] B) {
    Matrix result = new Matrix(A[0].getM(), A[0].getN());

    for (int i = 0; i < A.length; i++) {
      result = result.plus(A[i].times(B[i]));
    }

    return result;
  }

  // return x = A^-1 b, assuming A is square and has full rank
  public Matrix solve(Matrix rhs) {
    if (M != N || rhs.M != N || rhs.N != 1)
      throw new RuntimeException("Illegal matrix dimensions.");

    // create copies of the data
    Matrix A = new Matrix(this);
    Matrix b = new Matrix(rhs);

    // Gaussian elimination with partial pivoting
    for (int i = 0; i < N; i++) {

      // find pivot row and swap
      int max = i;
      for (int j = i + 1; j < N; j++)
        if (Math.abs(A.data[j][i]) > Math.abs(A.data[max][i]))
          max = j;
      A.swap(i, max);
      b.swap(i, max);

      // singular
      if (A.data[i][i] == 0.0)
        throw new RuntimeException("Matrix is singular.");

      // pivot within b
      for (int j = i + 1; j < N; j++)
        b.data[j][0] -= b.data[i][0] * A.data[j][i] / A.data[i][i];

      // pivot within A
      for (int j = i + 1; j < N; j++) {
        double m = A.data[j][i] / A.data[i][i];
        for (int k = i + 1; k < N; k++) {
          A.data[j][k] -= A.data[i][k] * m;
        }
        A.data[j][i] = 0.0;
      }
    }

    // back substitution
    Matrix x = new Matrix(N, 1);
    for (int j = N - 1; j >= 0; j--) {
      double t = 0.0;
      for (int k = j + 1; k < N; k++)
        t += A.data[j][k] * x.data[k][0];
      x.data[j][0] = (b.data[j][0] - t) / A.data[j][j];
    }
    return x;

  }

  // print matrix to standard output
  public void show() {
    for (int i = 0; i < M; i++) {
      for (int j = 0; j < N; j++)
        System.out.printf("%9.4f ", data[i][j]);
      System.out.println();
    }
  }

  public void show(String msg) {
    System.out.println(msg + ":");
    for (int i = 0; i < M; i++) {
      for (int j = 0; j < N; j++)
        System.out.printf("%9.4f ", data[i][j]);
      System.out.println();
    }
  }

  // test client
  public static void main(String[] args) {
    double[][] d = { { 1, 2, 3, 3 }, { 4, 5, 6, 3 }, { 9, 1, 3, 4 }, { 1, 2, 3, 4 } };
    Matrix D = new Matrix(d);
    Matrix Q = D.quarterify(0);
    // D.show();
    System.out.println();

    // create a 8x8 matrix with random values

    Matrix A = Matrix.random(4, 4);
    Matrix B = Matrix.random(4, 4);

    // int partitionSize = 16;
    int chunkSize = 2;

    Matrix[] matrixAChunks = A.divide(chunkSize);
    Matrix[] matrixBChunks = B.divide(chunkSize);

    for (int i = 0; i < matrixAChunks.length; i++) {
      System.out.println("Matrix A Chunk " + i + ": ");
      matrixAChunks[i].show();
      System.out.println();
    }
    // [1, 2, 3, 4]
    // [[1, 2]
    // [3, 4]]

    for (int i = 0; i < matrixBChunks.length; i++) {
      System.out.println("Matrix B Chunk " + i + ": ");
      matrixBChunks[i].show();
      System.out.println();
    }
    // [1, 2, 3, 4]
    // [[1, 2]
    // [3, 4]]

    // 2x2
    // [
    // [first row of A, first col of B],
    // [first row of A, second col of B],
    // [second row of A, first col of B],
    // [second row of A, second col of B]
    // ]
    Matrix[][][] matrixChunks = new Matrix[matrixAChunks.length][2][matrixAChunks[0].getM()];
    Matrix[][][] matrixChunks1 = new Matrix[matrixAChunks.length][2][matrixAChunks[0].getM()];

    Matrix AAA = new Matrix(8, 8);
    Matrix[] As = {
        Matrix.random(2, 2),
        Matrix.random(2, 2),
        Matrix.random(2, 2),
        Matrix.random(2, 2),
        Matrix.random(2, 2),
        Matrix.random(2, 2),
        Matrix.random(2, 2),
        Matrix.random(2, 2),
        Matrix.random(2, 2),
        Matrix.random(2, 2),
        Matrix.random(2, 2),
        Matrix.random(2, 2),
        Matrix.random(2, 2),
        Matrix.random(2, 2),
        Matrix.random(2, 2),
        Matrix.random(2, 2),
    };
    AAA.joinAll(As);
    AAA.show();

    Matrix testPadding = Matrix.random(8, 8);
    Matrix.padding(testPadding).show("Test padding:");

    // System.out.println(Helper.convertToIndex(0, 0, chunkSize));
    // System.out.println(Helper.convertToIndex(0, 1, chunkSize));

    // System.out.println(Helper.convertToIndex(0, 0, chunkSize));
    // System.out.println(Helper.convertToIndex(1, 0, chunkSize));

    // System.out.println(Helper.convertToIndex(0, 0, chunkSize));
    // System.out.println(Helper.convertToIndex(0, 1, chunkSize));

    // System.out.println(Helper.convertToIndex(0, 1, chunkSize));
    // System.out.println(Helper.convertToIndex(1, 1, chunkSize));

    // System.out.println(Helper.convertToIndex(1, 0, chunkSize));
    // System.out.println(Helper.convertToIndex(1, 1, chunkSize));

    // System.out.println(Helper.convertToIndex(0, 0, chunkSize));
    // System.out.println(Helper.convertToIndex(1, 0, chunkSize));

    // System.out.println(Helper.convertToIndex(1, 0, chunkSize));
    // System.out.println(Helper.convertToIndex(1, 1, chunkSize));

    // System.out.println(Helper.convertToIndex(1, 1, chunkSize));
    // System.out.println(Helper.convertToIndex(1, 1, chunkSize));

    /*
     *
     * [
     * ...task for workers,
     * [
     * [A0, A1, A2] ...Matrix A (row)
     * [B0, B2, B3] ...Matrix B (col)
     * ]
     * ]
     *
     */
    /*
     * matrixChunks1[0][0][0] = matrixAChunks[Helper.convertToIndex(0, 0,
     * chunkSize)];
     * //dot
     * matrixChunks1[0][1][0] = matrixBChunks[Helper.convertToIndex(0, 0,
     * chunkSize)];
     *
     * //plus
     *
     * matrixChunks1[0][0][1] = matrixAChunks[Helper.convertToIndex(0, 1,
     * chunkSize)];
     * //dot
     * matrixChunks1[0][1][1] = matrixBChunks[Helper.convertToIndex(1, 0,
     * chunkSize)];
     *
     *
     *
     *
     * matrixChunks1[1][0][0] = matrixAChunks[Helper.convertToIndex(0, 0,
     * chunkSize)];
     * //dot
     * matrixChunks1[1][1][0] = matrixBChunks[Helper.convertToIndex(0, 1,
     * chunkSize)];
     *
     * //plus
     *
     * matrixChunks1[1][0][1] = matrixAChunks[Helper.convertToIndex(0, 1,
     * chunkSize)];
     * //dot
     * matrixChunks1[1][1][1] = matrixBChunks[Helper.convertToIndex(1, 1,
     * chunkSize)];
     *
     *
     *
     *
     * matrixChunks1[2][0][0] = matrixAChunks[Helper.convertToIndex(1, 0,
     * chunkSize)];
     * //dot
     * matrixChunks1[2][1][0] = matrixBChunks[Helper.convertToIndex(0, 0,
     * chunkSize)];
     *
     * //plus
     *
     * matrixChunks1[2][0][1] = matrixAChunks[Helper.convertToIndex(1, 1,
     * chunkSize)];
     * //dot
     * matrixChunks1[2][1][1] = matrixBChunks[Helper.convertToIndex(1, 0,
     * chunkSize)];
     *
     *
     *
     *
     * matrixChunks1[3][0][0] = matrixAChunks[Helper.convertToIndex(1, 0,
     * chunkSize)];
     * //dot
     * matrixChunks1[3][1][0] = matrixBChunks[Helper.convertToIndex(0, 1,
     * chunkSize)];
     *
     * //plus
     *
     * matrixChunks1[3][0][1] = matrixAChunks[Helper.convertToIndex(1, 1,
     * chunkSize)];
     * //dot
     * matrixChunks1[3][1][1] = matrixBChunks[Helper.convertToIndex(1, 1,
     * chunkSize)];
     */

    // A 4x4
    // matrixAChunks [2x2, 2x2, 2x2, 2x2]
    // int numChunks = A.M * A.N / chunkSize^2
    // int numChunks = matrixAChunks.length;
    // int len = matrixAChunks[0].getM();
    /*
     * For each task you've to send a row of first matrix and a column of second
     * matrix
     * Suppose each matrix is 4x4
     * For example, if you've two matrices with 16 chunks each, then there must be
     * 16 workers.
     * Now, for each worker, send a row from first matrix and a column from second
     * matrix.
     * Worker 0: MatA.row(0), MatB.col(0)
     * Worker 1: MatA.row(0), MatB.col(1)
     * Worker 2: MatA.row(0), MatB.col(2)
     * Worker 3: MatA.row(0), MatB.col(3)
     * Worker 4: MatA.row(1), MatB.col(0)
     * Worker 5: MatA.row(1), MatB.col(1)
     * Worker 6: MatA.row(1), MatB.col(2)
     * Worker 7: MatA.row(1), MatB.col(3)
     */
    /*
     * Worker num =: w
     * w % x (e.g: 5%4 = 1 column of matB)
     * w / x (e.g: 5/4 = 1 row of matA - integer division)
     * x == dim(A)or dim(B)
     *
     */
    // for (int w = 0; w < numChunks; w++) {
    // int x = A.getM();
    // int columnB = w % x;
    // int rowA = w / x;
    // System.out.println(rowA + ", " + columnB);
    // // for (int j = 0; j < 2; j++) {
    // for (int k = 0; k < chunkSize; k++) {
    // System.out.println("Printing matrixChunksA[" + w + "][0][" + k + "]");
    // System.out.println("Printing matrixChunksB[" + w + "][1][" + k + "]");
    // // matrixChunks[w][0][k] = matrixChunks[Helper.convertToIndex(rowA, columnB,
    // chunkSize)];
    // }
    // // }
    // }

    /*
     * for (int w = 0; w < numChunks; w++) {
     * int x = A.getM();
     * int columnB = w % x;
     * int rowA = w / x;
     * }
     */
    /*
     * [
     * Matrix A Chunk 0:
     * 0.0889 0.7697
     * 0.5881 0.9030
     *
     * Matrix A Chunk 1:
     * 0.8547 0.3488
     * 0.7491 0.9750
     *
     * Matrix A Chunk 2:
     * 0.6128 0.2170
     * 0.6823 0.3417
     *
     * Matrix A Chunk 3:
     * 0.8508 0.1106
     * 0.0828 0.8620
     * ]
     */
    /*
     *
     * int rows = (int) Math.sqrt(matrixAChunks.length); // [1, 2, 3, 4]
     * int cols = matrixAChunks[0].getM();
     *
     * System.out.println("-------------------");
     * matrixAChunks[Helper.convertToIndex(0, 1, chunkSize)].show();
     * System.out.println("-------------------");
     *
     * // Printing matrixChunksA[0][0]
     * // Printing matrixChunksB[0][0]
     * // Printing matrixChunksA[0][1]
     * // Printing matrixChunksB[1][0]
     *
     * // Printing matrixChunksA[0][0]
     * // Printing matrixChunksB[0][1]
     * // Printing matrixChunksA[0][1]
     * // Printing matrixChunksB[1][1]
     *
     * // Printing matrixChunksA[1][0]
     * // Printing matrixChunksB[0][0]
     * // Printing matrixChunksA[1][1]
     * // Printing matrixChunksB[1][0]
     *
     * // Printing matrixChunksA[1][0]
     * // Printing matrixChunksB[0][1]
     * // Printing matrixChunksA[1][1]
     * // Printing matrixChunksB[1][1]
     *
     * for (int k = 0; k < matrixAChunks.length; k++) {
     * for (int i = 0; i < rows; i++) {
     * for (int j = 0; j < cols; j++) {
     * // for (int k = 0; k < cols; k++) {
     * // matrixChunks[Helper.convertToIndex(i, k, chunkSize)][0][j] =
     * matrixAChunks[Helper.convertToIndex(i, k, chunkSize)];
     * // matrixChunks[Helper.convertToIndex(k, j, chunkSize)][1][j] =
     * matrixBChunks[Helper.convertToIndex(k, j, chunkSize)];
     *
     * System.out.println("Printing " + k + " matrixChunksA[" + i + "][" + j + "]");
     * System.out.println("Printing " + k + " matrixChunksB[" + j + "][" + i + "]");
     *
     * // System.out.println(Helper.convertToIndex(j, k, chunkSize) + ", " + j +
     * ", " + k);
     * // matrixAChunks[Helper.convertToIndex(j, k, chunkSize)].show();
     * // System.out.println();
     * // }
     * }
     * System.out.println();
     * }
     * }
     */

    // matrixChunks1[0][0][0].show();
    // matrixChunks[0][0][0].show();

    // matrixChunks1[0][0][1].show();
    // matrixChunks[0][0][1].show();

    ////////////////////////////////////////////////
    // Assuming A and B are 8*8
    // int CHUNK_SIZE = 8;
    // int elements_in_chunk = CHUNK_SIZE*CHUNK_SIZE;
    // int NUM_WORKERS = (16*16) / (elements_in_chunk); //8*8 = num elements in each
    //////////////////////////////////////////////// matrix
    // int GAME_CHANGER = (int) Math.sqrt(NUM_WORKERS);

    // System.out.println("<--------------------FOCUS HERE--------------->");

    // for (int i = 0; i < NUM_WORKERS; i++) {
    // System.out.println("Worker# " + i);
    // int startA = (i / GAME_CHANGER) * GAME_CHANGER;
    // int endA = startA + GAME_CHANGER;
    // System.out.println("We will feed these chunk numbers of A to this worker:");
    // for (int x = startA; x < endA; x++) {
    // System.out.print(x + " ");
    // }
    // System.out.println();
    // System.out.println("We will feed these chunk numbers of B to this worker:");
    // int startB = (i % GAME_CHANGER);
    // int numElementsInB = 0;
    // for (int x = startB;; x += GAME_CHANGER) {
    // System.out.print(x + " ");
    // numElementsInB++;
    // if (numElementsInB == GAME_CHANGER) {
    // break;
    // }
    // }
    // System.out.println();

    // }
    // System.out.println("<--------------------FOCUS HERE--------------->");

    // Matrix[][][] my = arrangeTasks(matrixAChunks, matrixBChunks, A.getM());
    // my[0][0][0].show();
    // System.out.println("-------------------");
    // my[0][0][1].show();
    // System.out.println("-------------------");
    // my[0][1][0].show();
    // System.out.println("-------------------");
    // my[0][1][1].show();
    // System.out.println("-------------------");

    ///////////////////////////////////////////////

    // matrixChunks1[0][0][1].show();
    // matrixChunks[0][0][1].show();

    // Dot product store in list of matrixAChunks and matrixBChunks
    Matrix[] matrixDotProduct = new Matrix[matrixAChunks.length];

    // System.out.println("Quarter 1");
    // Q.show();
    // System.out.println("-----------------\n");

    // Q = D.quarterify(1);
    // System.out.println("Quarter 2");
    // Q.show();
    // System.out.println("-----------------\n");

    // Q = D.quarterify(2);
    // System.out.println("Quarter 3");
    // Q.show();
    // System.out.println("-----------------\n");

    // Q = D.quarterify(3);
    // System.out.println("Quarter 4");
    // Q.show();
    // System.out.println("-----------------\n");

    // Matrix d_square = Matrix.strassen(D, D);
    // d_square.show();
    // System.out.println();
    // ------------------------

    // double[][] e = { { 1, 2, 3 }, { 4, 5, 6 }, { 9, 1, 3 } };
    // Matrix E = new Matrix(e);
    // Matrix newMat = E.fillZeros()

    // Matrix A = Matrix.random(5, 5);
    // A.show();
    // System.out.println();

    // A.swap(1, 2);
    // A.show();
    // System.out.println();

    // Matrix B = A.transpose();
    // B.show();
    // System.out.println();

    // Matrix C = Matrix.identity(5);
    // C.show();
    // System.out.println();

    // A.plus(B).show();
    // System.out.println();

    // B.times(A).show();
    // System.out.println();

    // // shouldn't be equal since AB != BA in general
    // System.out.println(A.times(B).eq(B.times(A)));
    // System.out.println();

    // Matrix b = Matrix.random(5, 1);
    // b.show();
    // System.out.println();

    // Matrix x = A.solve(b);
    // x.show();
    // System.out.println();

    // A.times(x).show();

  }
}