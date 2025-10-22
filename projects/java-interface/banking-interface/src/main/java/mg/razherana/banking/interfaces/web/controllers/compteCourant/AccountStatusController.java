package mg.razherana.banking.interfaces.web.controllers.compteCourant;

import mg.razherana.banking.interfaces.application.compteCourantServices.CompteCourantService;
import mg.razherana.banking.interfaces.application.template.ThymeleafService;
import mg.razherana.banking.courant.entities.CompteCourant;
import mg.razherana.banking.courant.entities.TransactionCourant;
import mg.razherana.banking.common.entities.UserAdmin;
import mg.razherana.banking.common.utils.ExceptionUtils;
import mg.razherana.banking.interfaces.web.controllers.compteCourant.accountStatusDTOs.AccountStatusDTO;
import mg.razherana.banking.interfaces.web.controllers.compteCourant.accountStatusDTOs.TransactionDTO;

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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * Web Controller for current account status page.
 */
@WebServlet("/comptes-courants/account-status")
public class AccountStatusController extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(AccountStatusController.class.getName());

  @EJB
  private CompteCourantService compteCourantService;

  @EJB
  private ThymeleafService thymeleafService;

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("userAdmin") == null) {
      response.sendRedirect("../login.html");
      return;
    }

    UserAdmin userAdmin = (UserAdmin) session.getAttribute("userAdmin");
    String accountIdStr = request.getParameter("accountId");
    String actionDateTimeStr = request.getParameter("actionDateTime");

    if (accountIdStr == null || accountIdStr.trim().isEmpty()) {
      response.sendRedirect("../comptes-courants?error=Account ID missing");
      return;
    }

    if (actionDateTimeStr == null || actionDateTimeStr.trim().isEmpty()) {
      response.sendRedirect("detail?id=" + accountIdStr + "&error=Date missing");
      return;
    }

    try {
      Integer accountId = Integer.parseInt(accountIdStr);
      CompteCourant account = compteCourantService.getAccountById(userAdmin, accountId);

      if (account == null) {
        response.sendRedirect("../comptes-courants?error=Account not found");
        return;
      }

      // UserAdmin can view any account, no ownership check needed

      // Parse the action date and set time to end of day for status check
      LocalDateTime actionDateTime;
      try {
        actionDateTime = LocalDateTime.parse(actionDateTimeStr + "T23:59:59");
      } catch (Exception e) {
        response.sendRedirect("detail?id=" + accountIdStr + "&error=Invalid date format");
        return;
      }

      if (actionDateTime.isBefore(account.getCreatedAt())) {
        String errorMsg = "La date de vérification ne peut pas être antérieure à la date d'ouverture du compte.";
        String encodedError = URLEncoder.encode(errorMsg, StandardCharsets.UTF_8);
        response.sendRedirect("detail?id=" + accountId + "&error=" + encodedError);
        return;
      }

      // Get account balance at the specified date
      BigDecimal accountBalance = compteCourantService.getAccountBalance(userAdmin, accountId, actionDateTime);
      BigDecimal taxToPay = compteCourantService.getTaxToPay(userAdmin, accountId, actionDateTime);

      // Get transaction history filtered up to the status date
      List<TransactionCourant> transactionHistory = compteCourantService.getTransactionHistory(userAdmin, accountId);
      // System.out.println(transactionHistory);
      List<TransactionCourant> filteredTransactions = transactionHistory.stream()
          .filter(t -> !t.getDate().isAfter(actionDateTime))
          .sorted((t1, t2) -> t2.getDate().compareTo(t1.getDate())) // Most recent first
          .toList();

      // Create Thymeleaf context
      JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
      WebContext context = new WebContext(application.buildExchange(request, response));

      BigDecimal taxPaid = filteredTransactions.stream()
          .filter(t -> t.getSpecialAction().equals(TransactionCourant.SpecialAction.TAXE.getDatabaseName()))
          .map(e -> e.getMontant())
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      // Calculate transaction statistics
      int totalTransactions = filteredTransactions.size();

      BigDecimal totalDeposits = filteredTransactions.stream()
          .filter(t -> TransactionCourant.SpecialAction.DEPOSIT.getDatabaseName().equals(t.getSpecialAction()))
          .map(TransactionCourant::getMontant)
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      BigDecimal totalWithdrawals = filteredTransactions.stream()
          .filter(t -> TransactionCourant.SpecialAction.WITHDRAWAL.getDatabaseName().equals(t.getSpecialAction()))
          .map(TransactionCourant::getMontant)
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      BigDecimal totalTransfersSent = filteredTransactions.stream()
          .filter(t -> t.getSender() != null && t.getSender().getId().equals(accountId) &&
              t.getReceiver() != null)
          .map(TransactionCourant::getMontant)
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      BigDecimal totalTransfersReceived = filteredTransactions.stream()
          .filter(t -> t.getReceiver() != null && t.getReceiver().getId().equals(accountId) &&
              t.getSender() != null)
          .map(TransactionCourant::getMontant)
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      // Set template variables
      context.setVariable("userAdminName", userAdmin.getEmail());
      context.setVariable("BD_ZERO", BigDecimal.ZERO);
      context.setVariable("account", account);
      context.setVariable("accountBalance", accountBalance);
      context.setVariable("accountStatus", new AccountStatusDTO(accountBalance, taxPaid, taxToPay,
          totalTransactions, totalDeposits, totalWithdrawals,
          totalTransfersSent, totalTransfersReceived));
      context.setVariable("taxToPay", taxToPay);
      context.setVariable("statusDate", actionDateTime);
      context.setVariable("transactionHistory", filteredTransactions.stream().map(TransactionDTO::new).toList());
      context.setVariable("actionDateTime", actionDateTime);
      context.setVariable("error", request.getParameter("error"));
      context.setVariable("success", request.getParameter("success"));

      // Process template and write response
      response.setContentType("text/html;charset=UTF-8");
      thymeleafService.getTemplateEngine(getServletContext())
          .process("comptes-courants/account-status", context, response.getWriter());

    } catch (NumberFormatException e) {
      response.sendRedirect("../comptes-courants?error=invalid_account_id");
    } catch (Exception e) {
      var ex = ExceptionUtils.root(e);
      LOG.severe("Error processing account status: " + ex.getMessage());
      ex.printStackTrace();
      response.sendRedirect(
          "detail?id=" + accountIdStr + "&error=" + URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8));
    }
  }
}