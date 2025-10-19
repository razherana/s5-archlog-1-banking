package mg.razherana.banking.interfaces.web.controllers.users;

import mg.razherana.banking.interfaces.application.userServices.UserService;
import mg.razherana.banking.interfaces.application.template.ThymeleafService;
import mg.razherana.banking.interfaces.entities.User;

import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * Web Controller for creating new users.
 */
@WebServlet("/users/create")
public class CreateUserController extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(CreateUserController.class.getName());

  @EJB
  private UserService userService;

  @EJB
  private ThymeleafService thymeleafService;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("user") == null) {
      response.sendRedirect("../login.html");
      return;
    }

    User currentUser = (User) session.getAttribute("user");

    // Create Thymeleaf context
    JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
    WebContext context = new WebContext(application.buildExchange(request, response));

    // Set template variables
    context.setVariable("userName", currentUser.getName());
    context.setVariable("error", request.getParameter("error"));

    // Process template and write response
    response.setContentType("text/html;charset=UTF-8");
    thymeleafService.getTemplateEngine(getServletContext())
        .process("users/create", context, response.getWriter());
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("user") == null) {
      response.sendRedirect("../login.html");
      return;
    }

    String name = request.getParameter("name");
    String email = request.getParameter("email");
    String password = request.getParameter("password");

    // Basic validation
    if (name == null || name.trim().isEmpty()) {
      response.sendRedirect("create?error=" + URLEncoder.encode("Le nom est requis", StandardCharsets.UTF_8));
      return;
    }

    if (email == null || email.trim().isEmpty()) {
      response.sendRedirect("create?error=" + URLEncoder.encode("L'email est requis", StandardCharsets.UTF_8));
      return;
    }

    if (password == null || password.trim().isEmpty()) {
      response.sendRedirect("create?error=" + URLEncoder.encode("Le mot de passe est requis", StandardCharsets.UTF_8));
      return;
    }

    try {
      // Create the user
      User newUser = userService.createUser(name.trim(), email.trim(), password);
      
      LOG.info("User created successfully: " + newUser.getEmail());
      response.sendRedirect("../users?success=" + URLEncoder.encode("Utilisateur créé avec succès", StandardCharsets.UTF_8));

    } catch (IllegalArgumentException e) {
      LOG.warning("Failed to create user: " + e.getMessage());
      response.sendRedirect("create?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
    } catch (Exception e) {
      LOG.severe("Error creating user: " + e.getMessage());
      response.sendRedirect("create?error=" + URLEncoder.encode("Erreur système lors de la création", StandardCharsets.UTF_8));
    }
  }
}