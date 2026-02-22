package org.example.routes;

import Dao.CaselloDao;
import Model.Casello;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import static Dao.CaselloDao.addNuovoCasello;
import static Dao.CaselloDao.aggiornaStatoCasello;
import static Dao.TrattaDao.cambiaStatoTratta;
import static spark.Spark.*;

public class CaselloRoutes {
    
    private static final Gson gson = new Gson();
    
    public static void register() {
        
        // GET /caselli - Lista tutti i caselli
        get("/caselli", (req, res) -> {
            res.type("application/json");
            return gson.toJson(CaselloDao.getAllCaselli());
        });
        
        // GET /caselli/:cap/:id/:direzione - Dettaglio casello specifico
        get("/caselli/:cap/:id/:direzione", (req, res) -> {
            String cap = req.params(":cap");
            int id = Integer.parseInt(req.params(":id"));
            String direzione = req.params(":direzione");
            
            Casello casello = CaselloDao.getCaselloByKey(cap, id, direzione);
            
            if (casello == null) {
                res.status(404);
                return "Casello non trovato";
            } else {
                res.type("application/json");
                return gson.toJson(casello);
            }
        });
        
        // POST /aggiungiCasello - Aggiungi nuovo casello
        post("/aggiungiCasello", (req, res) -> {
            JsonObject body = gson.fromJson(req.body(), JsonObject.class);
            
            String cap = body.get("cap").getAsString();
            String direzione = body.get("direzione").getAsString();
            
            if (cap == null || direzione == null) {
                res.status(400);
                return "Parametri mancanti";
            }
            
            try {
                boolean success = addNuovoCasello(cap, direzione);
                if (success) {
                    res.status(201);
                    return "Casello aggiunto con successo";
                } else {
                    res.status(500);
                    return "Errore nell'aggiunta del casello";
                }
            } catch (Exception e) {
                res.status(500);
                e.printStackTrace();
                return "Errore interno del server: " + e.getMessage();
            }
        });
        
        // PATCH /caselli/:cap/:id/:direzione/stato - Modifica stato casello
        patch("/caselli/:cap/:id/:direzione/stato", (req, res) -> {
            String cap = req.params(":cap");
            int idCasello = Integer.parseInt(req.params(":id"));
            String direzione = req.params(":direzione");
            
            System.out.println("📥 PATCH /caselli/" + cap + "/" + idCasello + "/" + direzione + "/stato");
            System.out.println("🔧 Corpo richiesta (raw): " + req.body());
            
            JsonObject body = gson.fromJson(req.body(), JsonObject.class);
            
            if (!body.has("stato")) {
                System.out.println("❌ Errore: parametro 'stato' mancante");
                res.status(400);
                return "Parametro 'stato' mancante";
            }
            
            boolean statoRicevuto = body.get("stato").getAsBoolean();
            boolean statoInvertito = !statoRicevuto;
            
            System.out.println("✅ Parametri ricevuti:");
            System.out.println("   CAP: " + cap);
            System.out.println("   ID Casello: " + idCasello);
            System.out.println("   Direzione: " + direzione);
            System.out.println("   Stato ricevuto: " + statoRicevuto);
            System.out.println("   Stato da salvare (invertito): " + statoInvertito);
            
            boolean success = aggiornaStatoCasello(cap, idCasello, direzione, statoInvertito);
            
            if (success) {
                cambiaStatoTratta(cap, idCasello, cap, idCasello, statoInvertito);
                System.out.println("✅ Stato aggiornato con successo nel DB.");
                res.status(200);
                return "Stato casello aggiornato con successo";
            } else {
                System.out.println("❌ Errore durante aggiornamento nel DB.");
                res.status(500);
                return "Errore durante aggiornamento stato";
            }
        });
        
        // DELETE /caselli/:cap/:id/:direzione/elimina - Elimina casello
        delete("/caselli/:cap/:id/:direzione/elimina", (req, res) -> {
            String cap = req.params(":cap");
            int id = Integer.parseInt(req.params(":id"));
            String direzione = req.params(":direzione");
            
            System.out.println("🗑️ Richiesta DELETE casello - CAP: " + cap + ", ID: " + id + ", Direzione: " + direzione);
            
            boolean success = CaselloDao.eliminaCasello(cap, id, direzione);
            
            if (success) {
                cambiaStatoTratta(cap, id, cap, id, false);
                res.status(200);
                return "Casello eliminato con successo";
            } else {
                res.status(404);
                return "Casello non trovato o errore durante l'eliminazione";
            }
        });
    }
}
