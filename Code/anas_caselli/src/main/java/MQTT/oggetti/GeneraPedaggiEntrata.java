package MQTT.oggetti;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Random;

public class GeneraPedaggiEntrata {
    public static void main(String[] args) {
        String[] capArray = {"MI", "FI", "NA", "TO", "RM", "AL", "BA"};
        String[] direzioni = {"INGRESSO"};
        Random rand = new Random();

        String percorsoFile = "src/main/resources/pedaggi_entrata.txt";

        try (FileWriter writer = new FileWriter(percorsoFile)) {
            for (int i = 1; i <= 50; i++) {  // ID da 1 a 20
                int idCasello = rand.nextInt(2) + 1; // 1 o 2
                String cap = capArray[rand.nextInt(capArray.length)];
                String direzione = direzioni[rand.nextInt(direzioni.length)];
                String targa = generaTarga(rand);
                String timestamp = LocalDateTime.now().minusMinutes(rand.nextInt(1000)).toString();
                int idBiglietto = i;

                String json = String.format("""
                {
                    "idBiglietto": %d,
                    "targa": "%s",
                    "cap": "%s",
                    "idCasello": %d,
                    "direzione": "%s",
                    "timestamp": "%s"
                }
                """, idBiglietto, targa, cap, idCasello, direzione, timestamp);

                writer.write(json.replace("\n", "").replace("    ", "").trim() + "\n");
            }

            System.out.println("✅ File pedaggi_entrata.txt generato in: " + percorsoFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String generaTarga(Random rand) {
        String lettere = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        return "" + lettere.charAt(rand.nextInt(26)) +
                lettere.charAt(rand.nextInt(26)) +
                rand.nextInt(10) +
                rand.nextInt(10) +
                rand.nextInt(10) +
                lettere.charAt(rand.nextInt(26)) +
                lettere.charAt(rand.nextInt(26));
    }
}
