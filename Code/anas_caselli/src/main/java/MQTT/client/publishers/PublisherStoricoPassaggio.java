package MQTT.client.publishers;

import MQTT.client.publishers.util.SSLTrust;
import MQTT.messages.StoricoPassaggioMsg;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.eclipse.paho.client.mqttv3.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public class PublisherStoricoPassaggio {

    private static final Gson gson = new Gson();

    private static final String BROKER = "tcp://broker.emqx.io:1883";
    private static final String TOPIC_PREFIX = "casello/storico_passaggio/";

    /**
     * Pubblica uno storico passaggio unificando:
     * - entrata (pedaggi_entrata.txt)
     * - uscita (pedaggi_uscita.txt)
     * - pagamento (parametri in input)
     *
     * @param idBiglietto id biglietto
     * @param chilometri  distanza (km)
     * @param prezzo      prezzo pagato
     * @param metodo      TELEPASS / CONTANTI / EVASIONE
     * @param pagato      true/false
     */
    public static void publishStorico(int idBiglietto, int chilometri, double prezzo, String metodo, boolean pagato) throws Exception {

        InputStream isUscita = PublisherStoricoPassaggio.class.getClassLoader().getResourceAsStream("pedaggi_uscita.txt");
        InputStream isEntrata = PublisherStoricoPassaggio.class.getClassLoader().getResourceAsStream("pedaggi_entrata.txt");

        if (isUscita == null || isEntrata == null) {
            System.out.println("⚠️ pedaggi_uscita.txt o pedaggi_entrata.txt non trovato nel classpath!");
            return;
        }

        List<String> uscite = new BufferedReader(new InputStreamReader(isUscita)).lines().toList();
        List<String> entrate = new BufferedReader(new InputStreamReader(isEntrata)).lines().toList();

        // 1) uscita
        Optional<JsonObject> uscitaOpt = uscite.stream()
                .map(r -> gson.fromJson(r, JsonObject.class))
                .filter(o -> o.has("idBiglietto") && o.get("idBiglietto").getAsInt() == idBiglietto)
                .findFirst();

        if (uscitaOpt.isEmpty()) {
            System.out.println("❌ Storico: uscita non trovata per idBiglietto=" + idBiglietto);
            return;
        }
        JsonObject uscita = uscitaOpt.get();

        // 2) entrata associata (targa + stesso giorno + idBiglietto)
        String targa = uscita.get("targa").getAsString();
        String dataSolo = uscita.get("timestamp").getAsString().substring(0, 10);

        Optional<JsonObject> entrataOpt = entrate.stream()
                .map(r -> gson.fromJson(r, JsonObject.class))
                .filter(o ->
                        o.has("targa") && targa.equals(o.get("targa").getAsString()) &&
                                o.has("timestamp") && o.get("timestamp").getAsString().startsWith(dataSolo) &&
                                o.has("idBiglietto") && o.get("idBiglietto").getAsInt() == idBiglietto
                ).findFirst();

        if (entrataOpt.isEmpty()) {
            System.out.println("❌ Storico: entrata non trovata per targa=" + targa + " idBiglietto=" + idBiglietto);
            return;
        }
        JsonObject entrata = entrataOpt.get();

        // 3) dati tratta
        String capIn = entrata.get("cap").getAsString();
        int idCaselloIn = entrata.get("idCasello").getAsInt();
        String dirIn = entrata.get("direzione").getAsString();

        String capOut = uscita.get("cap").getAsString();
        int idCaselloOut = uscita.get("idCasello").getAsInt();
        String dirOut = uscita.get("direzione").getAsString();

        String tsIn = entrata.get("timestamp").getAsString();
        String tsOut = uscita.get("timestamp").getAsString();

        // 4) velocità media: km / ore
        double velocitaMedia = calcolaVelocitaMedia(chilometri, tsIn, tsOut);

        // 5) telepass flag (1 se metodo TELEPASS)
        int telepass = (metodo != null && metodo.equalsIgnoreCase("TELEPASS")) ? 1 : 0;

        StoricoPassaggioMsg msg = new StoricoPassaggioMsg();
        msg.setIdBiglietto(idBiglietto);
        msg.setTarga(targa);

        msg.setCapIn(capIn);
        msg.setIdCaselloIn(idCaselloIn);
        msg.setDirezioneIn(dirIn);

        msg.setCapOut(capOut);
        msg.setIdCaselloOut(idCaselloOut);
        msg.setDirezioneOut(dirOut);

        msg.setDataIngresso(tsIn);
        msg.setDataUscita(tsOut);

        msg.setChilometri(chilometri);
        msg.setVelocitaMedia(velocitaMedia);

        // Se è evasione, puoi decidere: prezzo_pagato = 0 oppure prezzo “dovuto”.
        // Qui lasciamo il prezzo sempre valorizzato (utile per analytics).
        msg.setPrezzoPagato(prezzo);
        msg.setMetodoPagamento(metodo != null ? metodo : (pagato ? "CONTANTI" : "EVASIONE"));
        msg.setTelepass(telepass);

        // publish mqtt
        SSLTrust.trustAllCerts();
        MqttClient client = new MqttClient(BROKER, MqttClient.generateClientId());
        client.connect();

        String topic = TOPIC_PREFIX + idBiglietto;
        String payload = gson.toJson(msg);

        MqttMessage mqttMsg = new MqttMessage(payload.getBytes());
        mqttMsg.setQos(0);
        client.publish(topic, mqttMsg);

        System.out.printf("📚 StoricoPassaggio pubblicato su %s | targa=%s | km=%d | v=%.1f km/h | metodo=%s | prezzo=€%.2f%n",
                topic, targa, chilometri, velocitaMedia, msg.getMetodoPagamento(), prezzo);

        client.disconnect();
        client.close();
    }

    private static double calcolaVelocitaMedia(int km, String tsIn, String tsOut) {
        try {
            // Timestamp come "2025-11-22T01:34:17.659506197" (senza offset):
            // OffsetDateTime richiede offset; se non c'è, usiamo parse “furbo” con +00:00
            OffsetDateTime in = parseTs(tsIn);
            OffsetDateTime out = parseTs(tsOut);

            long seconds = Duration.between(in, out).getSeconds();
            if (seconds <= 0) return 0.0;

            double ore = seconds / 3600.0;
            return km / ore;
        } catch (Exception e) {
            return 0.0;
        }
    }

    private static OffsetDateTime parseTs(String ts) {
        // Se manca offset, aggiunge Z
        if (ts != null && !ts.endsWith("Z") && !ts.contains("+")) {
            return OffsetDateTime.parse(ts + "Z");
        }
        return OffsetDateTime.parse(ts);
    }
}