package org.example.routes;

import Dao.StoricoPassaggioDao;
import com.google.gson.Gson;
import org.example.utils.RispostaErrore;

import java.util.List;

import static spark.Spark.*;

public class StoricoPassaggioRoutes {

    private static final Gson gson = new Gson();

    public static void register() {

        // POST /storico_passaggio (insert)
        post("/storico_passaggio", (req, res) -> {
            res.type("application/json");

            // Parametri minimi
            String targa = req.queryParams("targa");

            String capIn = req.queryParams("capIn");
            String idInStr = req.queryParams("idCaselloIn");
            String dirIn = req.queryParams("dirIn");

            String capOut = req.queryParams("capOut");
            String idOutStr = req.queryParams("idCaselloOut");
            String dirOut = req.queryParams("dirOut");

            String dataIn = req.queryParams("dataIngresso");
            String dataOut = req.queryParams("dataUscita");

            String kmStr = req.queryParams("chilometri");
            String velStr = req.queryParams("velocitaMedia");

            String telepassStr = req.queryParams("telepass");
            String prezzoStr = req.queryParams("prezzoPagato");
            String metodo = req.queryParams("metodoPagamento");

            // Validazioni “essenziali”
            if (targa == null || targa.isBlank()) {
                res.status(400);
                return gson.toJson(new RispostaErrore("Parametro 'targa' mancante"));
            }
            if (capIn == null || capIn.isBlank() || idInStr == null || idInStr.isBlank() || dirIn == null || dirIn.isBlank()) {
                res.status(400);
                return gson.toJson(new RispostaErrore("Parametri ingresso mancanti (capIn/idCaselloIn/dirIn)"));
            }
            if (capOut == null || capOut.isBlank() || idOutStr == null || idOutStr.isBlank() || dirOut == null || dirOut.isBlank()) {
                res.status(400);
                return gson.toJson(new RispostaErrore("Parametri uscita mancanti (capOut/idCaselloOut/dirOut)"));
            }
            if (dataIn == null || dataIn.isBlank() || dataOut == null || dataOut.isBlank()) {
                res.status(400);
                return gson.toJson(new RispostaErrore("Parametri data mancanti (dataIngresso/dataUscita)"));
            }
            if (kmStr == null || kmStr.isBlank() || velStr == null || velStr.isBlank()) {
                res.status(400);
                return gson.toJson(new RispostaErrore("Parametri distanza mancanti (chilometri/velocitaMedia)"));
            }
            if (telepassStr == null || telepassStr.isBlank() || prezzoStr == null || prezzoStr.isBlank() || metodo == null || metodo.isBlank()) {
                res.status(400);
                return gson.toJson(new RispostaErrore("Parametri pagamento mancanti (telepass/prezzoPagato/metodoPagamento)"));
            }

            int idIn, idOut, km, telepass;
            double vel, prezzo;
            try {
                idIn = Integer.parseInt(idInStr);
                idOut = Integer.parseInt(idOutStr);
                km = Integer.parseInt(kmStr);
                vel = Double.parseDouble(velStr);
                telepass = Integer.parseInt(telepassStr);
                prezzo = Double.parseDouble(prezzoStr);
            } catch (NumberFormatException e) {
                res.status(400);
                return gson.toJson(new RispostaErrore("Formato numerico non valido"));
            }

            StoricoPassaggioDao.StoricoPassaggioRecord s = new StoricoPassaggioDao.StoricoPassaggioRecord();
            s.targa = targa;

            s.capIngresso = capIn;
            s.idCaselloIngresso = idIn;
            s.direzioneIngresso = dirIn;

            s.capUscita = capOut;
            s.idCaselloUscita = idOut;
            s.direzioneUscita = dirOut;

            s.dataIngresso = dataIn;
            s.dataUscita = dataOut;

            s.chilometri = km;
            s.velocitaMedia = vel;

            s.telepass = telepass;
            s.prezzoPagato = prezzo;
            s.metodoPagamento = metodo;

            int newId = StoricoPassaggioDao.inserisciStoricoPassaggio(s);
            if (newId < 0) {
                res.status(500);
                return gson.toJson(new RispostaErrore("Inserimento storico_passaggi fallito"));
            }

            res.status(201);
            return gson.toJson(new ApiResponse(true, "storico inserito", newId));
        });

        // GET /storico_passaggi?targa=...
        get("/storico_passaggi", (req, res) -> {
            res.type("application/json");
            String targa = req.queryParams("targa");
            if (targa == null || targa.isBlank()) {
                res.status(400);
                return gson.toJson(new RispostaErrore("Parametro 'targa' mancante"));
            }
            return gson.toJson(StoricoPassaggioDao.getStoricoByTarga(targa));
        });

        // GET /storico_passaggi/uscita?capOut=...&idCaselloOut=...&dirOut=...
        get("/storico_passaggi/uscita", (req, res) -> {
            res.type("application/json");

            String capOut = req.queryParams("capOut");
            String idOutStr = req.queryParams("idCaselloOut");
            String dirOut = req.queryParams("dirOut");

            if (capOut == null || capOut.isBlank()) {
                res.status(400);
                return gson.toJson(new RispostaErrore("Parametro 'capOut' mancante"));
            }
            if (idOutStr == null || idOutStr.isBlank()) {
                res.status(400);
                return gson.toJson(new RispostaErrore("Parametro 'idCaselloOut' mancante"));
            }
            if (dirOut == null || dirOut.isBlank()) {
                res.status(400);
                return gson.toJson(new RispostaErrore("Parametro 'dirOut' mancante"));
            }

            int idOut;
            try { idOut = Integer.parseInt(idOutStr); }
            catch (NumberFormatException e) {
                res.status(400);
                return gson.toJson(new RispostaErrore("Parametro 'idCaselloOut' non valido"));
            }

            List<StoricoPassaggioDao.StoricoPassaggioRecord> list =
                    StoricoPassaggioDao.getStoricoPerUscita(capOut, idOut, dirOut);

            res.status(200);
            return gson.toJson(list);
        });


        // GET /storico_passaggi/guadagni?capIn=...&idCaselloIn=...&dirIn=...&capOut=...&idCaselloOut=...&dirOut=...
        get("/storico_passaggi/guadagni", (req, res) -> {
            res.type("application/json");

            String capIn = req.queryParams("capIn");
            String idInStr = req.queryParams("idCaselloIn");
            String dirIn = req.queryParams("dirIn");

            String capOut = req.queryParams("capOut");
            String idOutStr = req.queryParams("idCaselloOut");
            String dirOut = req.queryParams("dirOut");

            if (capIn == null || capIn.isBlank() || dirIn == null || dirIn.isBlank()
                    || capOut == null || capOut.isBlank() || dirOut == null || dirOut.isBlank()
                    || idInStr == null || idInStr.isBlank() || idOutStr == null || idOutStr.isBlank()) {
                res.status(400);
                return gson.toJson(new RispostaErrore("Parametri mancanti per calcolo guadagni"));
            }

            int idIn, idOut;
            try {
                idIn = Integer.parseInt(idInStr);
                idOut = Integer.parseInt(idOutStr);
            } catch (NumberFormatException e) {
                res.status(400);
                return gson.toJson(new RispostaErrore("idCaselloIn/idCaselloOut non validi"));
            }

            double totale = Dao.StoricoPassaggioDao.getTotaleGuadagniPerTratta(
                    capIn, idIn, dirIn, capOut, idOut, dirOut
            );

            res.status(200);
            return gson.toJson(new TotaleGuadagniResponse(totale));
        });


    }

    static class TotaleGuadagniResponse {
        double totaleGuadagni;
        TotaleGuadagniResponse(double totaleGuadagni) { this.totaleGuadagni = totaleGuadagni; }
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