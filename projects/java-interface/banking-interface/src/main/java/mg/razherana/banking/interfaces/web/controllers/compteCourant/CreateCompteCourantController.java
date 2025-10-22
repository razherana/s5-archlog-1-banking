package mg.razherana.banking.interfaces.web.controllers.compteCourant;

import mg.razherana.banking.interfaces.application.compteCourantServices.CompteCourantService;
import mg.razherana.banking.interfaces.application.template.ThymeleafService;
import mg.razherana.banking.courant.entities.CompteCourant;
import mg.razherana.banking.common.entities.UserAdmin;
import mg.razherana.banking.common.services.userServices.UserService;

import java.util.Map;

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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

/**
 * Web Controller for creating new current accounts (comptes courants).
 */
@WebServlet("/comptes-courants/create")
public class CreateCompteCourantController extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(CreateCompteCourantController.class.getName());

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
      response.sendRedirect("../login.html");
      return;
    }

    UserAdmin userAdmin = (UserAdmin) session.getAttribute("userAdmin");

    // Get all users for dropdown
    Map<Integer, String> usersForDropdown = userService.getAllUsersForDropdown();

    // Create Thymeleaf context
    JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
    WebContext context = new WebContext(application.buildExchange(request, response));

    // Set template variables
    context.setVariable("userAdminName", userAdmin.getEmail());
    context.setVariable("usersForDropdown", usersForDropdown);
    context.setVariable("error", request.getParameter("error"));
    context.setVariable("success", request.getParameter("success"));

    // Process template and write response
    response.setContentType("text/html;charset=UTF-8");
    thymeleafService.getTemplateEngine(getServletContext())
        .process("comptes-courants/create", context, response.getWriter());
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("userAdmin") == null) {
      response.sendRedirect("../login.html");
      return;
    }

    // UserAdmin userAdmin = (UserAdmin) session.getAttribute("userAdmin");
    
    // Get userId from form parameter
    String userIdParam = request.getParameter("userId");
    if (userIdParam == null || userIdParam.trim().isEmpty()) {
      response.sendRedirect("create?error=missing_user_id");
      return;
    }
    
    Integer userId;
    try {
      userId = Integer.parseInt(userIdParam);
    } catch (NumberFormatException e) {
      response.sendRedirect("create?error=invalid_user_id");
      return;
    }

    try {
      // Get the monthly tax amount from form (optional)
      String taxeParam = request.getParameter("taxe");
      String actionDateTimeParam = request.getParameter("actionDateTime");
      BigDecimal taxe = BigDecimal.ZERO;
      LocalDateTime actionDateTime = LocalDateTime.now();

      if (taxeParam != null && !taxeParam.trim().isEmpty()) {
        try {
          taxe = new BigDecimal(taxeParam);
          if (taxe.compareTo(BigDecimal.ZERO) < 0) {
            response.sendRedirect("create?error=invalid_taxe");
            return;
          }
        } catch (NumberFormatException e) {
          response.sendRedirect("create?error=invalid_taxe_format");
          return;
        }
      }
      
      if (actionDateTimeParam != null && !actionDateTimeParam.trim().isEmpty()) {
        try {
          actionDateTime = LocalDateTime.parse(actionDateTimeParam, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
          LOG.warning("Invalid date format, using current time: " + e.getMessage());
          actionDateTime = LocalDateTime.now();
        }
      }

      LOG.info("Creating account for user: " + userId + " with taxe: " + taxe + " at time: " + actionDateTime);

      // Create the account
      CompteCourant createdAccount = compteCourantService.createAccount(userId, taxe, actionDateTime);

      if (createdAccount != null) {
        LOG.info("Account created successfully with ID: " + createdAccount.getId());
        response.sendRedirect("../comptes-courants?success=account_created");
      } else {
        LOG.warning("Failed to create account for user: " + userId);
        response.sendRedirect("create?error=creation_failed");
      }

    } catch (Exception e) {
      LOG.severe("Error during account creation: " + e.getMessage());
      response.sendRedirect("create?error=system_error");
    }
  }
}