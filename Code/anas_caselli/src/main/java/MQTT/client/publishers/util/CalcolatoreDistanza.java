package MQTT.client.publishers.util;


import java.util.HashMap;
import java.util.Map;

public class CalcolatoreDistanza {

    // Mappa con i tempi di percorrenza stimati in ore (es. "MI->RM" = 6 ore)
    public static final Map<String, Integer> TEMPI_PERCORRENZA_ORE = new HashMap<>();

    static {
        // Popola la mappa con i dati noti
        TEMPI_PERCORRENZA_ORE.put("MI->FI", 3);
        TEMPI_PERCORRENZA_ORE.put("MI->NA", 10);
        TEMPI_PERCORRENZA_ORE.put("MI->TO", 2);
        TEMPI_PERCORRENZA_ORE.put("MI->RM", 6);
        TEMPI_PERCORRENZA_ORE.put("MI->AL", 1);
        TEMPI_PERCORRENZA_ORE.put("MI->BA", 9);

// Firenze
        TEMPI_PERCORRENZA_ORE.put("FI->MI", 3);
        TEMPI_PERCORRENZA_ORE.put("FI->NA", 5);
        TEMPI_PERCORRENZA_ORE.put("FI->TO", 3);
        TEMPI_PERCORRENZA_ORE.put("FI->RM", 3);
        TEMPI_PERCORRENZA_ORE.put("FI->AL", 3);
        TEMPI_PERCORRENZA_ORE.put("FI->BA", 7);

// Napoli
        TEMPI_PERCORRENZA_ORE.put("NA->MI", 10);
        TEMPI_PERCORRENZA_ORE.put("NA->FI", 5);
        TEMPI_PERCORRENZA_ORE.put("NA->TO", 11);
        TEMPI_PERCORRENZA_ORE.put("NA->RM", 2);
        TEMPI_PERCORRENZA_ORE.put("NA->AL", 9);
        TEMPI_PERCORRENZA_ORE.put("NA->BA", 3);

// Torino
        TEMPI_PERCORRENZA_ORE.put("TO->MI", 2);
        TEMPI_PERCORRENZA_ORE.put("TO->FI", 3);
        TEMPI_PERCORRENZA_ORE.put("TO->NA", 11);
        TEMPI_PERCORRENZA_ORE.put("TO->RM", 8);
        TEMPI_PERCORRENZA_ORE.put("TO->AL", 2);
        TEMPI_PERCORRENZA_ORE.put("TO->BA", 10);

// Roma
        TEMPI_PERCORRENZA_ORE.put("RM->MI", 6);
        TEMPI_PERCORRENZA_ORE.put("RM->FI", 3);
        TEMPI_PERCORRENZA_ORE.put("RM->NA", 2);
        TEMPI_PERCORRENZA_ORE.put("RM->TO", 8);
        TEMPI_PERCORRENZA_ORE.put("RM->AL", 7);
        TEMPI_PERCORRENZA_ORE.put("RM->BA", 4);

// Alessandria
        TEMPI_PERCORRENZA_ORE.put("AL->MI", 1);
        TEMPI_PERCORRENZA_ORE.put("AL->FI", 3);
        TEMPI_PERCORRENZA_ORE.put("AL->NA", 9);
        TEMPI_PERCORRENZA_ORE.put("AL->TO", 2);
        TEMPI_PERCORRENZA_ORE.put("AL->RM", 7);
        TEMPI_PERCORRENZA_ORE.put("AL->BA", 8);

// Bari
        TEMPI_PERCORRENZA_ORE.put("BA->MI", 9);
        TEMPI_PERCORRENZA_ORE.put("BA->FI", 7);
        TEMPI_PERCORRENZA_ORE.put("BA->NA", 3);
        TEMPI_PERCORRENZA_ORE.put("BA->TO", 10);
        TEMPI_PERCORRENZA_ORE.put("BA->RM", 4);
        TEMPI_PERCORRENZA_ORE.put("BA->AL", 8);
        // Aggiungi altri percorsi se necessario
    }

    public static int calcolaDistanza(String capEntrata, int idCaselloEntrata, String direzioneEntrata,
                                      String capUscita, int idCaselloUscita, String direzioneUscita) {

        // Caso 1: Stesso casello (entrata = uscita)
        if (capEntrata.equals(capUscita) && idCaselloEntrata == idCaselloUscita) {
            System.out.println("   ⚠️  Stesso casello di entrata e uscita: 0 km");
            return 0;
        }

        // Caso 2: Stesso CAP ma caselli diversi (es. MI casello 1 -> MI casello 4)
        if (capEntrata.equals(capUscita) && idCaselloEntrata != idCaselloUscita) {
            // Distanza locale stimata: 20 km per ogni casello di differenza
            int distanzaLocale = Math.abs(idCaselloUscita - idCaselloEntrata) * 20;
            System.out.println("   📍 Stesso CAP, caselli diversi: " + capEntrata +
                    " (casello " + idCaselloEntrata + " -> " + idCaselloUscita + ") = " +
                    distanzaLocale + " km");
            return distanzaLocale;
        }

        // Caso 3: CAP diversi - usa la tabella dei tempi
        String chiave = capEntrata + "->" + capUscita;
        Integer tempoOre = TEMPI_PERCORRENZA_ORE.get(chiave);

        if (tempoOre != null) {
            // Converti ore in chilometri (velocità media 100 km/h)
            int distanzaBase = tempoOre * 100;

            // Aggiungi eventuale distanza intra-casello
            int distanzaIntraCaselloEntrata = (idCaselloEntrata > 1) ? (idCaselloEntrata - 1) * 10 : 0;
            int distanzaIntraCaselloUscita = (idCaselloUscita > 1) ? (idCaselloUscita - 1) * 10 : 0;

            int distanzaTotale = distanzaBase + distanzaIntraCaselloEntrata + distanzaIntraCaselloUscita;

            System.out.println("   🛣️  Percorso " + chiave + ":");
            System.out.println("      - Distanza base: " + tempoOre + " ore × 100 km/h = " + distanzaBase + " km");
            if (distanzaIntraCaselloEntrata > 0) {
                System.out.println("      - Intra-casello entrata: +" + distanzaIntraCaselloEntrata + " km");
            }
            if (distanzaIntraCaselloUscita > 0) {
                System.out.println("      - Intra-casello uscita: +" + distanzaIntraCaselloUscita + " km");
            }
            System.out.println("      - TOTALE: " + distanzaTotale + " km");

            return distanzaTotale;
        }

        // Caso 4: Percorso non trovato - fallback
        System.out.println("   ⚠️  Percorso non mappato: " + chiave);
        System.out.println("      Usando distanza stimata di 150 km");
        return 150;
    }
}

