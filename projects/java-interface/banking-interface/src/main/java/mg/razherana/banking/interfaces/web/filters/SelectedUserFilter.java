package mg.razherana.banking.interfaces.web.filters;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.logging.Logger;

@WebFilter("/")
public class SelectedUserFilter extends HttpFilter {
  private static final Logger LOG = Logger.getLogger(SelectedUserFilter.class.getName());

  @Override
  protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws IOException, ServletException {

    LOG.info("SelectedUserFilter: Checking authentication for " + req.getRequestURI());

    String[] ignore = {
        "/login.html",
        "/login",
        "/register",
        "/selectUser",
        "/users"
    };

    var uri = req.getRequestURI().replace(req.getContextPath(), "");

    var check = Arrays.stream(ignore)
        .anyMatch(ignoredUri -> uri.startsWith(ignoredUri));

    if (!check && (req.getSession(false) == null || req.getSession(false).getAttribute("user") == null)) {
      res.sendRedirect(req.getContextPath() + "/login.html");
      return;
    }
    
    chain.doFilter(req, res);
  }
}
