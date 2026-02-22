package MQTT.client;

import MQTT.client.publishers.util.SSLTrust;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTTClient {
    MqttClient client;

    public MQTTClient() {
        try {
            SSLTrust.trustAllCerts();
            client = new MqttClient(
                    "tcp://broker.emqx.io:1883",
                    MqttClient.generateClientId(),
                    new MemoryPersistence()
            );
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        try {
            client.setCallback(new SubscribeCallback());
            client.connect();

            client.subscribe("casello/ingresso", 0);
            client.subscribe("casello/uscita/+", 0);
            client.subscribe("casello/guasto/#", 0);
            client.subscribe("tratta/incidente", 0);
            client.subscribe("casello/pagamento/#", 0);
            client.subscribe("tratta/infrazione/velocita", 0);
            client.subscribe("casello/storico_passaggio/#", 0);

            System.out.println("✅ MQTT Client connesso e iscritto ai topic:");
            System.out.println("   - casello/ingresso");
            System.out.println("   - casello/uscita/+");
            System.out.println("   - casello/guasto/#");
            System.out.println("   - tratta/incidente");
            System.out.println("   - casello/pagamento/#");
            System.out.println("   - tratta/infrazione/velocita");
            System.out.println("   - casello/storico_passaggio/#");

        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        MQTTClient client = new MQTTClient();
        client.start();
    }
}