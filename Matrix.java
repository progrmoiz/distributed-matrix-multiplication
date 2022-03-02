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

/**
 * Creating a class called Matrix.
 */
final public class Matrix implements Serializable {
  private final int M; // number of rows
  private final int N; // number of columns
  private final double[][] data; // M-by-N array

  // Logger for this class
  private static final Logger LOGGER = Logger.getLogger(Matrix.class.getName());

  // create M-by-N matrix of 0's
  // Creating a new Matrix object with M rows and N columns.
  public Matrix(int M, int N) {
    this.M = M;
    this.N = N;
    data = new double[M][N];
  }

  /**
   * Returns the number of rows in the matrix
   *
   * @return The value of the instance variable M.
   */
  public int getM() {
    return M;
  }

  /**
   * Returns the number of elements in the array
   *
   * @return The number of elements in the array.
   */
  public int getN() {
    return N;
  }

  /**
   * Given an integer i and an integer j, set the value of the element at the i-th
   * row and j-th column
   * of the matrix to value
   *
   * @param i     The row of the matrix.
   * @param j     The column index of the element to be set.
   * @param value the value to set the cell to
   */
  public void set(int i, int j, double value) {
    data[i][j] = value;
  }

  /**
   * Given an integer i and an integer j, return the value of the element at the
   * ith row and jth column
   * of the matrix
   *
   * @param i The row of the matrix.
   * @param j the column index
   * @return The value of the element at the specified indices.
   */
  public double get(int i, int j) {
    return data[i][j];
  }

  /**
   * Given an index, return the row of the matrix
   *
   * @param i The row index.
   * @return The row of data.
   */
  public double[] getRow(int i) {
    return data[i];
  }

  /**
   * Return a new array that contains the elements of the jth column of the matrix
   *
   * @param j the column index
   * @return A new array of doubles.
   */
  public double[] getColumn(int j) {
    double[] column = new double[M];
    for (int i = 0; i < M; i++) {
      column[i] = data[i][j];
    }
    return column;
  }

  // The constructor takes a double[][] as an argument and creates a new
  // double[][] that is a copy of the argument.
  public Matrix(double[][] data) {
    M = data.length;
    N = data[0].length;
    this.data = new double[M][N];
    for (int i = 0; i < M; i++)
      for (int j = 0; j < N; j++)
        this.data[i][j] = data[i][j];
  }

  // Creating a new Matrix object with the same data as the Matrix A.
  private Matrix(Matrix A) {
    this(A.data);
  }

  // create and return a random M-by-N matrix with values between 0 and 1
  /**
   * Generate a random matrix of size M by N
   *
   * @param M the number of rows in the matrix
   * @param N the number of rows in the matrix
   * @return A new Matrix object.
   */
  public static Matrix random(int M, int N) {
    Matrix A = new Matrix(M, N);
    for (int i = 0; i < M; i++)
      for (int j = 0; j < N; j++)
        A.data[i][j] = Math.random();
    return A;
  }

  /**
   * Generate a random matrix with elements in the range [min, max]
   *
   * @param M   the number of rows in the matrix
   * @param N   the number of rows in the matrix
   * @param min the minimum value of the random numbers
   * @param max The maximum value of the random numbers.
   * @return A new matrix object.
   */
  public static Matrix random(int M, int N, double min, double max) {
    Matrix A = new Matrix(M, N);
    for (int i = 0; i < M; i++)
      for (int j = 0; j < N; j++)
        A.data[i][j] = min + (max - min) * Math.random();
    return A;
  }

  /**
   * Create a NxN matrix with the diagonal elements set to 1
   *
   * @param N the size of the matrix
   * @return A new Matrix object.
   */
  public static Matrix identity(int N) {
    Matrix I = new Matrix(N, N);
    for (int i = 0; i < N; i++)
      I.data[i][i] = 1;
    return I;
  }

  /**
   * Swap the values of the data array at indices i and j
   *
   * @param i The index of the first element to swap.
   * @param j The index of the element to swap with.
   */
  private void swap(int i, int j) {
    double[] temp = data[i];
    data[i] = data[j];
    data[j] = temp;
  }

  /**
   * Transpose() returns a new matrix that is the transpose of this matrix
   *
   * @return A new matrix.
   */
  public Matrix transpose() {
    Matrix A = new Matrix(N, M);
    for (int i = 0; i < M; i++)
      for (int j = 0; j < N; j++)
        A.data[j][i] = this.data[i][j];
    return A;
  }

