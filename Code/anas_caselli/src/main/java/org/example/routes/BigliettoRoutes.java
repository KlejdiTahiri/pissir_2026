package org.example.routes;

import Model.Biglietto;
import Model.StoricoConPagamento;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.LocalDateTimeAdapter;

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
        
        // GET /biglietti_pagati - Lista biglietti con storico pagamento
        get("/biglietti_pagati", (req, res) -> {
            System.out.println("[DEBUG] Richiesta GET /biglietti_pagati ricevuta");
            
            String capIn = req.queryParams("capIn");
            String idCaselloStrIn = req.queryParams("idCaselloIn");
            String dirIn = req.queryParams("dirIn");
            
            String capOut = req.queryParams("capOut");
            String idCaselloStr = req.queryParams("idCaselloOut");
            String dirOut = req.queryParams("dirOut");
            
            int idCasello = 0, idCaselloIngresso = 0;
            try {
                idCasello = Integer.parseInt(idCaselloStr);
                idCaselloIngresso = Integer.parseInt(idCaselloStrIn);
            } catch (Exception e) {
                System.err.println("[ERROR] Parametro 'idCasello' non valido: " + idCaselloStr);
                res.status(400);
                return "Parametro 'idCasello' non valido";
            }
            
            List<StoricoConPagamento> bigliettiPagati = getStoricoConPagamento(
                capIn, idCaselloIngresso, dirIn, 
                capOut, idCasello, dirOut
            );
            
            res.type("application/json");
            return gson.toJson(bigliettiPagati);
        });
    }
}
