package org.example.middleware;

import static spark.Spark.*;

public class LoggingMiddleware {
    
    public static void enable() {
        before((request, response) -> {
            System.out.println("📥 " + request.requestMethod() + " " + request.pathInfo());
        });
        
        after((request, response) -> {
            System.out.println("📤 " + request.requestMethod() + " " + request.pathInfo() + 
                             " - Status: " + response.status());
        });
    }
}
