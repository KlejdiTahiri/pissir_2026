package MQTT.client.publishers;

import MQTT.client.publishers.util.SSLTrust;
import MQTT.messages.InfrazioneVelocitaMsg;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.eclipse.paho.client.mqttv3.*;

import java.time.Instant;
import java.util.Random;

public class PublisherInfrazioneVelocita {

    private static final String BROKER = "tcp://broker.emqx.io:1883";
    private static final String TOPIC = "tratta/infrazione/velocita";

    private static final Gson gson = new Gson();
    private static final Random rnd = new Random();

    public static void publishInfrazioneVelocitaDaIngresso(JsonObject ingresso) throws Exception {

        int idBiglietto = ingresso.get("idBiglietto").getAsInt();
        String targa = ingresso.get("targa").getAsString();
        String cap = ingresso.get("cap").getAsString();
        int idCasello = ingresso.get("idCasello").getAsInt();
        String direzione = ingresso.get("direzione").getAsString();

        // limiti demo (puoi collegarlo alla tratta in futuro)
        int vmax = 130;
        int over = 10 + rnd.nextInt(61); // 10..70 oltre
        int v = vmax + over;

        double km = 1 + rnd.nextInt(900);
        double importo = calcolaMultaDemo(vmax, v);

        InfrazioneVelocitaMsg msg = new InfrazioneVelocitaMsg();
        msg.setIdBiglietto(idBiglietto);
        msg.setTarga(targa);

        msg.setCap(cap);
        msg.setIdCasello(idCasello);
        msg.setDirezione(direzione);

        msg.setIdTratta(0);
        msg.setKmRilevazione(km);

        msg.setVelocitaMassima(vmax);
        msg.setVelocitaRilevata(v);

        msg.setImporto(importo);
        msg.setTimestamp(Instant.now().toString());

        SSLTrust.trustAllCerts();
        MqttClient client = new MqttClient(BROKER, MqttClient.generateClientId());
        client.connect();

        client.publish(TOPIC, new MqttMessage(gson.toJson(msg).getBytes()));

        System.out.printf("🚓 Infrazione pubblicata su %s | targa=%s idBiglietto=%d v=%d vmax=%d €%.2f%n",
                TOPIC, targa, idBiglietto, v, vmax, importo);

        client.disconnect();
        client.close();
    }

    private static double calcolaMultaDemo(int vmax, int v) {
        int delta = v - vmax;
        if (delta <= 10) return 42.00;
        if (delta <= 40) return 173.00;
        if (delta <= 60) return 543.00;
        return 845.00;
    }
}