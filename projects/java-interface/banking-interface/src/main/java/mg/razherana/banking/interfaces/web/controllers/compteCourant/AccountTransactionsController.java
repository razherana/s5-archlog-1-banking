package mg.razherana.banking.interfaces.web.controllers.compteCourant;

import mg.razherana.banking.common.entities.UserAdmin;
import mg.razherana.banking.courant.entities.CompteCourant;
import mg.razherana.banking.courant.entities.TransactionCourant;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * Displays the transaction list for a specific current account.
 */
@WebServlet("/comptes-courants/transactions")
public class AccountTransactionsController extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(AccountTransactionsController.class.getName());

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
    String accountIdParam = request.getParameter("accountId");

    if (accountIdParam == null || accountIdParam.isBlank()) {
      response.sendRedirect("/comptes-courants?error=missing_account_id");
      return;
    }

    try {
      Integer accountId = Integer.valueOf(accountIdParam);
      CompteCourant account = compteCourantService.getAccountById(userAdmin, accountId);

      if (account == null) {
        response.sendRedirect("/comptes-courants?error=account_not_found");
        return;
      }

      List<TransactionCourant> transactions = compteCourantService.getTransactionHistory(userAdmin, accountId);
      BigDecimal currentBalance = compteCourantService.getAccountBalance(userAdmin, accountId, LocalDateTime.now());

      JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
      WebContext context = new WebContext(application.buildExchange(request, response));

      context.setVariable("userAdminName", userAdmin.getEmail());
      context.setVariable("account", account);
      context.setVariable("transactions", transactions);
      context.setVariable("currentBalance", currentBalance);

      response.setContentType("text/html;charset=UTF-8");
      thymeleafService.getTemplateEngine(getServletContext())
          .process("comptes-courants/transactions", context, response.getWriter());
    } catch (NumberFormatException e) {
      LOG.warning("Invalid account id provided: " + accountIdParam);
      response.sendRedirect("/comptes-courants?error=invalid_account_id");
    } catch (Exception e) {
      LOG.severe("Unable to load transactions: " + e.getMessage());
      response.sendRedirect("/comptes-courants?error=transactions_fetch_failed");
    }
  }
}
