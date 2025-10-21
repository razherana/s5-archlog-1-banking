package mg.razherana.banking.interfaces.application.remoteServices;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class EJBLookupService {
  private Context context;

  public EJBLookupService() throws NamingException {
    // Create once, reuse multiple times
    Properties props = new Properties();
    props.put(Context.INITIAL_CONTEXT_FACTORY,
        "org.apache.openejb.client.RemoteInitialContextFactory");
    props.put(Context.PROVIDER_URL, "http://localhost:8080/tomee/ejb");

    this.context = new InitialContext(props);
  }

  public EJBLookupService(String url) throws NamingException {
    // Create once, reuse multiple times
    Properties props = new Properties();
    props.put(Context.INITIAL_CONTEXT_FACTORY,
        "org.apache.openejb.client.RemoteInitialContextFactory");
    props.put(Context.PROVIDER_URL, "http://" + url + ":8080/tomee/ejb");

    this.context = new InitialContext(props);
  }

  public <T> T lookupStatefulBean(String jndiName, Class<T> clazz) throws NamingException {
    return clazz.cast(context.lookup(jndiName));
  }

  public void close() throws NamingException {
    if (context != null) {
      context.close();
    }
  }
}
