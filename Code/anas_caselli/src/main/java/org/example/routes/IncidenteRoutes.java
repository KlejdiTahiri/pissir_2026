package org.example.routes;

import Dao.IncidenteDao;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.utils.RispostaErrore;
import org.example.utils.RispostaSuccesso;

import static spark.Spark.*;

public class IncidenteRoutes {
    
    private static final Gson gson = new Gson();
    
    public static void register() {
        
        // POST /incidente/:trattaId - Registra nuovo incidente
        post("/incidente/:trattaId", (req, res) -> {
            res.type("application/json");
            
            try {
                int trattaId = Integer.parseInt(req.params(":trattaId"));
                
                System.out.println("🚨 Richiesta POST incidente per trattaId: " + trattaId);
                System.out.println("📦 Body: " + req.body());

                JsonObject json = JsonParser.parseString(req.body()).getAsJsonObject();
                json.addProperty("idTratta", trattaId); // ✅ forza coerenza path/body
                IncidenteDao.salvaIncidente(json);
                
                System.out.println("✅ Incidente salvato con successo per trattaId: " + trattaId);
                
                res.status(200);
                return gson.toJson(new RispostaSuccesso("Incidente registrato correttamente"));
                
            } catch (NumberFormatException nfe) {
                System.out.println("❌ Parametro trattaId non valido: " + req.params(":trattaId"));
                res.status(400);
                return gson.toJson(new RispostaErrore("Parametro trattaId non valido"));
            } catch (Exception e) {
                System.out.println("❌ Errore durante il salvataggio dell'incidente");
                e.printStackTrace();
                res.status(500);
                return gson.toJson(new RispostaErrore("Errore interno del server: " + e.getMessage()));
            }
        });
        
        // GET /incidente/:trattaId - Leggi incidente per tratta
        get("/incidente/:trattaId", (req, res) -> {
            res.type("application/json");
            
            try {
                String trattaIdParam = req.params(":trattaId");
                System.out.println("📥 Ricevuta richiesta GET incidente per trattaId: " + trattaIdParam);
                
                int trattaId = Integer.parseInt(trattaIdParam);
                JsonObject incidente = IncidenteDao.leggiIncidente(trattaId);
                
                if (incidente != null) {
                    System.out.println("✅ Incidente trovato per trattaId " + trattaId + ": " + incidente.toString());
                    res.status(200);
                    return gson.toJson(incidente);
                } else {
                    System.out.println("⚠️ Nessun incidente trovato per trattaId " + trattaId);
                    res.status(404);
                    return gson.toJson(new RispostaErrore("Nessun incidente trovato per la tratta"));
                }
                
            } catch (NumberFormatException nfe) {
                System.out.println("❌ Parametro trattaId non valido: " + req.params(":trattaId"));
                res.status(400);
                return gson.toJson(new RispostaErrore("Parametro trattaId non valido"));
            } catch (Exception e) {
                System.out.println("❌ Errore interno durante la lettura dell'incidente");
                e.printStackTrace();
                res.status(500);
                return gson.toJson(new RispostaErrore("Errore interno del server"));
            }
        });
    }
}
