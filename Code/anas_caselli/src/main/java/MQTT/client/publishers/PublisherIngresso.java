package MQTT.client.publishers;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;

public class PublisherIngresso {
    public static String publishIngresso() throws MqttException{
        InputStream is = PublisherIngresso.class.getClassLoader().getResourceAsStream("pedaggi_entrata.txt");
        if (is == null) {
            System.out.println("⚠️ File non trovato nel classpath!");
            return null;
        }

        List<String> righe = new BufferedReader(new InputStreamReader(is)).lines().toList();
        if (righe.isEmpty()) {
            System.out.println("⚠️ Il file è vuoto.");
            return null;
        }

        Random rand = new Random();
        String evento = righe.get(rand.nextInt(righe.size()));

        MqttClient client = new MqttClient("tcp://broker.emqx.io:1883", MqttClient.generateClientId());
        client.connect();

        String topic = "casello/ingresso";

        MqttMessage message = new MqttMessage(evento.getBytes());
        message.setQos(0);
        client.publish(topic, message);

        System.out.println("✅ Evento ingresso pubblicato: " + evento);
        client.disconnect();

        return evento;
    }

    public static void main(String[] args) {
        try {
            publishIngresso();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

