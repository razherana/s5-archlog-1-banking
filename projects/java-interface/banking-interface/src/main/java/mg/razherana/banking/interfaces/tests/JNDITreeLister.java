package mg.razherana.banking.interfaces.tests;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Binding;
import java.util.Properties;
import java.util.Enumeration;

public class JNDITreeLister {

  public static void list() {
    new JNDITreeLister().listAllJNDINames();
  }

  public void listAllJNDINames() {
    Context context = null;
    try {
      // TomEE remote connection properties
      Properties props = new Properties();
      props.put(Context.INITIAL_CONTEXT_FACTORY,
          "org.apache.openejb.client.RemoteInitialContextFactory");
      props.put(Context.PROVIDER_URL, "http://localhost:8080/tomee/ejb");
      // props.put(Context.SECURITY_PRINCIPAL, "username"); // if needed
      // props.put(Context.SECURITY_CREDENTIALS, "password"); // if needed

      context = new InitialContext(props);

      System.out.println("=== JNDI TREE ===");
      listContext("", context);

    } catch (NamingException e) {
      System.err.println("JNDI lookup failed: " + e.getMessage());
      e.printStackTrace();
    } finally {
      if (context != null) {
        try {
          context.close();
        } catch (NamingException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void listContext(String prefix, Context ctx) throws NamingException {
    Enumeration<Binding> bindings = ctx.listBindings("");

    while (bindings.hasMoreElements()) {
      Binding binding = bindings.nextElement();
      String name = binding.getName();
      String fullName = prefix + name;
      Object obj = binding.getObject();

      System.out.println(fullName + " [class: " +
          (obj != null ? obj.getClass().getName() : "null") + "]");

      // Recursively explore if it's a context
      if (obj instanceof Context) {
        listContext(fullName + "/", (Context) obj);
      }
    }
  }
}