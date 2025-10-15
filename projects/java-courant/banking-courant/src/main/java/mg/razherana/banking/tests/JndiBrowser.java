package mg.razherana.banking.tests;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.Binding;
import java.util.Properties;

public class JndiBrowser {

  public static void main(String[] args) {
    try {
      Properties props = new Properties();
      props.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");
      // **Replace <SERVER_IP> with the actual IP of the other PC**
      props.put(Context.PROVIDER_URL, "http://127.0.0.2:8080/tomee/ejb");

      props.put("openejb.client.connection.force.close", "true");
      props.put("openejb.client.connection.pool.max", "1");
      props.put("openejb.client.connection.idle.timeout", "1000");
      // Force the network connection to avoid local optimization confusion
      props.put("openejb.client.connection.mode", "http");

      Context context = new InitialContext(props);

      System.out.println("--- Listing all contexts ---");
      listContext(context, "");

      System.out.println("--- Listing global context ---");
      listContext(context, "global");
      System.out.println();

      context.lookup("UserServiceRemoteImplRemote");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void listContext(Context context, String contextName) {
    try {
      NamingEnumeration<Binding> list = context.listBindings(contextName);
      while (list != null && list.hasMore()) {
        Binding binding = list.next();
        System.out.println("  Name: " + binding.getName() + " -> Type: " + binding.getClassName());
      }
    } catch (javax.naming.NameNotFoundException e) {
      System.out.println("  Context '" + contextName + "' not found on remote server.");
    } catch (Exception e) {
      System.out.println("  Error listing context: " + e.getMessage());
    }
  }
}