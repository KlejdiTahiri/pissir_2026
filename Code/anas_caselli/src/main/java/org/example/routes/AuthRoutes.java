package org.example.routes;

import Dao.UtenteDAO;
import Model.Utente;
import com.google.gson.Gson;
import spark.Session;

import java.util.Map;

import static spark.Spark.*;

public class AuthRoutes {
    
    private static final Gson gson = new Gson();
    
    public static void register() {
        
        // POST /login - Login utente
        post("/login", (req, res) -> {
            Map<String, String> credentials = gson.fromJson(req.body(), Map.class);
            
            String username = credentials.get("username");
            String password = credentials.get("password");
            
            System.out.println("➡️ Tentativo login per username: " + username);
            
            if (username == null || password == null) {
                System.out.println("❌ Username o password mancanti");
                res.status(400);
                return "Username o password mancanti";
            }
            
            // Usa direttamente il metodo login del DAO
            Utente utente = UtenteDAO.login(username, password);
            
            if (utente != null) {
                System.out.println("✅ Login riuscito per: " + username);
                
                // Salva nella sessione
                Session session = req.session(true);
                session.attribute("utente", utente);
                session.attribute("username", username);
                
                res.status(200);
                res.type("application/json");
                return gson.toJson(Map.of(
                    "message", "Login ok", 
                    "ruolo", utente.getRuolo()
                ));
            } else {
                System.out.println("❌ Credenziali non valide per: " + username);
                res.status(401);
                return "Credenziali non valide";
            }
        });
        
        // POST /crea_utente - Crea nuovo utente
        post("/crea_utente", (req, res) -> {
            Map<String, String> body = gson.fromJson(req.body(), Map.class);
            
            String username = body.get("username");
            String password = body.get("password");
            String ruolo = body.get("ruolo");
            
            System.out.println("➡️ Richiesta creazione nuovo utente: " + username);
            
            if (username == null || password == null || ruolo == null) {
                res.status(400);
                return "Missing fields";
            }
            
            // Controlla se esiste già
            if (UtenteDAO.getByUsername(username) != null) {
                res.status(409);
                return "Username già esistente";
            }
            
            // NON hashare qui, lascia al DAO
            boolean ok = UtenteDAO.createUser(username, password.trim(), ruolo);
            
            if (ok) {
                res.status(200);
                return "success";
            } else {
                res.status(500);
                return "Errore server";
            }
        });
        
        // GET /sessione - Verifica stato sessione
        get("/sessione", (req, res) -> {
            res.type("application/json");
            String username = req.session().attribute("username");
            
            if (username != null) {
                return gson.toJson(Map.of(
                        "logged", true,
                        "username", username
                ));
            } else {
                return gson.toJson(Map.of("logged", false));
            }
        });
        
        // GET /logout - Logout utente
        get("/logout", (req, res) -> {
            req.session().invalidate(); // Distrugge la sessione
            res.status(200);
            return gson.toJson(Map.of("message", "Logout effettuato"));
        });
    }
}
