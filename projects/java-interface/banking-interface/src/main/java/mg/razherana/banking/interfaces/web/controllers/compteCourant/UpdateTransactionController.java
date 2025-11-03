package mg.razherana.banking.interfaces.web.controllers.compteCourant;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mg.razherana.banking.common.entities.UserAdmin;
import mg.razherana.banking.common.utils.ExceptionUtils;
import mg.razherana.banking.interfaces.application.compteCourantServices.CompteCourantService;

@WebServlet("/comptes-courants/transactions/update")
public class UpdateTransactionController extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(UpdateTransactionController.class.getName());

  @EJB
  private CompteCourantService compteCourantService;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    HttpSession session = req.getSession(false);
    if (session == null || session.getAttribute("userAdmin") == null) {
      resp.sendRedirect("../login.html");
      return;
    }

    UserAdmin userAdmin = (UserAdmin) session.getAttribute("userAdmin");
    String idTransactionStr = req.getParameter("idTransaction");
    String montantStr = req.getParameter("montant");
    String change = req.getParameter("change");

    // Validate required parameters
    if (idTransactionStr == null || idTransactionStr.trim().isEmpty()) {
      resp.sendRedirect("/comptes-courants?error=Transaction ID missing");
      return;
    }

    if (montantStr == null || montantStr.trim().isEmpty()) {
      resp.sendRedirect("/comptes-courants?error=Amount missing");
      return;
    }

    try {
      Integer idTransaction = Integer.parseInt(idTransactionStr);
      BigDecimal montant = new BigDecimal(montantStr);

      // Update the transaction
      compteCourantService.updateTransaction(userAdmin, idTransaction, montant, change);

      LOG.info("Transaction " + idTransaction + " updated successfully by user " + userAdmin.getEmail());

      // Redirect back with success message
      String successMsg = "Transaction mise à jour avec succès.";
      String encodedSuccess = URLEncoder.encode(successMsg, StandardCharsets.UTF_8);
      resp.sendRedirect("/comptes-courants?success=" + encodedSuccess);

    } catch (NumberFormatException e) {
      resp.sendRedirect("/comptes-courants?error=Invalid ID or amount format");
    } catch (Exception e) {
      var ex = ExceptionUtils.root(e);
      LOG.severe("Error updating transaction: " + ex.getMessage());
      ex.printStackTrace();
      String errorMsg = "Erreur lors de la mise à jour de la transaction: " + ex.getMessage();
      String encodedError = URLEncoder.encode(errorMsg, StandardCharsets.UTF_8);
      resp.sendRedirect("/comptes-courants?error=" + encodedError);
    }
  }
}
