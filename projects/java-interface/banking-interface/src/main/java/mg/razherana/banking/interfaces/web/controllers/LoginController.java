package mg.razherana.banking.interfaces.web.controllers;

import mg.razherana.banking.common.entities.User;
import mg.razherana.banking.common.services.userServices.UserService;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Web Controller for user authentication (login).
 * 
 * <p>
 * Handles login form submission and session management.
 * </p>
 */
@WebServlet("/login")
public class LoginController extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(LoginController.class.getName());

  @EJB
  private UserService userService;

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String email = request.getParameter("email");
    String password = request.getParameter("password");

    LOG.info("Login attempt for email: " + email);

    try {
      User user = userService.authenticateUser(email, password);

      if (user != null) {
        // Login successful - create session
        HttpSession session = request.getSession();
        session.setAttribute("user", user);
        session.setAttribute("userId", user.getId());
        session.setAttribute("userName", user.getName());

        LOG.info("Login successful for user: " + email);
        response.sendRedirect("menu.html");
      } else {
        // Login failed
        LOG.info("Login failed for user: " + email);
        response.sendRedirect("login.html?error=invalid");
      }

    } catch (Exception e) {
      LOG.severe("Error during login: " + e.getMessage());
      response.sendRedirect("login.html?error=system");
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // Redirect GET requests to login page
    response.sendRedirect("login.html");
  }
}