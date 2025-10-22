package mg.razherana.banking.interfaces.web.controllers.compteCourant;

import mg.razherana.banking.interfaces.application.compteCourantServices.CompteCourantService;
import mg.razherana.banking.interfaces.application.template.ThymeleafService;
import mg.razherana.banking.courant.entities.CompteCourant;
import mg.razherana.banking.common.entities.UserAdmin;
import mg.razherana.banking.common.services.userServices.UserService;

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
 * Web Controller for displaying current accounts (comptes courants).
 */
@WebServlet("/comptes-courants")
public class CompteCourantController extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(CompteCourantController.class.getName());

  @EJB
  private CompteCourantService compteCourantService;

  @EJB
  private ThymeleafService thymeleafService;

  @EJB
  private UserService userService;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("userAdmin") == null) {
      response.sendRedirect("login.html");
      return;
    }

    UserAdmin userAdmin = (UserAdmin) session.getAttribute("userAdmin");
    String userIdParam = request.getParameter("userId");

    // Create Thymeleaf context
    JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
    WebContext context = new WebContext(application.buildExchange(request, response));

    // Set template variables
    context.setVariable("userAdminName", userAdmin.getEmail());
    context.setVariable("usersForDropdown", userService.getAllUsersForDropdown());
    context.setVariable("selectedUserId", userIdParam);

    // If userId is provided, fetch accounts for that user
    if (userIdParam != null && !userIdParam.trim().isEmpty()) {
      try {
        Integer userId = Integer.parseInt(userIdParam);
        LOG.info("Fetching current accounts for user: " + userId);

        List<CompteCourant> accounts = compteCourantService.getAccountsByUserId(userId);
        context.setVariable("accounts", accounts);
      } catch (NumberFormatException e) {
        LOG.warning("Invalid userId parameter: " + userIdParam);
        context.setVariable("error", "ID utilisateur invalide");
      }
    }

    // Process template and write response
    response.setContentType("text/html;charset=UTF-8");
    thymeleafService.getTemplateEngine(getServletContext()).process("comptes-courants/list", context,
        response.getWriter());
  }
}