package org.example.routes;

import Dao.InfrazioneVelocitaDao;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.utils.RispostaErrore;
import org.example.utils.RispostaSuccesso;

import static spark.Spark.*;

public class InfrazioneVelocitaRoutes {

    private static final Gson gson = new Gson();

    public static void register() {

        // POST /infrazione/velocita
        post("/infrazione/velocita", (req, res) -> {
            res.type("application/json");
            try {
                JsonObject json = JsonParser.parseString(req.body()).getAsJsonObject();

                if (!json.has("targa") || json.get("targa").getAsString().isBlank()) {
                    res.status(400);
                    return gson.toJson(new RispostaErrore("Targa mancante"));
                }
                if (!json.has("velocitaRilevata") || !json.has("velocitaMassima")) {
                    res.status(400);
                    return gson.toJson(new RispostaErrore("Dati velocità mancanti"));
                }

                int v = json.get("velocitaRilevata").getAsInt();
                int vmax = json.get("velocitaMassima").getAsInt();
                if (v <= vmax) {
                    res.status(400);
                    return gson.toJson(new RispostaErrore("Non è un'infrazione: velocità entro limite"));
                }

                InfrazioneVelocitaDao.salva(json);
                res.status(200);
                return gson.toJson(new RispostaSuccesso("Infrazione velocità registrata"));

            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(new RispostaErrore("Errore interno server"));
            }
        });

        // ✅ PRIMA: route specifica (così non viene “mangiata” da :targa)
        // GET /infrazione/velocita/casello?cap=MI&idCasello=1&direzione=INGRESSO
        get("/infrazione/velocita/casello", (req, res) -> {
            res.type("application/json");

            System.out.println("[DEBUG] >>> GET /infrazione/velocita/casello");

            String cap = req.queryParams("cap");
            String idStr = req.queryParams("idCasello");
            String dir = req.queryParams("direzione");

            System.out.printf("[DEBUG] Parametri: cap='%s' idCasello='%s' direzione='%s'%n", cap, idStr, dir);

            if (cap == null || cap.isBlank() || idStr == null || idStr.isBlank() || dir == null || dir.isBlank()) {
                res.status(400);
                return gson.toJson(new RispostaErrore("Parametri richiesti: cap, idCasello, direzione"));
            }

            int idCasello;
            try {
                idCasello = Integer.parseInt(idStr.trim());
            } catch (Exception e) {
                res.status(400);
                return gson.toJson(new RispostaErrore("Parametro 'idCasello' non valido"));
            }

            var lista = InfrazioneVelocitaDao.tuttePerCasello(cap.trim(), idCasello, dir.trim());

            System.out.println("[DEBUG] Risultati DAO: " + lista.size());

            // ✅ Meglio 200 sempre (il frontend gestisce lista vuota senza “error state”)
            res.status(200);
            return gson.toJson(lista);
        });

        // DOPO: route parametrica generica
        // GET /infrazione/velocita/:targa
        get("/infrazione/velocita/:targa", (req, res) -> {
            res.type("application/json");

            String targa = req.params(":targa");

            JsonObject last = InfrazioneVelocitaDao.ultimaPerTarga(targa);
            if (last == null) {
                res.status(404);
                return gson.toJson(new RispostaErrore("Nessuna infrazione trovata"));
            }

            res.status(200);
            return gson.toJson(last);
        });
    }
}