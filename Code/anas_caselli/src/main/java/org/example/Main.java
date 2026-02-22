package org.example;

import org.example.config.ServerConfig;
import org.example.middleware.CorsMiddleware;
import org.example.middleware.LoggingMiddleware;
import org.example.routes.*;

import static spark.Spark.*;

public class Main {
    public static void main(String[] args) {
        // 1. Configurazione server
        ServerConfig.configure();

        // 2. Middleware
        CorsMiddleware.enable();
        LoggingMiddleware.enable();

        // 3. Registrazione routes
        registerRoutes();

        // 4. Log avvio
        ServerConfig.logStartup();
    }

    private static void registerRoutes() {
        // Endpoint base
        get("/ping", (req, res) -> "Server OK");

        // Registra tutti i gruppi di routes
        TrattaRoutes.register();
        CaselloRoutes.register();
        BigliettoRoutes.register();
        MqttRoutes.register();
        AuthRoutes.register();
        UtenteRoutes.register();
        IncidenteRoutes.register();
        PagamentoRoutes.register();
        EvasioneRoutes.register();
        InfrazioneVelocitaRoutes.register();
    }
}