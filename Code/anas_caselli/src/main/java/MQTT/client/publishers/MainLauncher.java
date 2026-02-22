package MQTT.client.publishers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static MQTT.client.publishers.util.AssegnaIdTratta.scegliTrattaCompatibile;

public class MainLauncher {

    private static final int NUM_INGRESSI = 5;
    private static final long ATTESA_DOPO_GUASTO_MS = 2000;
    private static final long PAUSA_TRA_USCITE_MS = 5000;

    // ✅ INFRAZIONI
    private static final int NUM_INFRAZIONI = 2;
    private static final long PAUSA_PRIMA_INFRAZIONI_MS = 1500;

    public static void main(String[] args) throws Exception {

        List<JsonObject> ingressi = new ArrayList<>();

        // 1) Raccolta ingressi
        for (int i = 1; i <= NUM_INGRESSI; i++) {
            System.out.println("🔄 Iterazione " + i + "/" + NUM_INGRESSI);

            String ingressoJson = PublisherIngresso.publishIngresso();
            if (ingressoJson == null) {
                System.out.println("⚠️ Fallita pubblicazione ingresso " + i);
                continue;
            }

            JsonObject ingressoObj = JsonParser.parseString(ingressoJson).getAsJsonObject();
            if (!ingressoObj.has("idBiglietto")) {
                System.out.println("❌ idBiglietto non trovato nell'ingresso: " + ingressoObj);
                continue;
            }

            ingressi.add(ingressoObj);
            int idBiglietto = ingressoObj.get("idBiglietto").getAsInt();
            System.out.println("✔️ Ingresso registrato per biglietto " + idBiglietto);
        }

        if (ingressi.isEmpty()) {
            System.out.println("⛔ Nessun ingresso valido raccolto. Stop.");
            return;
        }

        System.out.println("\n🆗 Raccolti " + ingressi.size() + " ingressi.\n");

        // 2) Guasto casello (come prima)
        System.out.println("🔧 Simulazione guasto casello...");
        PublisherCaselloGuasto.publishGuasto();
        Thread.sleep(ATTESA_DOPO_GUASTO_MS);

        // 3) Incidente su uno degli ingressi (come prima)
        Random random = new Random();
        int indexIncidente = random.nextInt(ingressi.size());
        JsonObject ingressoIncidente = ingressi.get(indexIncidente);

        int idIncidente = ingressoIncidente.get("idBiglietto").getAsInt();
        ingressoIncidente.addProperty("incidente", true);
        ingressoIncidente.addProperty("descrizione", generaDescrizioneCasuale());
        ingressoIncidente.addProperty("gravita", generaGravitaCasuale());

        String cap = ingressoIncidente.get("cap").getAsString();
        int idTratta = scegliTrattaCompatibile(cap);
        ingressoIncidente.addProperty("idTratta", idTratta);

        salvaIncidenteSuFile(ingressoIncidente);
        PublisherIncidente.publishIncidente(ingressoIncidente);

        System.out.println("💥 INCIDENTE simulato sul biglietto: " + idIncidente + " (idTratta=" + idTratta + ")\n");

        // ✅ 3.5) INFRAZIONI VELOCITÀ (su alcuni ingressi non incidentati)
        Thread.sleep(PAUSA_PRIMA_INFRAZIONI_MS);
        simulaInfrazioniVelocita(ingressi, idIncidente, NUM_INFRAZIONI);

        // 4) Uscite per tutti tranne il biglietto incidentato
        for (JsonObject ingresso : ingressi) {
            int idBiglietto = ingresso.get("idBiglietto").getAsInt();

            if (idBiglietto == idIncidente) {
                System.out.println("⛔ Nessuna uscita per biglietto " + idBiglietto + " (incidente)");
                continue;
            }

            Thread.sleep(PAUSA_TRA_USCITE_MS);
            PublisherUscita.main(new String[]{String.valueOf(idBiglietto)});
            System.out.println("✅ Uscita pubblicata per biglietto " + idBiglietto);
        }

        System.out.println("\n🏁 Procedura completata (ingressi + guasto + incidente + infrazioni + uscite).");
    }

    // ✅ Simulazione infrazioni velocità: sceglie ingressi reali già raccolti
    private static void simulaInfrazioniVelocita(List<JsonObject> ingressi, int idIncidente, int numInfrazioni) {
        Random rnd = new Random();

        System.out.println("\n🚓 Simulazione infrazioni velocità (" + numInfrazioni + ")...");

        int tentativi = 0;
        int pubblicate = 0;

        while (pubblicate < numInfrazioni && tentativi < 20) {
            tentativi++;

            JsonObject ingresso = ingressi.get(rnd.nextInt(ingressi.size()));
            int idBiglietto = ingresso.get("idBiglietto").getAsInt();

            // Evita di fare infrazione sull’auto incidentata (coerenza logica)
            if (idBiglietto == idIncidente) continue;

            try {
                // Questo metodo lo implementiamo in PublisherInfrazioneVelocita
                PublisherInfrazioneVelocita.publishInfrazioneVelocitaDaIngresso(ingresso);
                pubblicate++;
            } catch (Exception e) {
                System.out.println("❌ Errore pubblicazione infrazione: " + e.getMessage());
            }
        }

        System.out.println("✅ Infrazioni velocità pubblicate: " + pubblicate + "/" + numInfrazioni + "\n");
    }

    private static void salvaIncidenteSuFile(JsonObject incidente) {
        try (FileWriter writer = new FileWriter("incidente_log.txt", true)) {
            writer.write(incidente.toString() + System.lineSeparator());
        } catch (IOException e) {
            System.out.println("❌ Errore scrittura file incidente: " + e.getMessage());
        }
    }

    private static String generaDescrizioneCasuale() {
        String[] descrizioni = {
                "Tamponamento a catena",
                "Veicolo in fiamme",
                "Ribaltamento autocarro",
                "Collisione frontale"
        };
        return descrizioni[new Random().nextInt(descrizioni.length)];
    }

    private static String generaGravitaCasuale() {
        String[] gravita = {"LIEVE", "MEDIA", "GRAVE"};
        return gravita[new Random().nextInt(gravita.length)];
    }
}