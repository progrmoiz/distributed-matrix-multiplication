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

final public class Matrix {
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

 /*  public Matrix quarterify(int index) {
    // Divide the matrix into 4 quarters
    Matrix quarter = new Matrix(M / 2, N / 2);
    int len = M / 2;
    switch (index) {
      case 0:
        // Quarter 1
        for (int i = 0; i < M / 2; i++) {
          for (int j = 0; j < N / 2; j++) {
            quarter.data[i][j] = data[i][j];
          }
        }
        break;
      case 1:
        // Quarter 2
        for (int i = 0; i < M / 2; i++) {
          for (int j = N / 2; j < N; j++) {
            quarter.data[i][j] = data[i][j + len];
          }
        }
        break;
      case 2:
        // Quarter 3
        for (int i = M / 2; i < M; i++) {
          for (int j = 0; j < N / 2; j++) {
            quarter.data[i][j] = data[i + len][j];
          }
        }
        break;
      case 3:
        // Quarter 4
        for (int i = M / 2; i < M; i++) {
          for (int j = N / 2; j < N; j++) {
            quarter.data[i][j] = data[i + len][j + len];
          }
        }
        break;
    }

    return quarter;
  }
 */
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

  // test client
  public static void main(String[] args) {
    double[][] d = { { 1, 2, 3, 3 }, { 4, 5, 6, 3 }, { 9, 1, 3, 4 }, { 1, 2, 3, 4 } };
    Matrix D = new Matrix(d);
    Matrix Q = D.quarterify(0);
    D.show();
    System.out.println();

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

    Matrix d_square = Matrix.strassen(D, D);
    d_square.show();
    System.out.println();
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