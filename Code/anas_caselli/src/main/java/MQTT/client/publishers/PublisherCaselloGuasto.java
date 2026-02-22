package MQTT.client.publishers;


import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;

public class PublisherCaselloGuasto {

    public static String publishGuasto() throws MqttException {

        // 📌 Leggi il file caselli_guasti.txt
        InputStream is = PublisherCaselloGuasto.class.getClassLoader().getResourceAsStream("caselli_guasti.txt");
        if (is == null) {
            System.out.println("⚠️ File caselli_guasti.txt NON trovato!");
            return null;
        }

        List<String> righe = new BufferedReader(new InputStreamReader(is)).lines().toList();
        if (righe.isEmpty()) {
            System.out.println("⚠️ Il file caselli_guasti.txt è vuoto.");
            return null;
        }

        // 📌 Prendo un casello casuale da guastare
        Random rand = new Random();
        String evento = righe.get(rand.nextInt(righe.size()));  // esempio: "MI 1 INGRESSO"

        // 🔍 Parsing
        String[] parts = evento.split("\\s+");
        String cap = parts[0];
        String idCasello = parts[1];
        String direzione = parts[2];

        // 📌 Topic MQTT con wildcard struttura
        String topic = "casello/guasto/" + cap + "/" + idCasello + "/" + direzione;

        MqttClient client = new MqttClient("tcp://broker.emqx.io:1883", MqttClient.generateClientId());
        client.connect();

        // 🔥 Payload JSON (puoi modificarlo come vuoi)
        String json = String.format(
                "{\"cap\":\"%s\",\"idCasello\":%s,\"direzione\":\"%s\",\"timestamp\":\"%s\"}",
                cap, idCasello, direzione, java.time.LocalDateTime.now()
        );

        MqttMessage message = new MqttMessage(json.getBytes());
        message.setQos(0);

        client.publish(topic, message);

        System.out.println("🚨 Evento GUASTO pubblicato:");
        System.out.println("  Topic: " + topic);
        System.out.println("  Payload: " + json);

        client.disconnect();

        return json;
    }

    public static void main(String[] args) {
        try {
            publishGuasto();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