  /**
   * Add the elements of matrix B to the elements of matrix A
   *
   * @param B the matrix to be added to this matrix
   */
  public void plusInPlace(Matrix B) {
    if (B.M != M || B.N != N)
      throw new RuntimeException("Illegal matrix dimensions.");

    for (int i = 0; i < M; i++)
      for (int j = 0; j < N; j++)
        data[i][j] += +B.data[i][j];
  }

  /**
   * Add two matrices together
   *
   * @param B the matrix to be added to A
   * @return A new matrix C.
   */
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

  /**
   * Subtracts the matrix B from the matrix A and returns the result in matrix C
   *
   * @param B The matrix to subtract from this matrix.
   * @return A new matrix C.
   */
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

  /**
   * If the matrices are equal, return true, otherwise return false
   *
   * @param B the matrix to compare to A
   * @return A boolean value.
   */
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

  /**
   * Given a number, return true if it is a power of two, else return false
   *
   * @param n The number to check if it's a power of two.
   * @return The return value is a boolean value.
   */
  static boolean isPowerOfTwo(int n) {
    return (int) (Math.ceil((Math.log(n) / Math.log(2)))) == (int) (Math.floor(((Math.log(n) / Math.log(2)))));
  }

  /**
   * Given a matrix, cut it into a smaller matrix of a specified size
   *
   * @param a    The matrix to be cut.
   * @param rows The number of rows to cut out of the matrix.
   * @param cols the number of columns to cut the matrix into
   * @return The matrix that is cut from the original matrix.
   */
  public static Matrix cut(Matrix a, int rows, int cols) {
    Matrix temp = new Matrix(rows, cols);

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        temp.data[i][j] = a.data[i][j];
      }
    }

    return temp;
  }

  /**
   * Given a matrix, split it into two matrices
   *
   * @param childMatrixLength the length of the child matrix.
   * @param fromIndex         The index of the first row of the matrix to be
   *                          copied.
   * @param toIndex           The index of the last element in the row of the
   *                          parent matrix that will be copied to
   *                          the child matrix.
   * @return The child matrix.
   */
  public Matrix split(int childMatrixLength, int fromIndex, int toIndex) {
    Matrix child = new Matrix(childMatrixLength, childMatrixLength);

    for (int i1 = 0, i2 = fromIndex; i1 < childMatrixLength; i1++, i2++)
      for (int j1 = 0, j2 = toIndex; j1 < childMatrixLength; j1++, j2++)
        child.data[i1][j1] = data[i2][j2];

    return child;
  }

  /**
   * Divide the matrix into smaller matrices of size childMatrixLength
   *
   * @param childMatrixLength The length of the child matrix.
   * @return An array of matrices.
   */
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

  /**
   * Given a matrix P, join the matrix to the matrix at the given index
   *
   * @param P         the matrix to be joined with this matrix
   * @param fromIndex the index of the first row of the matrix to be joined.
   * @param toIndex   the index of the first element in the new matrix.
   */
  public void join(Matrix P, int fromIndex, int toIndex) {
    for (int i1 = 0, i2 = fromIndex; i1 < P.getM(); i1++, i2++) {
      for (int j1 = 0, j2 = toIndex; j1 < P.getN(); j1++, j2++) {
        data[i2][j2] = P.data[i1][j1];
      }
    }
  }

  /**
   * Join all the matrices in the array into one matrix
   *
   * @param matrices an array of matrices to join
   */
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

  // Given a matrix, pad it with zeros until it is a power of 2.
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

  // For each of the four submatrices, copy the submatrix into the appropriate
  // location in the original matrix.
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

  /**
   * Multiply the matrix A by the matrix B, and store the result in the matrix C
   *
   * @param B the matrix to be multiplied
   * @return The product of A and B.
   */
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

  /**
   * It computes the dot product of A and B.
   *
   * @param A an array of matrices
   * @param B the matrix that is being multiplied
   * @return The result of the matrix multiplication.
   */
  public static Matrix dot(Matrix[] A, Matrix[] B) { // 10 chunks example in A and B so create 10 threads
    Matrix result = new Matrix(A[0].getM(), A[0].getN());

    for (int i = 0; i < A.length; i++) {
      result = result.plus(A[i].times(B[i]));
    }

    return result;
  }

  /**
   * Given a matrix A and a vector b, solve for x in Ax = b
   *
   * @param rhs the right hand side of the equation, a matrix with as many rows as
   *            the
   * @return The solution matrix.
   */
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

  /**
   * Prints the matrix to the standard output
   */
  public void show() {
    for (int i = 0; i < M; i++) {
      for (int j = 0; j < N; j++)
        System.out.printf("%9.4f ", data[i][j]);
      System.out.println();
    }
  }

  /**
   * Prints the matrix to the standard output
   *
   * @param msg The message to display.
   */
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
  }
}