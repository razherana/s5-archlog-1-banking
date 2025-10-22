package mg.razherana.banking.common.utils;

public class ExceptionUtils {
  private ExceptionUtils() {
  }

  public static Throwable root(Throwable throwable) {
    Throwable rootCause = throwable;
    while (rootCause.getCause() != null) {
      rootCause = rootCause.getCause();
    }
    return rootCause;
  }
}
