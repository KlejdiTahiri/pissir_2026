package org.example.routes;

import Dao.TrattaDao;
import Model.Tratta;
import com.google.gson.Gson;

import java.util.Map;

import static spark.Spark.*;

public class TrattaRoutes {
    
    private static final Gson gson = new Gson();
    
    public static void register() {
        
        // GET /tratte - Lista tutte le tratte
        get("/tratte", (req, res) -> {
            res.type("application/json");
            return gson.toJson(TrattaDao.getAllTratte());
        });
        
        // GET /tratta/:id - Dettaglio tratta
        get("/tratta/:id", (req, res) -> {
            int idTratta = Integer.parseInt(req.params(":id"));
            Tratta tratta = TrattaDao.getTrattaById(idTratta);
            
            if (tratta == null) {
                res.status(404);
                return "Tratta non trovata";
            }
            
            res.type("application/json");
            return gson.toJson(tratta);
        });
        
        // PUT /tratte/:id - Modifica tratta completa
        put("/tratte/:id", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            Tratta t = gson.fromJson(req.body(), Tratta.class);
            t.id = id;
            
            boolean success = TrattaDao.aggiornaTratta(t);
            
            if (success) {
                res.status(200);
                return "Tratta aggiornata con successo";
            } else {
                res.status(500);
                return "Errore durante l'aggiornamento";
            }
        });
        
        // PATCH /tratta/:id/modificaPrezzo - Modifica solo prezzo
        patch("/tratta/:id/modificaPrezzo", (req, res) -> {
            int idTratta = Integer.parseInt(req.params(":id"));
            Map<String, Object> body = gson.fromJson(req.body(), Map.class);
            
            if (!body.containsKey("prezzo")) {
                res.status(400);
                return "Prezzo mancante";
            }
            
            double nuovoPrezzo = Double.parseDouble(body.get("prezzo").toString());
            Tratta trattaAggiornata = TrattaDao.modificaPrezzoTratta(idTratta, nuovoPrezzo);
            
            if (trattaAggiornata != null) {
                res.status(200);
                res.type("application/json");
                return gson.toJson(trattaAggiornata);
            } else {
                res.status(404);
                return "Tratta non trovata";
            }
        });
        
        // PATCH /tratta/:id/modificaVelocita - Modifica solo velocità
        patch("/tratta/:id/modificaVelocita", (req, res) -> {
            int idTratta = Integer.parseInt(req.params(":id"));
            Map<String, Object> body = gson.fromJson(req.body(), Map.class);
            
            if (!body.containsKey("velocitaMax")) {
                res.status(400);
                return "Campo 'velocitaMax' mancante";
            }
            
            double velocitaDouble = (double) body.get("velocitaMax");
            int velocita = (int) velocitaDouble;
            
            Tratta trattaAggiornata = TrattaDao.modificaVelocitaTratta(idTratta, velocita);
            
            if (trattaAggiornata != null) {
                res.status(200);
                res.type("application/json");
                return gson.toJson(trattaAggiornata);
            } else {
                res.status(404);
                return "Tratta non trovata";
            }
        });
        
        // GET /tratta/:id/traffico - Informazioni traffico (placeholder)
        get("/tratta/:id/traffico", (req, res) -> {
            int idTratta = Integer.parseInt(req.params(":id"));
            // TODO: Implementare logica traffico
            res.status(501); // Not Implemented
            return "Funzionalità non ancora implementata";
        });
    }
}
