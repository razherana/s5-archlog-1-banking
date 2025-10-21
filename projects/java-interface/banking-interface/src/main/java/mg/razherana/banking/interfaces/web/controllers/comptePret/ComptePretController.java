package mg.razherana.banking.interfaces.web.controllers.comptePret;

import mg.razherana.banking.interfaces.application.comptePretServices.ComptePretService;
import mg.razherana.banking.interfaces.application.template.ThymeleafService;
import mg.razherana.banking.interfaces.dto.comptePret.ComptePretDTO;
import mg.razherana.banking.common.entities.User;

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
import java.util.List;
import java.util.logging.Logger;

/**
 * Web Controller for displaying loan accounts (comptes prêts).
 */
@WebServlet("/comptes-prets")
public class ComptePretController extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(ComptePretController.class.getName());

  @EJB
  private ComptePretService comptePretService;

  @EJB
  private ThymeleafService thymeleafService;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("user") == null) {
      response.sendRedirect("login.html");
      return;
    }

    User user = (User) session.getAttribute("user");
    Integer userId = user.getId();

    LOG.info("Fetching loan accounts for user: " + userId);

    List<ComptePretDTO> loans = comptePretService.getLoansByUserId(userId);

    // Create Thymeleaf context
    JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
    WebContext context = new WebContext(application.buildExchange(request, response));

    // Set template variables
    context.setVariable("userName", user.getName());
    context.setVariable("loans", loans);

    // Process template and write response
    response.setContentType("text/html;charset=UTF-8");
    thymeleafService.getTemplateEngine(getServletContext()).process("comptes-prets/list", context,
        response.getWriter());
  }
}