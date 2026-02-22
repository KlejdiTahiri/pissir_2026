package org.example.routes;

import Dao.PagamentoDao;
import Model.Biglietto;
import Model.StoricoConPagamento;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.LocalDateTimeAdapter;
import org.example.utils.RispostaErrore;

import java.time.LocalDateTime;
import java.util.List;

import static Dao.BigliettiDao.getAllBigliettiCasello;
import static Dao.BigliettiDao.getStoricoConPagamento;
import static spark.Spark.*;

public class BigliettoRoutes {
    
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    
    public static void register() {
        
        // GET /veicoli/caselli/:cap/:id/:direzione - Biglietti per casello specifico
        get("/veicoli/caselli/:cap/:id/:direzione", (req, res) -> {
            String cap = req.params("cap");
            String direzione = req.params("direzione");
            int idCasello = Integer.parseInt(req.params("id"));
            
            List<Biglietto> biglietti = getAllBigliettiCasello(cap, idCasello, direzione);
            
            // DEBUG: stampa parametri ricevuti
            System.out.println("📥 [DEBUG] Richiesta ricevuta:");
            System.out.println("  CAP = " + cap);
            System.out.println("  ID Casello = " + idCasello);
            System.out.println("  Direzione = " + direzione);
            
            // DEBUG: stampa contenuto lista
            System.out.println("📤 [DEBUG] Biglietti trovati: " + biglietti.size());
            for (Biglietto b : biglietti) {
                System.out.println("    → Targa: " + b.getTarga() +
                        ", CAP ingresso: " + b.getCapIngresso() +
                        ", Direzione: " + b.getDirezioneIngresso());
            }
            
            res.type("application/json");
            return gson.toJson(biglietti);
        });
        
        // GET /biglietti - Lista biglietti con filtri
        get("/biglietti", (req, res) -> {
            System.out.println("[DEBUG] Richiesta GET /biglietti ricevuta");
            
            // Estrai parametri
            String capIn = req.queryParams("capIn");
            String idCaselloStr = req.queryParams("idCasello");
            String dirIn = req.queryParams("dirIn");
            
            System.out.println("[DEBUG] Parametri ricevuti: capIn=" + capIn + 
                             ", idCasello=" + idCaselloStr + ", dirIn=" + dirIn);
            
            int idCasello = 0;
            try {
                idCasello = Integer.parseInt(idCaselloStr);
            } catch (Exception e) {
                System.err.println("[ERROR] Parametro 'idCasello' non valido: " + idCaselloStr);
                res.status(400);
                return "Parametro 'idCasello' non valido";
            }
            
            List<Biglietto> biglietti = getAllBigliettiCasello(capIn, idCasello, dirIn);
            
            System.out.println("[DEBUG] Numero biglietti trovati: " + biglietti.size());
            for (Biglietto b : biglietti) {
                System.out.println(" - ID=" + b.getId()
                        + ", targa=" + b.getTarga()
                        + ", cap=" + b.getCapIngresso()
                        + ", casello=" + b.getIdCaselloIngresso()
                        + ", direzione=" + b.getDirezioneIngresso()
                        + ", data=" + b.getDataIngresso()
                        + ", valido=" + b.getValido());
            }
            
            res.type("application/json");
            return gson.toJson(biglietti);
        });

        get("/biglietti_pagati", (req, res) -> {
            res.type("application/json");

            System.out.println("\n📥 GET /biglietti_pagati");
            System.out.println("[DEBUG] QueryString: " + req.queryString());
            System.out.println("[DEBUG] Remote: " + req.ip() + " | UA: " + req.userAgent());

            String capOut       = req.queryParams("capOut");
            String idCaselloStr = req.queryParams("idCaselloOut");
            String dirOut       = req.queryParams("dirOut");

            System.out.printf("[DEBUG] Parametri ricevuti: capOut=%s, idCaselloOut=%s, dirOut=%s%n",
                    capOut, idCaselloStr, dirOut);

            // Validazione
            if (capOut == null || capOut.isBlank()) {
                System.out.println("[WARN] capOut mancante");
                res.status(400);
                return gson.toJson(new RispostaErrore("Parametro 'capOut' mancante"));
            }
            if (idCaselloStr == null || idCaselloStr.isBlank()) {
                System.out.println("[WARN] idCaselloOut mancante");
                res.status(400);
                return gson.toJson(new RispostaErrore("Parametro 'idCaselloOut' mancante"));
            }
            if (dirOut == null || dirOut.isBlank()) {
                System.out.println("[WARN] dirOut mancante");
                res.status(400);
                return gson.toJson(new RispostaErrore("Parametro 'dirOut' mancante"));
            }

            int idCaselloOut;
            try {
                idCaselloOut = Integer.parseInt(idCaselloStr);
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] idCaselloOut non valido: " + idCaselloStr);
                res.status(400);
                return gson.toJson(new RispostaErrore("Parametro 'idCaselloOut' non valido"));
            }

            System.out.printf("[DEBUG] Fetch pagamenti → capOut=%s | idCaselloOut=%d | dirOut=%s%n",
                    capOut, idCaselloOut, dirOut);

            List<PagamentoDao.PagamentoRecord> pagamenti =
                    PagamentoDao.getPagamentiPerUscita(capOut, idCaselloOut, dirOut);

            System.out.println("[DEBUG] Pagamenti trovati: " + pagamenti.size());

            // Stampa prime righe (max 5) per verifica
            for (int i = 0; i < Math.min(5, pagamenti.size()); i++) {
                PagamentoDao.PagamentoRecord p = pagamenti.get(i);
                System.out.printf(" - ID=%d | targa=%s | data=%s | importo=%.2f | metodo=%s | stato=%d | cap=%s | casello=%d | dir=%s%n",
                        p.id, p.targa, p.data, p.importo, p.metodo, p.statoPagamento, p.cap, p.idCasello, p.direzione);
            }

            String json = gson.toJson(pagamenti);
            System.out.println("[DEBUG] JSON size: " + json.length() + " chars");
            System.out.println("📤 GET /biglietti_pagati - Status: 200");

            res.status(200);
            return json;
        });
    }
}
