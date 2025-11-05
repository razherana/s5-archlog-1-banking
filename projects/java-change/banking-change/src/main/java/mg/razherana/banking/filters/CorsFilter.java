package mg.razherana.banking.filters;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * CORS (Cross-Origin Resource Sharing) filter for handling cross-origin requests.
 * 
 * <p>
 * This filter automatically adds the necessary CORS headers to all HTTP responses,
 * allowing web browsers to make cross-origin requests to the API from different domains/ports.
 * </p>
 * 
 * <p>
 * <strong>Configured Headers:</strong>
 * </p>
 * <ul>
 * <li>Access-Control-Allow-Origin: * (allows requests from any origin)</li>
 * <li>Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS</li>
 * <li>Access-Control-Allow-Headers: Content-Type, Authorization</li>
 * <li>Access-Control-Max-Age: 3600 (cache preflight for 1 hour)</li>
 * </ul>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 */
@Provider
public class CorsFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        
        // Add CORS headers to allow cross-origin requests
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        responseContext.getHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        responseContext.getHeaders().add("Access-Control-Max-Age", "3600");
        
        // Allow credentials if needed (commented out for security)
        // responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
    }
}