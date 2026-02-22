package MQTT.client.publishers;

import MQTT.client.publishers.util.CalcolatoreDistanza;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.eclipse.paho.client.mqttv3.*;

import java.io.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class PublisherUscita {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("❌ Devi passare idBiglietto come argomento.");
            return;
        }

        int idBigliettoDaControllare = Integer.parseInt(args[0]);

        InputStream isUscita = PublisherUscita.class.getClassLoader().getResourceAsStream("pedaggi_uscita.txt");
        InputStream isEntrata = PublisherUscita.class.getClassLoader().getResourceAsStream("pedaggi_entrata.txt");

        if (isUscita == null || isEntrata == null) {
            System.out.println("⚠️ Uno dei file non trovato nel classpath!");
            return;
        }

        try {
            List<String> uscite = new BufferedReader(new InputStreamReader(isUscita)).lines().toList();
            List<String> entrate = new BufferedReader(new InputStreamReader(isEntrata)).lines().toList();

            if (uscite.isEmpty() || entrate.isEmpty()) {
                System.out.println("⚠️ Uno dei file è vuoto.");
                return;
            }

            Random rand = new Random();

            // Trova una uscita con idBiglietto che coincide con quello passato
            Optional<String> uscitaFiltrata = uscite.stream()
                    .filter(u -> {
                        JsonObject uscitaJson = new Gson().fromJson(u, JsonObject.class);
                        return uscitaJson.has("idBiglietto") &&
                                uscitaJson.get("idBiglietto").getAsInt() == idBigliettoDaControllare;
                    })
                    .findFirst();

            if (uscitaFiltrata.isEmpty()) {
                System.out.println("❌ Nessun messaggio di uscita trovato con idBiglietto = " + idBigliettoDaControllare);
                return;
            }

            String eventoUscita = uscitaFiltrata.get();
            JsonObject uscitaJson = new Gson().fromJson(eventoUscita, JsonObject.class);

            String targa = uscitaJson.get("targa").getAsString();
            String capUscita = uscitaJson.get("cap").getAsString();
            int idCaselloUscita = uscitaJson.get("idCasello").getAsInt();
            String direzioneUscita = uscitaJson.get("direzione").getAsString();
            String timestampUscita = uscitaJson.get("timestamp").getAsString();

            String dataSolo = timestampUscita.substring(0, 10);

            // Cerca la riga di entrata con stessa targa e data
            Optional<JsonObject> entrataAssociata = entrate.stream()
                    .map(riga -> new Gson().fromJson(riga, JsonObject.class))
                    .filter(obj ->
                            obj.get("targa").getAsString().equals(targa) &&
                                    obj.get("timestamp").getAsString().startsWith(dataSolo) &&
                                    obj.has("idBiglietto") &&
                                    obj.get("idBiglietto").getAsInt() == idBigliettoDaControllare
                    ).findFirst();

            if (entrataAssociata.isEmpty()) {
                System.out.println("❌ Nessun messaggio di entrata trovato per targa " + targa + " e idBiglietto " + idBigliettoDaControllare);
                return;
            }

            JsonObject entrataJson = entrataAssociata.get();
            String capEntrata = entrataJson.get("cap").getAsString();
            int idCaselloEntrata = entrataJson.get("idCasello").getAsInt();
            String direzioneEntrata = entrataJson.get("direzione").getAsString();

            int distanza = CalcolatoreDistanza.calcolaDistanza(
                    capEntrata, idCaselloEntrata, direzioneEntrata,
                    capUscita, idCaselloUscita, direzioneUscita
            );

            MqttClient client = new MqttClient("tcp://broker.emqx.io:1883", MqttClient.generateClientId());
            client.connect();

            String topic = "casello/uscita/" + distanza;
            MqttMessage message = new MqttMessage(eventoUscita.getBytes());
            message.setQos(0);
            client.publish(topic, message);

            System.out.println("✅ Evento pubblicato su topic: " + topic);
            System.out.println("📨 Contenuto: " + eventoUscita);

            // ✅ Simula pagamento (contanti/telepass/evasione)
            PublisherPagamento.publishPagamento(idBigliettoDaControllare, distanza);
            client.disconnect();

        } catch (Exception e) {
            System.err.println("❌ Errore durante la pubblicazione:");
            e.printStackTrace();
        }
    }
}
