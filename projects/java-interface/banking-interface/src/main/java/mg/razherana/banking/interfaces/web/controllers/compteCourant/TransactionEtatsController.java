package mg.razherana.banking.interfaces.web.controllers.compteCourant;

import mg.razherana.banking.common.entities.UserAdmin;
import mg.razherana.banking.courant.entities.TransactionCourant;
import mg.razherana.banking.courant.entities.TransactionEtat;
import mg.razherana.banking.courant.entities.TransactionEtat.TransactionEtatEnum;
import mg.razherana.banking.interfaces.application.compteCourantServices.CompteCourantService;
import mg.razherana.banking.interfaces.application.template.ThymeleafService;

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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Shows the state history for a given transaction.
 */
@WebServlet("/comptes-courants/transaction-etats")
public class TransactionEtatsController extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(TransactionEtatsController.class.getName());

  @EJB
  private CompteCourantService compteCourantService;

  @EJB
  private ThymeleafService thymeleafService;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("userAdmin") == null) {
      response.sendRedirect("../login.html");
      return;
    }

    UserAdmin userAdmin = (UserAdmin) session.getAttribute("userAdmin");
    String transactionIdParam = request.getParameter("transactionId");
    String accountIdParam = request.getParameter("accountId");

    if (transactionIdParam == null || transactionIdParam.isBlank()) {
      response.sendRedirect("/comptes-courants?error=missing_transaction_id");
      return;
    }

    try {
      Integer transactionId = Integer.valueOf(transactionIdParam);
      Integer accountId = null;
      if (accountIdParam != null && !accountIdParam.isBlank()) {
        try {
          accountId = Integer.valueOf(accountIdParam);
        } catch (NumberFormatException ex) {
          LOG.warning("Invalid account id provided for navigation: " + accountIdParam);
        }
      }
      TransactionCourant transaction = compteCourantService.getTransactionById(userAdmin, transactionId);

      if (transaction == null) {
        response.sendRedirect("/comptes-courants?error=transaction_not_found");
        return;
      }

      List<TransactionEtat> etats = compteCourantService.getTransactionEtats(userAdmin, transactionId);

      JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
      WebContext context = new WebContext(application.buildExchange(request, response));

      context.setVariable("userAdminName", userAdmin.getEmail());
      context.setVariable("transaction", transaction);
      context.setVariable("etats", etats);
      context.setVariable("accountId", accountId);
      context.setVariable("etatOptions", TransactionEtatEnum.values());

      String successMessage = (String) session.getAttribute("transactionEtatSuccess");
      if (successMessage != null) {
        context.setVariable("successMessage", successMessage);
        session.removeAttribute("transactionEtatSuccess");
      }

      String errorMessage = (String) session.getAttribute("transactionEtatError");
      if (errorMessage != null) {
        context.setVariable("errorMessage", errorMessage);
        session.removeAttribute("transactionEtatError");
      }

      response.setContentType("text/html;charset=UTF-8");
      thymeleafService.getTemplateEngine(getServletContext())
          .process("comptes-courants/transaction-etats", context, response.getWriter());
    } catch (NumberFormatException e) {
      LOG.warning("Invalid transaction id provided: " + transactionIdParam);
      response.sendRedirect("/comptes-courants?error=invalid_transaction_id");
    } catch (Exception e) {
      LOG.severe("Unable to load transaction states: " + e.getMessage());
      response.sendRedirect("/comptes-courants?error=transaction_states_fetch_failed");
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("userAdmin") == null) {
      response.sendRedirect("../login.html");
      return;
    }

    UserAdmin userAdmin = (UserAdmin) session.getAttribute("userAdmin");
    String transactionIdParam = request.getParameter("transactionId");
    String accountIdParam = request.getParameter("accountId");
    String etatParam = request.getParameter("etat");
    String actionDateParam = request.getParameter("actionDateTime");

    Integer transactionId;
    try {
      transactionId = Integer.valueOf(transactionIdParam);
    } catch (Exception ex) {
      session.setAttribute("transactionEtatError", "Identifiant de transaction invalide");
      response.sendRedirect("/comptes-courants?error=invalid_transaction_id");
      return;
    }

    Integer accountId = null;
    if (accountIdParam != null && !accountIdParam.isBlank()) {
      try {
        accountId = Integer.valueOf(accountIdParam);
      } catch (NumberFormatException ex) {
        LOG.warning("Invalid account id provided in transaction state form: " + accountIdParam);
      }
    }

    try {
      if (etatParam == null || etatParam.isBlank()) {
        throw new IllegalArgumentException("Veuillez sélectionner un nouvel état");
      }

      TransactionEtatEnum newEtat = TransactionEtatEnum.valueOf(etatParam);

      LocalDateTime actionDateTime = null;
      if (actionDateParam != null && !actionDateParam.isBlank()) {
        try {
          actionDateTime = LocalDateTime.parse(actionDateParam, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        } catch (DateTimeParseException ex) {
          throw new IllegalArgumentException("Format de date invalide. Utilisez AAAA-MM-JJ HH:MM.");
        }
      }

      compteCourantService.updateTransactionEtat(userAdmin, transactionId, newEtat, actionDateTime);
      session.setAttribute("transactionEtatSuccess",
          "L'état du virement a été mis à jour en " + newEtat.name() + ".");
    } catch (Exception e) {
      LOG.severe("Unable to update transaction state: " + e.getMessage());
      String message = e.getMessage() != null ? e.getMessage() : "Une erreur est survenue lors de la mise à jour.";
      session.setAttribute("transactionEtatError", message);
    }

    StringBuilder redirectUrl = new StringBuilder("/comptes-courants/transaction-etats?transactionId=" + transactionId);
    if (accountId != null) {
      redirectUrl.append("&accountId=").append(accountId);
    }

    response.sendRedirect(redirectUrl.toString());
  }
}
