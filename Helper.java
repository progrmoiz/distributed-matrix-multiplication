import java.net.InetSocketAddress;
import java.awt.Point;

public class Helper {
  /**
   * Convert InetSocketAddress to String. We implement this function because
   * InetSocketAddress.toString() is not giving the unifiying format every time.
   *
   * @param inetSocketAddress
   * @return
   */
  public static String inetSocketAddressToString(InetSocketAddress inetSocketAddress) {
    return inetSocketAddress.getHostString() + ":" + inetSocketAddress.getPort();
  }

  // Method to convert single dimensional array index to Point x, y
  public static Point convertToXY(int i, int n) {
    int x = i % n;
    int y = i / n;

    return new Point(x, y);
  }

  // Method to convert Point x, y to single dimensional array index
  public static int convertToIndex(int x, int y, int n) {
    return y * n + x;
  }
}
