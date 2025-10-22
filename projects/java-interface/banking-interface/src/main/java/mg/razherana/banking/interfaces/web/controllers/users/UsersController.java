package mg.razherana.banking.interfaces.web.controllers.users;

import mg.razherana.banking.interfaces.application.template.ThymeleafService;
import mg.razherana.banking.common.entities.User;
import mg.razherana.banking.common.entities.UserAdmin;
import mg.razherana.banking.common.services.userServices.UserService;
import mg.razherana.banking.common.utils.ExceptionUtils;

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
import java.util.List;
import java.util.logging.Logger;

/**
 * Web Controller for user management - list all users.
 */
@WebServlet("/users")
public class UsersController extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(UsersController.class.getName());

  @EJB
  private UserService userService;

  @EJB
  private ThymeleafService thymeleafService;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("userAdmin") == null) {
      response.sendRedirect("login.html");
      return;
    }

    UserAdmin currentUserAdmin = (UserAdmin) session.getAttribute("userAdmin");

    try {
      // Get all users
      List<User> allUsers = userService.getAllUsers(currentUserAdmin);

      // Create Thymeleaf context
      JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
      WebContext context = new WebContext(application.buildExchange(request, response));

      // Set template variables
      context.setVariable("userAdminName", currentUserAdmin.getEmail());
      context.setVariable("users", allUsers);
      context.setVariable("error", request.getParameter("error"));
      context.setVariable("success", request.getParameter("success"));

      // Process template and write response
      response.setContentType("text/html;charset=UTF-8");
      thymeleafService.getTemplateEngine(getServletContext())
          .process("users/list", context, response.getWriter());

    } catch (Exception e) {
      e = ExceptionUtils.root(e);
      LOG.severe("Error loading users: " + e.getMessage());
      response.sendRedirect("menu.html?error=" + e.getMessage());
    }
  }
}