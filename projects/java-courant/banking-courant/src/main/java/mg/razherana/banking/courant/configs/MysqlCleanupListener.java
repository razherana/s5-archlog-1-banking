package mg.razherana.banking.courant.configs;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class MysqlCleanupListener implements ServletContextListener {

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    AbandonedConnectionCleanupThread.checkedShutdown();
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    // Nothing needed
  }
}
