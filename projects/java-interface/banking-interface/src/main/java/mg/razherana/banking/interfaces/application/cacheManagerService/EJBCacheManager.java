package mg.razherana.banking.interfaces.application.cacheManagerService;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

@Singleton
@Startup
public class EJBCacheManager {
    private static final Logger LOG = Logger.getLogger(EJBCacheManager.class.getName());
    
    private final List<Runnable> refreshCallbacks = new ArrayList<>();
    
    @PostConstruct
    public void init() {
        LOG.info("EJBCacheManager initialized - server started");
    }
    
    @PreDestroy
    public void cleanup() {
        LOG.info("EJBCacheManager cleaning up - server stopping");
    }
    
    public void registerRefreshCallback(Runnable callback) {
        refreshCallbacks.add(callback);
    }
    
    // Call this method when you detect server reload
    public void refreshAllCaches() {
        LOG.info("Refreshing all EJB caches");
        for (Runnable callback : refreshCallbacks) {
            try {
                callback.run();
            } catch (Exception e) {
                LOG.warning("Cache refresh callback failed: " + e.getMessage());
            }
        }
    }
}