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

                int v    = json.get("velocitaRilevata").getAsInt();
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

        // GET /infrazione/velocita/:targa  →  ultima infrazione per targa
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

        // GET /infrazione/velocita/tratta/:idTratta  →  tutte le infrazioni di una tratta
        get("/infrazione/velocita/tratta/:idTratta", (req, res) -> {
            res.type("application/json");

            try {
                int idTratta = Integer.parseInt(req.params(":idTratta"));
                var lista = InfrazioneVelocitaDao.tuttePerTratta(idTratta);

                if (lista.isEmpty()) {
                    res.status(404);
                    return gson.toJson(new RispostaErrore("Nessuna infrazione trovata per questa tratta"));
                }

                res.status(200);
                return gson.toJson(lista);

            } catch (NumberFormatException e) {
                res.status(400);
                return gson.toJson(new RispostaErrore("ID tratta non valido"));
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(new RispostaErrore("Errore interno server"));
            }
        });
    }
}