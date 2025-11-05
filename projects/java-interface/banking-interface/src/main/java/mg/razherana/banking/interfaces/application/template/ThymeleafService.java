package mg.razherana.banking.interfaces.application.template;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import jakarta.servlet.ServletContext;
import jakarta.ejb.Stateless;

/**
 * Thymeleaf configuration service for template processing.
 */
@Stateless
public class ThymeleafService {

    private TemplateEngine templateEngine;

    public TemplateEngine getTemplateEngine(ServletContext servletContext) {
        if (templateEngine == null) {
            initTemplateEngine(servletContext);
        }
        return templateEngine;
    }

    private void initTemplateEngine(ServletContext servletContext) {
        JakartaServletWebApplication application = 
            JakartaServletWebApplication.buildApplication(servletContext);
        
        WebApplicationTemplateResolver templateResolver = 
            new WebApplicationTemplateResolver(application);
        
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("/WEB-INF/views/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheTTLMs(3600000L); // 1 hour cache
        templateResolver.setCacheable(true);
        
        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
    }
}