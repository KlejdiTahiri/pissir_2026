package org.example.routes;

import Dao.PagamentoDao;
import com.google.gson.Gson;
import org.example.utils.RispostaErrore;

import java.util.List;

import static spark.Spark.*;

public class PagamentoRoutes {

    private static final Gson gson = new Gson();

    public static void register() {

        // Inserimento pagamento
        post("/pagamento", (req, res) -> {
            res.type("application/json");

            String targa = req.queryParams("targa");
            String importoStr = req.queryParams("importo");
            String metodo = req.queryParams("metodo"); // CONTANTI / TELEPASS / EVASIONE
            String statoStr = req.queryParams("statoPagamento"); // 1/0
            String cap = req.queryParams("cap");
            String idCaselloStr = req.queryParams("idCasello");
            String direzione = req.queryParams("direzione");

            // Validazioni minime
            if (targa == null || targa.isBlank()) {
                res.status(400);
                return gson.toJson(err("targa mancante"));
            }
            if (importoStr == null || importoStr.isBlank()) {
                res.status(400);
                return gson.toJson(err("importo mancante"));
            }
            if (metodo == null || metodo.isBlank()) {
                res.status(400);
                return gson.toJson(err("metodo mancante"));
            }
            if (statoStr == null || statoStr.isBlank()) {
                res.status(400);
                return gson.toJson(err("statoPagamento mancante"));
            }
            if (cap == null || cap.isBlank() || idCaselloStr == null || idCaselloStr.isBlank() ||
                    direzione == null || direzione.isBlank()) {
                res.status(400);
                return gson.toJson(err("cap/idCasello/direzione mancanti"));
            }

            double importo;
            int statoPagamento;
            int idCasello;
            try {
                importo = Double.parseDouble(importoStr);
                statoPagamento = Integer.parseInt(statoStr);
                idCasello = Integer.parseInt(idCaselloStr);
            } catch (NumberFormatException e) {
                res.status(400);
                return gson.toJson(err("formato numerico non valido (importo/statoPagamento/idCasello)"));
            }

            if (statoPagamento != 0 && statoPagamento != 1) {
                res.status(400);
                return gson.toJson(err("statoPagamento deve essere 0 o 1"));
            }

            int id = PagamentoDao.inserisciPagamento(
                    targa, importo, metodo, statoPagamento, idCasello, cap, direzione
            );

            if (id < 0) {
                res.status(500);
                return gson.toJson(err("inserimento pagamento fallito"));
            }

            res.status(201);
            return gson.toJson(ok("pagamento inserito", id));
        });

        // (Opzionale) Lista pagamenti per targa
        get("/pagamenti", (req, res) -> {
            res.type("application/json");

            String targa = req.queryParams("targa");
            if (targa == null || targa.isBlank()) {
                res.status(400);
                return gson.toJson(err("targa mancante"));
            }

            return gson.toJson(PagamentoDao.getPagamentiByTarga(targa));
        });

        // (Opzionale) Ultimi pagamenti
        get("/pagamenti/recenti", (req, res) -> {
            res.type("application/json");
            String limitStr = req.queryParams("limit");
            int limit = 20;
            if (limitStr != null && !limitStr.isBlank()) {
                try { limit = Integer.parseInt(limitStr); } catch (NumberFormatException ignored) {}
            }
            return gson.toJson(PagamentoDao.getPagamentiRecenti(limit));
        });


        // Aggiunge alla classe che registra le route (es. BigliettoRoutes.java o simile)

// GET /biglietti_pagati
// Parametri query: capOut, idCaselloOut, dirOut
// Restituisce la lista dei pagamenti del casello di uscita specificato
        get("/biglietti_pagati", (req, res) -> {
            res.type("application/json");
            System.out.println("[DEBUG] Richiesta GET /biglietti_pagati ricevuta");

            String capOut        = req.queryParams("capOut");
            String idCaselloStr  = req.queryParams("idCaselloOut");
            String dirOut        = req.queryParams("dirOut");

            // ── Validazione parametri obbligatori ──
            if (capOut == null || capOut.isBlank()) {
                res.status(400);
                return gson.toJson(new RispostaErrore("Parametro 'capOut' mancante"));
            }
            if (dirOut == null || dirOut.isBlank()) {
                res.status(400);
                return gson.toJson(new RispostaErrore("Parametro 'dirOut' mancante"));
            }

            int idCaselloOut;
            try {
                idCaselloOut = Integer.parseInt(idCaselloStr);
            } catch (NumberFormatException e) {
                System.err.println("[ERROR] Parametro 'idCaselloOut' non valido: " + idCaselloStr);
                res.status(400);
                return gson.toJson(new RispostaErrore("Parametro 'idCaselloOut' non valido"));
            }

            System.out.printf("[DEBUG] Fetch pagamenti → capOut=%s | idCaselloOut=%d | dirOut=%s%n",
                    capOut, idCaselloOut, dirOut);

            // ── Query al DAO ──
            List<PagamentoDao.PagamentoRecord> pagamenti =
                    PagamentoDao.getPagamentiPerUscita(capOut, idCaselloOut, dirOut);

            System.out.println("[DEBUG] Pagamenti trovati: " + pagamenti.size());

            res.status(200);
            return gson.toJson(pagamenti);
        });
    }




    private static Object err(String msg) {
        return new ApiResponse(false, msg, null);
    }

    private static Object ok(String msg, Integer id) {
        return new ApiResponse(true, msg, id);
    }

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