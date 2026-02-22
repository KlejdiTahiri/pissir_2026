package org.example.routes;

import Dao.PagamentoDao;
import com.google.gson.Gson;

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