package org.example.config;

import static spark.Spark.*;

public class ServerConfig {

    private static final int PORT = 4567;
    private static final String KEYSTORE_PATH = "keystore.jks";
    private static final String KEYSTORE_PASSWORD = "password";

    public static void configure() {
        // Porta del server
        port(PORT);

        // Configura HTTPS con keystore
        secure(KEYSTORE_PATH, KEYSTORE_PASSWORD, null, null);

        System.out.println("🔒 Server configurato per HTTPS sulla porta " + PORT);
        System.out.println("🔑 Keystore utilizzato: " + KEYSTORE_PATH);
    }

    public static void logStartup() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🚀 Server HTTPS avviato sulla porta " + PORT);
        System.out.println("🔒 Base URL: https://localhost:" + PORT);
        System.out.println("=".repeat(60));

        System.out.println("\n📡 Endpoints disponibili:");

        System.out.println("\n--- Autenticazione ---");
        System.out.println("   POST   /login                  - Login utente");
        System.out.println("   POST   /crea_utente            - Crea nuovo utente");
        System.out.println("   GET    /sessione               - Verifica sessione");
        System.out.println("   GET    /logout                 - Logout");

        System.out.println("\n--- Tratte ---");
        System.out.println("   GET    /tratte                 - Lista tutte le tratte");
        System.out.println("   GET    /tratta/:id             - Dettaglio tratta");
        System.out.println("   PUT    /tratte/:id             - Modifica tratta");
        System.out.println("   PATCH  /tratta/:id/modificaPrezzo    - Modifica prezzo");
        System.out.println("   PATCH  /tratta/:id/modificaVelocita  - Modifica velocità");

        System.out.println("\n--- Caselli ---");
        System.out.println("   GET    /caselli                - Lista tutti i caselli");
        System.out.println("   GET    /caselli/:cap/:id/:dir  - Dettaglio casello");
        System.out.println("   POST   /aggiungiCasello        - Aggiungi nuovo casello");
        System.out.println("   PATCH  /caselli/:cap/:id/:dir/stato  - Modifica stato");
        System.out.println("   DELETE /caselli/:cap/:id/:dir/elimina - Elimina casello");

        System.out.println("\n--- Biglietti ---");
        System.out.println("   GET    /biglietti              - Lista biglietti");
        System.out.println("   GET    /biglietti_pagati       - Lista biglietti pagati");
        System.out.println("   GET    /veicoli/caselli/:cap/:id/:dir - Biglietti per casello");

        System.out.println("\n--- MQTT (Automazione) ---");
        System.out.println("   POST   /ingresso               - Registra ingresso veicolo");
        System.out.println("   POST   /uscita                 - Registra uscita veicolo");
        System.out.println("   PUT    /api/caselloGuasto      - Registra guasto casello");

        System.out.println("\n--- Pagamenti ---");
        System.out.println("   POST   /pagamento              - Registra pagamento pedaggio");

        System.out.println("\n--- Evasioni ---");
        System.out.println("   POST   /evasione               - Registra evasione pagamento");

        System.out.println("\n--- Infrazioni Velocità ---");
        System.out.println("   POST   /infrazione/velocita    - Registra infrazione per eccesso di velocità");
        System.out.println("   GET    /infrazione/velocita/:targa - Ultima infrazione per targa");

        System.out.println("\n--- Utente ---");
        System.out.println("   GET    /profilo                - Visualizza profilo");
        System.out.println("   POST   /modifica_profilo       - Modifica profilo");

        System.out.println("\n--- Incidenti ---");
        System.out.println("   POST   /incidente/:trattaId    - Registra incidente");
        System.out.println("   GET    /incidente/:trattaId    - Leggi incidente");

        System.out.println("\n" + "=".repeat(60) + "\n");
    }
}