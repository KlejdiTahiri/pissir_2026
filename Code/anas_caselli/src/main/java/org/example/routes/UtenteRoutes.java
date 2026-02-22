package org.example.routes;

import Dao.UtenteDAO;
import Model.Utente;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.utils.RispostaErrore;
import org.mindrot.jbcrypt.BCrypt;

import static spark.Spark.*;

public class UtenteRoutes {
    
    private static final Gson gson = new Gson();
    
    public static void register() {
        
        // GET /profilo - Visualizza profilo utente
        get("/profilo", (req, res) -> {
            res.type("application/json");
            
            String username = req.queryParams("username");
            
            if (username == null || username.isEmpty()) {
                res.status(400);
                return gson.toJson(new RispostaErrore("Username mancante"));
            }
            
            Utente utente = UtenteDAO.getByUsername(username);
            
            if (utente == null) {
                res.status(404);
                return gson.toJson(new RispostaErrore("Utente non trovato"));
            }
            
            return gson.toJson(utente);
        });
        
        // POST /modifica_profilo - Modifica dati profilo
        post("/modifica_profilo", (req, res) -> {
            res.type("application/json");
            
            try {
                JsonObject json = JsonParser.parseString(req.body()).getAsJsonObject();
                
                // Username originale dalla sessione
                String oldUsername = req.session().attribute("username");
                if (oldUsername == null) {
                    res.status(401);
                    return gson.toJson(new RispostaErrore("Utente non autenticato"));
                }
                
                String newUsername = json.get("username").getAsString();
                String newPassword = json.get("password").getAsString();
                
                String hashedPassword = null;
                
                if (newPassword != null && !newPassword.isEmpty()) {
                    hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
                }
                
                boolean success = UtenteDAO.modificaDatiUtente(oldUsername, newUsername, hashedPassword);
                
                if (success) {
                    // Aggiorna la sessione con il nuovo username
                    if (!newUsername.equals(oldUsername)) {
                        req.session().attribute("username", newUsername);
                    }
                    
                    return "success";
                } else {
                    res.status(400);
                    return gson.toJson(new RispostaErrore("Errore durante la modifica del profilo"));
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(new RispostaErrore("Errore interno del server"));
            }
        });
    }
}
