package MQTT.client.publishers;

import com.google.gson.JsonObject;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.time.Instant;

public class PublisherIncidente {

    public static void publishIncidente(JsonObject incidenteJson) throws MqttException {

        // Aggiungo timestamp ISO se manca
        if (!incidenteJson.has("timestamp")) {
            incidenteJson.addProperty("timestamp", Instant.now().toString());
        }

        // ✅ aggiungi KM se manca (valore demo)
        if (!incidenteJson.has("km")) {
            incidenteJson.addProperty("km", 0.0);
        }

        String jsonString = incidenteJson.toString();

        MqttClient client = new MqttClient("tcp://broker.emqx.io:1883",
                MqttClient.generateClientId());
        client.connect();

        String topic = "tratta/incidente";

        MqttMessage message = new MqttMessage(jsonString.getBytes());
        message.setQos(0);

        client.publish(topic, message);

        System.out.println("🚨 Evento incidente pubblicato:");
        System.out.println(jsonString);

        client.disconnect();
    }
}
