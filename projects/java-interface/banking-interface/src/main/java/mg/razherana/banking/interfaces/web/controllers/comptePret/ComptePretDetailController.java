package mg.razherana.banking.interfaces.web.controllers.comptePret;

import mg.razherana.banking.interfaces.application.comptePretServices.ComptePretService;
import mg.razherana.banking.interfaces.application.template.ThymeleafService;
import mg.razherana.banking.interfaces.dto.comptePret.*;
import mg.razherana.banking.common.entities.User;

import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
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
 * Web Controller for loan detail page with payment capabilities.
 */
@WebServlet("/comptes-prets/detail")
public class ComptePretDetailController extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(ComptePretDetailController.class.getName());

  @EJB
  private ComptePretService comptePretService;

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
    String loanIdStr = request.getParameter("id");

    if (loanIdStr == null || loanIdStr.trim().isEmpty()) {
      response.sendRedirect("../comptes-prets?error=missing_loan_id");
      return;
    }

    try {
      Integer loanId = Integer.parseInt(loanIdStr);
      ComptePretDTO loan = comptePretService.getLoanById(loanId);

      if (loan == null) {
        response.sendRedirect("../comptes-prets?error=loan_not_found");
        return;
      }

      // Verify that the loan belongs to the logged-in user
      if (!loan.getUserId().equals(user.getId())) {
        response.sendRedirect("../comptes-prets?error=unauthorized_access");
        return;
      }

      // Get payment status and history
      PaymentStatusDTO paymentStatus = comptePretService.getPaymentStatus(loanId);
      List<EcheanceDTO> paymentHistory = comptePretService.getPaymentHistory(loanId);

      // Create Thymeleaf context
      JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
      WebContext context = new WebContext(application.buildExchange(request, response));

      // Set template variables
      context.setVariable("userName", user.getName());
      context.setVariable("loan", loan);
      context.setVariable("paymentStatus", paymentStatus);
      context.setVariable("paymentHistory", paymentHistory);
      context.setVariable("error", request.getParameter("error"));
      context.setVariable("success", request.getParameter("success"));

      // Add BigDecimal ZERO for Thymeleaf comparisons
      context.setVariable("ZERO", BigDecimal.ZERO);

      // Process template and write response
      response.setContentType("text/html;charset=UTF-8");
      thymeleafService.getTemplateEngine(getServletContext())
          .process("comptes-prets/detail", context, response.getWriter());

    } catch (NumberFormatException e) {
      response.sendRedirect("../comptes-prets?error=invalid_loan_id");
    }
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
    String action = request.getParameter("action");
    String loanIdStr = request.getParameter("loanId");

    if (loanIdStr == null || loanIdStr.trim().isEmpty()) {
      response.sendRedirect("../comptes-prets?error=missing_loan_id");
      return;
    }

    try {
      Integer loanId = Integer.parseInt(loanIdStr);
      ComptePretDTO loan = comptePretService.getLoanById(loanId);

      if (loan == null || !loan.getUserId().equals(user.getId())) {
        response.sendRedirect("../comptes-prets?error=loan_not_found");
        return;
      }

      if ("make_payment".equals(action)) {
        handleMakePayment(request, response, loanId);
      } else {
        response.sendRedirect("detail?id=" + loanId + "&error=invalid_action");
      }

    } catch (NumberFormatException e) {
      response.sendRedirect("../comptes-prets?error=invalid_loan_id");
    }
  }

  private void handleMakePayment(HttpServletRequest request, HttpServletResponse response, Integer loanId)
      throws IOException {

    String montantStr = request.getParameter("montant");
    String paymentDateStr = request.getParameter("paymentDate");

    if (montantStr == null || montantStr.trim().isEmpty()) {
      response.sendRedirect("detail?id=" + loanId + "&error=missing_amount");
      return;
    }

    try {
      BigDecimal montant = new BigDecimal(montantStr);

      if (montant.compareTo(BigDecimal.ZERO) <= 0) {
        response.sendRedirect("detail?id=" + loanId + "&error=invalid_amount");
        return;
      }

      // Create payment request
      MakePaymentRequest paymentRequest = new MakePaymentRequest();
      paymentRequest.setCompteId(loanId);
      paymentRequest.setMontant(montant);

      // Use provided date or current time
      if (paymentDateStr != null && !paymentDateStr.trim().isEmpty()) {
        try {
          LocalDateTime paymentDate = LocalDateTime.parse(paymentDateStr + "T10:00:00");
          paymentRequest.setActionDateTime(paymentDate);
        } catch (Exception e) {
          LOG.warning("Invalid payment date format: " + paymentDateStr + ", using current time");
          paymentRequest.setActionDateTime(LocalDateTime.now());
        }
      } else {
        paymentRequest.setActionDateTime(LocalDateTime.now());
      }

      // Make payment
      EcheanceDTO payment = comptePretService.makePayment(paymentRequest);

      if (payment != null) {
        response.sendRedirect("detail?id=" + loanId + "&success=payment_successful");
      } else {
        response.sendRedirect("detail?id=" + loanId + "&error=payment_failed");
      }

    } catch (NumberFormatException e) {
      response.sendRedirect("detail?id=" + loanId + "&error=invalid_amount_format");
    } catch (EJBException e) {
      LOG.severe("Error processing payment: " + e.getMessage());
      String str;
      if (e.getCause() != null) {
        str = e.getCause().getMessage();
        str = URLEncoder.encode(str, StandardCharsets.UTF_8);

        response.sendRedirect("detail?id=" + loanId + "&error=" + str);
      } else {
        str = e.getMessage();
        str = URLEncoder.encode(str, StandardCharsets.UTF_8);
        
        response.sendRedirect("detail?id=" + loanId + "&error=" + str);
      }
    }
  }
}