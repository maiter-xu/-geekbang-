package org.maiter.com.week3.router;

import java.util.List;

public interface HttpEndpointRouter {
    
    String route(List<String> endpoints);
}
