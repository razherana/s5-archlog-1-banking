package mg.razherana.banking.interfaces.web.controllers;

import java.io.IOException;

import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mg.razherana.banking.interfaces.application.template.ThymeleafService;
import mg.razherana.banking.common.entities.UserAdmin;
import mg.razherana.banking.common.services.userServices.UserService;

@WebServlet({ "/menu", "/menu.html" })
public class MenuController extends HttpServlet {
  @EJB
  private ThymeleafService thymeleafService;

  @EJB
  private UserService userService;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    HttpSession session = req.getSession(false);
    if (session == null || session.getAttribute("userAdmin") == null) {
      resp.sendRedirect(req.getContextPath() + "/login");
      return;
    }

    UserAdmin userAdmin = (UserAdmin) session.getAttribute("userAdmin");

    // Create Thymeleaf context
    JakartaServletWebApplication application = JakartaServletWebApplication
        .buildApplication(getServletContext());
    WebContext webContext = new WebContext(application.buildExchange(req, resp));

    // Add variables to context
    webContext.setVariable("userAdmin", userAdmin);
    webContext.setVariable("users", userService.getAllUsers(userAdmin));

    // Process template
    resp.setContentType("text/html;charset=UTF-8");
    thymeleafService.getTemplateEngine(getServletContext()).process("menu", webContext,
        resp.getWriter());
  }
}
