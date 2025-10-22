package mg.razherana.banking.interfaces.web.controllers.comptePret;

import mg.razherana.banking.interfaces.application.comptePretServices.ComptePretService;
import mg.razherana.banking.interfaces.application.template.ThymeleafService;
import mg.razherana.banking.interfaces.dto.comptePret.*;
import mg.razherana.banking.common.entities.UserAdmin;
import mg.razherana.banking.common.services.userServices.UserService;
import mg.razherana.banking.common.utils.ExceptionUtils;

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
import java.time.YearMonth;
import java.util.List;
import java.util.logging.Logger;

/**
 * Web Controller for loan payment status page.
 */
@WebServlet("/comptes-prets/payment-status")
public class PaymentStatusController extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(PaymentStatusController.class.getName());

  @EJB
  private ComptePretService comptePretService;

  @EJB
  private ThymeleafService thymeleafService;

  @EJB
  private UserService userService;

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("userAdmin") == null) {
      response.sendRedirect("../login.html");
      return;
    }

    UserAdmin userAdmin = (UserAdmin) session.getAttribute("userAdmin");
    String loanIdStr = request.getParameter("loanId");
    String actionDateTimeStr = request.getParameter("actionDateTime");

    if (loanIdStr == null || loanIdStr.trim().isEmpty()) {
      response.sendRedirect("../comptes-prets?error=missing_loan_id");
      return;
    }

    if (actionDateTimeStr == null || actionDateTimeStr.trim().isEmpty()) {
      response.sendRedirect("detail?id=" + loanIdStr + "&error=missing_date");
      return;
    }

    try {
      Integer loanId = Integer.parseInt(loanIdStr);
      ComptePretDTO loan = comptePretService.getLoanById(userAdmin, loanId);

      if (loan == null) {
        response.sendRedirect("../comptes-prets?error=loan_not_found");
        return;
      }

      // UserAdmin can access any loan, no ownership check needed

      // Parse the action date and set time to end of day for status check
      LocalDateTime actionDateTime = LocalDateTime.parse(actionDateTimeStr + "T23:59:59");

      if (actionDateTime.isBefore(loan.getDateDebut())) {
        var str = "La date de l'action ne peut pas être antérieure à la date de début du prêt.";
        str = URLEncoder.encode(str, StandardCharsets.UTF_8);

        response.sendRedirect("detail?id=" + loanId + "&error=" + str);
        return;
      }

      // Use API call to get payment status with the specific date
      // This leverages the backend's comprehensive payment calculation logic
      // instead of manually calculating values in the frontend
      PaymentStatusDTO paymentStatus = comptePretService.getPaymentStatus(userAdmin, loanId, actionDateTime);
      List<EcheanceDTO> paymentHistory = comptePretService.getPaymentHistory(userAdmin, loanId, actionDateTime);

      // Extract values from API response or set defaults if null
      BigDecimal totalExpected = paymentStatus != null ? paymentStatus.getTotalExpected() : BigDecimal.ZERO;
      BigDecimal remainingToPay = paymentStatus != null ? paymentStatus.getAmountDue() : loan.getMontant();
      BigDecimal monthlyPayment = paymentStatus != null ? paymentStatus.getMonthlyPayment() : loan.getMonthlyPayment();

      // Calculate what should be paid this month (from action date)
      YearMonth actionMonth = YearMonth.from(actionDateTime);
      YearMonth loanStartMonth = YearMonth.from(loan.getDateDebut());

      BigDecimal toPayThisMonth = BigDecimal.ZERO;
      BigDecimal remainingThisMonth = BigDecimal.ZERO;

      if (!actionMonth.isBefore(loanStartMonth)) {
        toPayThisMonth = monthlyPayment;

        // Calculate how much of this month's payment is remaining
        BigDecimal paidThisMonth = calculatePaidInMonth(paymentHistory, actionMonth);
        remainingThisMonth = toPayThisMonth.subtract(paidThisMonth);
        if (remainingThisMonth.compareTo(BigDecimal.ZERO) < 0) {
          remainingThisMonth = BigDecimal.ZERO;
        }
      }

      // Create Thymeleaf context
      JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
      WebContext context = new WebContext(application.buildExchange(request, response));

      // Set template variables
      context.setVariable("userAdminName", userAdmin.getEmail());
      context.setVariable("BD_ZERO", BigDecimal.ZERO);
      context.setVariable("loan", loan);
      context.setVariable("paymentStatus", paymentStatus);
      context.setVariable("paymentHistory", paymentHistory);
      context.setVariable("actionDateTime", actionDateTime);
      context.setVariable("remainingToPay", remainingToPay);
      context.setVariable("toPayThisMonth", toPayThisMonth);
      context.setVariable("remainingThisMonth", remainingThisMonth);
      context.setVariable("expectedByDate", totalExpected); // Use API-calculated value
      context.setVariable("error", request.getParameter("error"));
      context.setVariable("success", request.getParameter("success"));

      // Process template and write response
      response.setContentType("text/html;charset=UTF-8");
      thymeleafService.getTemplateEngine(getServletContext())
          .process("comptes-prets/payment-status", context, response.getWriter());

    } catch (NumberFormatException e) {
      response.sendRedirect("../comptes-prets?error=invalid_loan_id");
    } catch (Exception e) {
      e = ExceptionUtils.root(e);
      LOG.severe("Error processing payment status: " + e.getMessage());
      response.sendRedirect("detail?id=" + loanIdStr + "&error=" + e.getMessage());
    }
  }

  /**
   * Calculate total amount paid in a specific month
   */
  private BigDecimal calculatePaidInMonth(List<EcheanceDTO> paymentHistory, YearMonth targetMonth) {
    if (paymentHistory == null) {
      return BigDecimal.ZERO;
    }

    return paymentHistory.stream()
        .filter(payment -> payment.getDateEcheance() != null)
        .filter(payment -> YearMonth.from(payment.getDateEcheance()).equals(targetMonth))
        .map(EcheanceDTO::getMontant)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}