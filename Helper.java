import java.net.InetSocketAddress;

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
}
