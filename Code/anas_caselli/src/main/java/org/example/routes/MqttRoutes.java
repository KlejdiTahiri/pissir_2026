package org.example.routes;

import Dao.BigliettiDao;
import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.util.Map;

import static Dao.CaselloDao.aggiornaStatoCasello;
import static Dao.TrattaDao.cambiaStatoTratta;
import static spark.Spark.*;

public class MqttRoutes {
    
    private static final Gson gson = new Gson();
    
    public static void register() {
        
        // POST /ingresso - Registra ingresso veicolo via MQTT
        post("/ingresso", (req, res) -> {
            try {
                String targa = req.queryParams("targa");
                String cap = req.queryParams("cap");
                String idCaselloStr = req.queryParams("idCasello");
                String direzione = req.queryParams("direzione");
                
                System.out.println("✅ Registrazione ingresso ricevuta via MQTT:");
                System.out.println("   Targa: " + targa);
                System.out.println("   CAP: " + cap);
                System.out.println("   ID Casello: " + idCaselloStr);
                System.out.println("   Direzione: " + direzione);
                
                // Validazione parametri
                if (targa == null || cap == null || idCaselloStr == null || direzione == null) {
                    System.out.println("❌ Parametri mancanti per registrazione ingresso");
                    res.status(400);
                    return "Parametri mancanti (targa, cap, idCasello, direzione richiesti)";
                }
                
                int idCasello;
                try {
                    idCasello = Integer.parseInt(idCaselloStr);
                } catch (NumberFormatException e) {
                    System.out.println("❌ ID Casello non valido: " + idCaselloStr);
                    res.status(400);
                    return "ID Casello non valido";
                }
                
                // Registra l'ingresso nel database
                boolean success = BigliettiDao.registraIngresso(targa, cap, idCasello, direzione);
                
                if (success) {
                    System.out.println("✅ Ingresso registrato con successo per targa: " + targa);
                    res.status(201);
                    res.type("application/json");
                    return gson.toJson(Map.of(
                            "message", "Ingresso registrato con successo",
                            "targa", targa,
                            "timestamp", LocalDateTime.now().toString()
                    ));
                } else {
                    System.out.println("❌ Errore durante la registrazione dell'ingresso per targa: " + targa);
                    res.status(500);
                    return "Errore durante la registrazione dell'ingresso";
                }
                
            } catch (Exception e) {
                System.out.println("❌ Errore interno durante registrazione ingresso: " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                return "Errore interno del server: " + e.getMessage();
            }
        });
        
        // POST /uscita - Registra uscita veicolo via MQTT
        post("/uscita", (req, res) -> {
            try {
                String targa = req.queryParams("targa");
                String cap = req.queryParams("cap");
                String idCaselloStr = req.queryParams("idCasello");
                String direzione = req.queryParams("direzione");
                String distanzaStr = req.queryParams("distanza");
                String idBigliettoStr = req.queryParams("idBiglietto");
                
                System.out.println("🚪 Registrazione uscita ricevuta via MQTT:");
                System.out.println("   Targa: " + targa);
                System.out.println("   CAP: " + cap);
                System.out.println("   ID Casello: " + idCaselloStr);
                System.out.println("   Direzione: " + direzione);
                System.out.println("   Distanza: " + distanzaStr);
                System.out.println("   ID Biglietto: " + idBigliettoStr);
                
                // Validazione ID Biglietto
                if (idBigliettoStr == null) {
                    System.out.println("❌ idBiglietto mancante");
                    res.status(400);
                    return "idBiglietto mancante";
                }
                
                int idBiglietto;
                try {
                    idBiglietto = Integer.parseInt(idBigliettoStr);
                    System.out.println("idBiglietto : " + idBiglietto);
                } catch (NumberFormatException e) {
                    System.out.println("❌ idBiglietto non valido: " + idBigliettoStr);
                    res.status(400);
                    return "idBiglietto non valido";
                }
                
                // Validazione altri parametri
                if (targa == null || cap == null || idCaselloStr == null || direzione == null || distanzaStr == null) {
                    System.out.println("❌ Parametri mancanti per registrazione uscita");
                    res.status(400);
                    return "Parametri mancanti (targa, cap, idCasello, direzione, distanza richiesti)";
                }
                
                int idCasello;
                int distanza;
                try {
                    idCasello = Integer.parseInt(idCaselloStr);
                    distanza = Integer.parseInt(distanzaStr);
                } catch (NumberFormatException e) {
                    System.out.println("❌ Parametri numerici non validi");
                    res.status(400);
                    return "ID Casello o distanza non validi";
                }
                
                // Registra l'uscita nel database
                boolean success = BigliettiDao.registraUscita(idBiglietto, targa, cap, idCasello, direzione, distanza);
                
                if (success) {
                    System.out.println("✅ Uscita registrata con successo per targa: " + targa);
                    res.status(200);
                    res.type("application/json");
                    return gson.toJson(Map.of(
                            "message", "Uscita registrata con successo",
                            "targa", targa,
                            "timestamp", LocalDateTime.now().toString()
                    ));
                } else {
                    System.out.println("❌ Errore durante la registrazione dell'uscita per targa: " + targa);
                    res.status(500);
                    return "Errore durante la registrazione dell'uscita";
                }
                
            } catch (Exception e) {
                System.out.println("❌ Errore interno durante registrazione uscita: " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                return "Errore interno del server: " + e.getMessage();
            }
        });
        
        // PUT /api/caselloGuasto - Registra guasto casello via MQTT
        put("/api/caselloGuasto", (req, res) -> {
            try {
                String cap = req.queryParams("cap");
                String idCaselloStr = req.queryParams("idCasello");
                String direzione = req.queryParams("direzione");
                
                System.out.println("⚠️ Segnalazione guasto casello ricevuta via MQTT:");
                System.out.println("   CAP: " + cap);
                System.out.println("   ID Casello: " + idCaselloStr);
                System.out.println("   Direzione: " + direzione);
                
                // Validazione parametri
                if (cap == null || idCaselloStr == null || direzione == null) {
                    System.out.println("❌ Parametri mancanti per segnalazione guasto");
                    res.status(400);
                    return "Parametri mancanti (cap, idCasello, direzione richiesti)";
                }
                
                int idCasello;
                try {
                    idCasello = Integer.parseInt(idCaselloStr);
                } catch (NumberFormatException e) {
                    System.out.println("❌ ID Casello non valido: " + idCaselloStr);
                    res.status(400);
                    return "ID Casello non valido";
                }
                
                // Imposta lo stato del casello come guasto (false = non funzionante)
                boolean success = aggiornaStatoCasello(cap, idCasello, direzione, false);
                
                if (success) {
                    // Aggiorna anche lo stato della tratta
                    cambiaStatoTratta(cap, idCasello, cap, idCasello, false);
                    
                    System.out.println("✅ Stato casello aggiornato per guasto: " + cap + "-" + idCasello + "-" + direzione);
                    res.status(200);
                    res.type("application/json");
                    return gson.toJson(Map.of(
                            "message", "Guasto casello registrato con successo",
                            "cap", cap,
                            "idCasello", idCasello,
                            "direzione", direzione,
                            "timestamp", LocalDateTime.now().toString()
                    ));
                } else {
                    System.out.println("❌ Errore durante l'aggiornamento dello stato per guasto casello");
                    res.status(500);
                    return "Errore durante la registrazione del guasto";
                }
                
            } catch (Exception e) {
                System.out.println("❌ Errore interno durante registrazione guasto: " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                return "Errore interno del server: " + e.getMessage();
            }
        });
    }
}
