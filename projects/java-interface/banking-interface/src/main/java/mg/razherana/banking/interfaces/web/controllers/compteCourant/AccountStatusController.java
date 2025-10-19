package mg.razherana.banking.interfaces.web.controllers.compteCourant;

import mg.razherana.banking.interfaces.application.compteCourantServices.CompteCourantService;
import mg.razherana.banking.interfaces.application.template.ThymeleafService;
import mg.razherana.banking.interfaces.dto.CompteCourantDTO;
import mg.razherana.banking.interfaces.dto.AccountStatusDTO;
import mg.razherana.banking.interfaces.dto.TransactionCourantDTO;
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
    if (session == null || session.getAttribute("user") == null) {
      response.sendRedirect("../login.html");
      return;
    }

    User user = (User) session.getAttribute("user");
    String accountIdStr = request.getParameter("accountId");
    String actionDateTimeStr = request.getParameter("actionDateTime");

    if (accountIdStr == null || accountIdStr.trim().isEmpty()) {
      response.sendRedirect("../comptes-courants?error=missing_account_id");
      return;
    }

    if (actionDateTimeStr == null || actionDateTimeStr.trim().isEmpty()) {
      response.sendRedirect("detail?id=" + accountIdStr + "&error=missing_date");
      return;
    }

    try {
      Integer accountId = Integer.parseInt(accountIdStr);
      CompteCourantDTO account = compteCourantService.getAccountById(accountId);

      if (account == null) {
        response.sendRedirect("../comptes-courants?error=account_not_found");
        return;
      }

      // Verify that the account belongs to the logged-in user
      if (!account.getUserId().equals(user.getId())) {
        response.sendRedirect("../comptes-courants?error=unauthorized_access");
        return;
      }

      // Parse the action date and set time to end of day for status check
      LocalDateTime actionDateTime = LocalDateTime.parse(actionDateTimeStr + "T23:59:59");

      if (actionDateTime.isBefore(account.getDateOuverture())) {
        String errorMsg = "La date de vérification ne peut pas être antérieure à la date d'ouverture du compte.";
        String encodedError = URLEncoder.encode(errorMsg, StandardCharsets.UTF_8);
        response.sendRedirect("detail?id=" + accountId + "&error=" + encodedError);
        return;
      }

      // Get account status at the specified date
      AccountStatusDTO accountStatus = compteCourantService.getAccountStatus(accountId, actionDateTime);
      
      if (accountStatus == null) {
        response.sendRedirect("detail?id=" + accountId + "&error=status_calculation_failed");
        return;
      }

      // Get transaction history filtered up to the status date
      List<TransactionCourantDTO> transactionHistory = compteCourantService.getTransactionHistory(accountId);
      List<TransactionCourantDTO> filteredTransactions = transactionHistory.stream()
          .filter(t -> t.getDate() == null || !t.getDate().isAfter(actionDateTime))
          .sorted((t1, t2) -> {
            if (t1.getDate() == null && t2.getDate() == null) return 0;
            if (t1.getDate() == null) return 1;
            if (t2.getDate() == null) return -1;
            return t2.getDate().compareTo(t1.getDate()); // Most recent first
          })
          .collect(java.util.stream.Collectors.toList());

      // Create Thymeleaf context
      JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
      WebContext context = new WebContext(application.buildExchange(request, response));

      // Set template variables
      context.setVariable("userName", user.getName());
      context.setVariable("BD_ZERO", BigDecimal.ZERO);
      context.setVariable("account", account);
      context.setVariable("accountStatus", accountStatus);
      context.setVariable("transactionHistory", filteredTransactions);
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
      LOG.severe("Error processing account status: " + e.getMessage());
      response.sendRedirect("detail?id=" + accountIdStr + "&error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
    }
  }
}