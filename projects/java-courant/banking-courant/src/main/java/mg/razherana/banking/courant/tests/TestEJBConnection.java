package mg.razherana.banking.courant.tests;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

public class TestEJBConnection {
  public static void main(String[] args) {
    String[] hosts = { "127.0.0.5" };

    for (String host : hosts) {
      System.out.println("Testing connection to: " + host);
      try {
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY,
            "org.apache.openejb.client.RemoteInitialContextFactory");
        props.put(Context.PROVIDER_URL, "http://" + host + ":8080/tomee/ejb");

        Context context = new InitialContext(props);
        context.list(""); // Simple operation to test connection
        context.close();

        System.out.println("SUCCESS: Connected to " + host);
        break;
      } catch (Exception e) {
        e.printStackTrace();
        System.out.println("FAILED: " + host + " - " + e.getMessage());
      }
    }
  }
}