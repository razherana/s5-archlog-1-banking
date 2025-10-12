package mg.razherana.banking.interfaces.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Web Controller for user logout.
 * 
 * <p>
 * Handles user logout and session cleanup.
 * </p>
 */
@WebServlet("/logout")
public class LogoutController extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(LogoutController.class.getName());

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);

    if (session != null) {
      String userName = (String) session.getAttribute("userName");
      LOG.info("User logout: " + (userName != null ? userName : "Unknown"));

      // Invalidate session
      session.invalidate();
    }

    // Redirect to login page
    response.sendRedirect("login.html?message=logged_out");
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // Handle POST requests the same way as GET
    doGet(request, response);
  }
}