package org.example.routes;

import Dao.EvasioneDao;
import com.google.gson.Gson;

import static spark.Spark.*;

public class EvasioneRoutes {

    private static final Gson gson = new Gson();

    public static void register() {

        post("/evasione", (req, res) -> {
            res.type("application/json");

            String idBigliettoStr = req.queryParams("idBiglietto");
            String targa = req.queryParams("targa");
            String importoStr = req.queryParams("importo");
            String metodo = req.queryParams("metodo");
            String distanzaStr = req.queryParams("distanza");

            String capIn = req.queryParams("capIn");
            String idCaselloInStr = req.queryParams("idCaselloIn");
            String direzioneIn = req.queryParams("direzioneIn");

            String capOut = req.queryParams("capOut");
            String idCaselloOutStr = req.queryParams("idCaselloOut");
            String direzioneOut = req.queryParams("direzioneOut");

            String note = req.queryParams("note");

            if (targa == null || targa.isBlank()) {
                res.status(400);
                return gson.toJson(err("targa mancante"));
            }

            Integer idBiglietto = parseIntOrNull(idBigliettoStr);
            Double importo = parseDoubleOrNull(importoStr);
            Integer distanza = parseIntOrNull(distanzaStr);

            Integer idCaselloIn = parseIntOrNull(idCaselloInStr);
            Integer idCaselloOut = parseIntOrNull(idCaselloOutStr);

            if (metodo == null || metodo.isBlank()) metodo = "EVASIONE";

            int id = EvasioneDao.inserisciEvasione(
                    idBiglietto,
                    targa,
                    importo,
                    metodo,
                    distanza,
                    capIn,
                    idCaselloIn,
                    direzioneIn,
                    capOut,
                    idCaselloOut,
                    direzioneOut,
                    note
            );

            if (id < 0) {
                res.status(500);
                return gson.toJson(err("inserimento evasione fallito"));
            }

            res.status(201);
            return gson.toJson(ok("evasione registrata", id));
        });
    }

    private static Integer parseIntOrNull(String s) {
        try { return (s == null || s.isBlank()) ? null : Integer.parseInt(s); }
        catch (Exception e) { return null; }
    }

    private static Double parseDoubleOrNull(String s) {
        try { return (s == null || s.isBlank()) ? null : Double.parseDouble(s); }
        catch (Exception e) { return null; }
    }

    private static Object err(String msg) { return new ApiResponse(false, msg, null); }
    private static Object ok(String msg, Integer id) { return new ApiResponse(true, msg, id); }

    private static class ApiResponse {
        boolean success;
        String message;
        Integer id;
        ApiResponse(boolean success, String message, Integer id) {
            this.success = success;
            this.message = message;
            this.id = id;
        }
    }
}