package mg.razherana.banking.interfaces.web.controllers.compteCourant;

import mg.razherana.banking.common.entities.UserAdmin;
import mg.razherana.banking.common.utils.ExceptionUtils;
import mg.razherana.banking.interfaces.application.compteCourantServices.CompteCourantService;

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
 * Web Controller for validating transactions.
 */
@WebServlet("/comptes-courants/invalidate-transaction")
public class TransactionInvalidationController extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(TransactionInvalidationController.class.getName());

  @EJB
  private CompteCourantService compteCourantService;

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("userAdmin") == null) {
      response.sendRedirect("../login.html");
      return;
    }

    UserAdmin userAdmin = (UserAdmin) session.getAttribute("userAdmin");
    String transactionIdStr = request.getParameter("transactionId");
    String accountIdStr = request.getParameter("accountId");

    if (transactionIdStr == null || transactionIdStr.trim().isEmpty()) {
      response.sendRedirect("account-status?error=Transaction ID missing");
      return;
    }

    if (accountIdStr == null || accountIdStr.trim().isEmpty()) {
      response.sendRedirect("account-status?error=Account ID missing");
      return;
    }

    try {
      Integer transactionId = Integer.parseInt(transactionIdStr);
      Integer accountId = Integer.parseInt(accountIdStr);

      // Validate the transaction
      compteCourantService.validateTransaction(userAdmin, transactionId, null);

      LOG.info("Transaction " + transactionId + " set back to pending by user " + userAdmin.getEmail());

      // Redirect back to account status page with success message
      String successMsg = "Transaction invalidée avec succès.";
      String encodedSuccess = URLEncoder.encode(successMsg, StandardCharsets.UTF_8);
      response.sendRedirect("/comptes-courants/detail?id=" + accountId + "&success=" + encodedSuccess);

    } catch (NumberFormatException e) {
      response.sendRedirect("/comptes-courants?error=Invalid ID format");
    } catch (Exception e) {
      var ex = ExceptionUtils.root(e);
      LOG.severe("Error validating transaction: " + ex.getMessage());
      ex.printStackTrace();
      String errorMsg = "Erreur lors de la validation de la transaction: " + ex.getMessage();
      String encodedError = URLEncoder.encode(errorMsg, StandardCharsets.UTF_8);
      response.sendRedirect("/comptes-courants?&error=" + encodedError);
    }
  }
}