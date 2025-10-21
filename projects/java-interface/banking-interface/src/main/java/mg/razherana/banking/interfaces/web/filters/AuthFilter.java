package mg.razherana.banking.interfaces.web.filters;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.logging.Logger;

@WebFilter("/")
public class AuthFilter extends HttpFilter {
  private static final Logger LOG = Logger.getLogger(AuthFilter.class.getName());

  @Override
  protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws IOException, ServletException {

    LOG.info("AuthFilter: Checking authentication for " + req.getRequestURI());

    if (!req.getRequestURI().replace(req.getContextPath(), "").equals("/login.html")
        && (req.getSession(false) == null || req.getSession(false).getAttribute("userAdmin") == null)) {
      res.sendRedirect(req.getContextPath() + "/login.html");
      return;
    }
    chain.doFilter(req, res);
  }
}
