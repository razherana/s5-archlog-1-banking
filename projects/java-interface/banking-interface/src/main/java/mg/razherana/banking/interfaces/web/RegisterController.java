package mg.razherana.banking.interfaces.web;

import mg.razherana.banking.interfaces.application.userServices.UserService;
import mg.razherana.banking.interfaces.entities.User;

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
 * Web Controller for user registration.
 * 
 * <p>
 * Handles user registration form submission.
 * </p>
 */
@WebServlet("/register")
public class RegisterController extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(RegisterController.class.getName());

  @EJB
  private UserService userService;

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String name = request.getParameter("name");
    String email = request.getParameter("email");
    String password = request.getParameter("password");

    LOG.info("Registration attempt for email: " + email);

    try {
      User user = userService.createUser(name, email, password);
      if (user != null) {
        // Registration successful - auto-login
        HttpSession session = request.getSession();
        session.setAttribute("user", user);
        session.setAttribute("userId", user.getId());
        session.setAttribute("userName", user.getName());

        LOG.info("Registration and auto-login successful for user: " + email);
        response.sendRedirect("menu.html?success=registered");
      } else {
        LOG.warning("Registration failed for user: " + email);
        response.sendRedirect("register.html?error=failed");
      }

    } catch (Exception e) {
      LOG.severe("Error during registration: " + e.getMessage());
      if (e.getMessage().contains("already exists")) {
        response.sendRedirect("register.html?error=exists");
      } else {
        response.sendRedirect("register.html?error=system");
      }
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // Redirect GET requests to register page
    response.sendRedirect("register.html");
  }
}