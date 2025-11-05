package mg.razherana.banking.interfaces.web.filters;

import java.io.IOException;
import java.util.logging.Logger;

import jakarta.ejb.EJB;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mg.razherana.banking.interfaces.application.cacheManagerServices.EJBCacheManager;

@WebFilter("/*")
public class ServerReloadFilter extends HttpFilter {
    private static final String SERVER_START_TIME = String.valueOf(System.currentTimeMillis());
    private static final String START_TIME_ATTRIBUTE = "serverStartTime";
    
    @EJB
    private EJBCacheManager cacheManager;

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpSession session = request.getSession(true);

        String sessionStartTime = (String) session.getAttribute(START_TIME_ATTRIBUTE);
        
        // If this is a new session after server restart
        if (sessionStartTime == null || !sessionStartTime.equals(SERVER_START_TIME)) {
            session.setAttribute(START_TIME_ATTRIBUTE, SERVER_START_TIME);
            
            // Force refresh all EJB caches
            refreshEJBCaches();
        }
        
        chain.doFilter(request, response);
    }
    
    private void refreshEJBCaches() {
        try {
            // Lookup and call cache manager
            cacheManager.refreshAllCaches();
        } catch (Exception e) {
            // Log but don't break the request
            Logger.getLogger(ServerReloadFilter.class.getName())
                  .warning("Failed to refresh EJB caches: " + e.getMessage());
        }
    }
}
