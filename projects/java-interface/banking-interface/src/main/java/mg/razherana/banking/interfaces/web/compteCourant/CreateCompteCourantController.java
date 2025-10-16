package mg.razherana.banking.interfaces.web.compteCourant;

import mg.razherana.banking.interfaces.application.compteCourantServices.CompteCourantService;
import mg.razherana.banking.interfaces.application.template.ThymeleafService;
import mg.razherana.banking.interfaces.dto.CompteCourantDTO;
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

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("user") == null) {
      response.sendRedirect("../login.html");
      return;
    }

    User user = (User) session.getAttribute("user");

    // Create Thymeleaf context
    JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
    WebContext context = new WebContext(application.buildExchange(request, response));

    // Set template variables
    context.setVariable("userName", user.getName());
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
    if (session == null || session.getAttribute("user") == null) {
      response.sendRedirect("../login.html");
      return;
    }

    User user = (User) session.getAttribute("user");
    Integer userId = user.getId();

    try {
      // Get the monthly tax amount from form (optional)
      String taxeParam = request.getParameter("taxe");
      String actionDateTime = request.getParameter("actionDateTime");
      Double taxe = null;

      if (taxeParam != null && !taxeParam.trim().isEmpty()) {
        try {
          taxe = Double.parseDouble(taxeParam);
          if (taxe < 0) {
            response.sendRedirect("create?error=invalid_taxe");
            return;
          }
        } catch (NumberFormatException e) {
          response.sendRedirect("create?error=invalid_taxe_format");
          return;
        }
      }

      LOG.info("Creating account for user: " + userId + " with taxe: " + taxe +
          (actionDateTime != null && !actionDateTime.trim().isEmpty() ? " at time: " + actionDateTime : ""));

      // Create the account
      CompteCourantDTO createdAccount = compteCourantService.createAccount(userId, taxe, actionDateTime);

      if (createdAccount != null) {
        LOG.info("Account created successfully: " + createdAccount.getNumeroCompte());
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