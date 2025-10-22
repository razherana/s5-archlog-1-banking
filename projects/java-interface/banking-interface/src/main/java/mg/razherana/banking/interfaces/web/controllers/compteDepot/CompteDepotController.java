package mg.razherana.banking.interfaces.web.controllers.compteDepot;

import mg.razherana.banking.interfaces.application.compteDepotServices.CompteDepotService;
import mg.razherana.banking.interfaces.application.template.ThymeleafService;
import mg.razherana.banking.interfaces.dto.CompteDepotDTO;
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
 * Web Controller for deposit accounts listing.
 * Displays all deposit accounts for the logged-in user.
 */
@WebServlet("/comptes-depots")
public class CompteDepotController extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(CompteDepotController.class.getName());

  @EJB
  private CompteDepotService compteDepotService;

  @EJB
  private ThymeleafService thymeleafService;

  @EJB
  private UserService userService;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("userAdmin") == null) {
      response.sendRedirect(request.getContextPath() + "/login");
      return;
    }

    UserAdmin userAdmin = (UserAdmin) session.getAttribute("userAdmin");
    String userIdParam = request.getParameter("userId");

    // Create Thymeleaf context
    JakartaServletWebApplication application = JakartaServletWebApplication
        .buildApplication(getServletContext());
    WebContext webContext = new WebContext(application.buildExchange(request, response));

    // Add variables to context
    webContext.setVariable("userAdminName", userAdmin.getEmail());
    webContext.setVariable("usersForDropdown", userService.getAllUsersForDropdown());
    webContext.setVariable("selectedUserId", userIdParam);

    // If userId is provided, fetch accounts for that user
    if (userIdParam != null && !userIdParam.trim().isEmpty()) {
      try {
        Integer userId = Integer.parseInt(userIdParam);
        LOG.info("Fetching deposit accounts for user: " + userId);

        List<CompteDepotDTO> comptes = compteDepotService.getAccountsByUserId(userId);
        webContext.setVariable("comptes", comptes);
      } catch (NumberFormatException e) {
        LOG.warning("Invalid userId parameter: " + userIdParam);
        webContext.setVariable("error", "ID utilisateur invalide");
      }
    }

    webContext.setVariable("error", request.getParameter("error"));
    webContext.setVariable("success", request.getParameter("success"));

    // Process template
    response.setContentType("text/html;charset=UTF-8");
    thymeleafService.getTemplateEngine(getServletContext()).process("comptes-depots/list", webContext,
        response.getWriter());
  }
}