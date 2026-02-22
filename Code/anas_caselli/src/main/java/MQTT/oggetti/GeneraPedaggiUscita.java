package MQTT.oggetti;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GeneraPedaggiUscita {

    public static void main(String[] args) {
        String[] capArray = {"MI", "FI", "NA", "TO", "RM", "AL", "BA"};
        String percorsoFileEntrata = "src/main/resources/pedaggi_entrata.txt";
        String percorsoFileUscita = "src/main/resources/pedaggi_uscita.txt";
        Random rand = new Random();
        Map<String, Integer> tempiPercorrenza = new HashMap<>();

        // Milano
        tempiPercorrenza.put("MI->FI", 3);
        tempiPercorrenza.put("MI->NA", 10);
        tempiPercorrenza.put("MI->TO", 2);
        tempiPercorrenza.put("MI->RM", 6);
        tempiPercorrenza.put("MI->AL", 1);
        tempiPercorrenza.put("MI->BA", 9);

        // Firenze
        tempiPercorrenza.put("FI->MI", 3);
        tempiPercorrenza.put("FI->NA", 5);
        tempiPercorrenza.put("FI->TO", 3);
        tempiPercorrenza.put("FI->RM", 3);
        tempiPercorrenza.put("FI->AL", 3);
        tempiPercorrenza.put("FI->BA", 7);

        // Napoli
        tempiPercorrenza.put("NA->MI", 10);
        tempiPercorrenza.put("NA->FI", 5);
        tempiPercorrenza.put("NA->TO", 11);
        tempiPercorrenza.put("NA->RM", 2);
        tempiPercorrenza.put("NA->AL", 9);
        tempiPercorrenza.put("NA->BA", 3);

        // Torino
        tempiPercorrenza.put("TO->MI", 2);
        tempiPercorrenza.put("TO->FI", 3);
        tempiPercorrenza.put("TO->NA", 11);
        tempiPercorrenza.put("TO->RM", 8);
        tempiPercorrenza.put("TO->AL", 2);
        tempiPercorrenza.put("TO->BA", 10);

        // Roma
        tempiPercorrenza.put("RM->MI", 6);
        tempiPercorrenza.put("RM->FI", 3);
        tempiPercorrenza.put("RM->NA", 2);
        tempiPercorrenza.put("RM->TO", 8);
        tempiPercorrenza.put("RM->AL", 7);
        tempiPercorrenza.put("RM->BA", 4);

        // Alessandria
        tempiPercorrenza.put("AL->MI", 1);
        tempiPercorrenza.put("AL->FI", 3);
        tempiPercorrenza.put("AL->NA", 9);
        tempiPercorrenza.put("AL->TO", 2);
        tempiPercorrenza.put("AL->RM", 7);
        tempiPercorrenza.put("AL->BA", 8);

        // Bari
        tempiPercorrenza.put("BA->MI", 9);
        tempiPercorrenza.put("BA->FI", 7);
        tempiPercorrenza.put("BA->NA", 3);
        tempiPercorrenza.put("BA->TO", 10);
        tempiPercorrenza.put("BA->RM", 4);
        tempiPercorrenza.put("BA->AL", 8);

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        try {
            List<String> ingressi = Files.readAllLines(Paths.get(percorsoFileEntrata));

            try (FileWriter writer = new FileWriter(percorsoFileUscita)) {
                int idBiglietto = 1;  // Contatore idBiglietto
                for (String ingresso : ingressi) {
                    String targa = estraiValore(ingresso, "targa");
                    String capIngresso = estraiValore(ingresso, "cap");
                    String tsIngresso = estraiValore(ingresso, "timestamp");

                    // Parse del timestamp ingresso
                    LocalDateTime timestampIngresso = LocalDateTime.parse(tsIngresso, formatter);

                    // CAP uscita scelto casualmente
                    String capUscita = capArray[rand.nextInt(capArray.length)];

                    // Calcolo tempo percorrenza
                    int orePercorrenza;
                    String key = capIngresso + "->" + capUscita;
                    if (tempiPercorrenza.containsKey(key)) {
                        orePercorrenza = tempiPercorrenza.get(key);
                    } else {
                        // default: tempo random tra 1 e 12 ore
                        orePercorrenza = rand.nextInt(12) + 1;
                    }

                    // Timestamp uscita = ingresso + tempo percorrenza
                    LocalDateTime timestampUscita = timestampIngresso.plusHours(orePercorrenza);

                    int idCasello = rand.nextInt(2) + 3; // 3 o 4
                    String direzione = "USCITA";

                    String json = String.format("""
                    {
                        "idBiglietto": %d,
                        "targa": "%s",
                        "cap": "%s",
                        "idCasello": %d,
                        "direzione": "%s",
                        "timestamp": "%s"
                    }
                    """, idBiglietto, targa, capUscita, idCasello, direzione, timestampUscita);

                    writer.write(json.replace("\n", "").replace("    ", "").trim() + "\n");
                    idBiglietto++;  // Incrementa il contatore
                }
            }

            System.out.println("✅ File pedaggi_uscita.txt generato in: " + percorsoFileUscita);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String estraiValore(String json, String chiave) {
        String cerca = "\"" + chiave + "\":";
        int start = json.indexOf(cerca);
        if (start == -1) return "";
        start = json.indexOf("\"", start + cerca.length()) + 1;
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
}
