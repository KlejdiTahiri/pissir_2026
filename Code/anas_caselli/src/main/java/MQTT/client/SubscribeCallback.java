package MQTT.client;

import MQTT.messages.*;
import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.*;

public class SubscribeCallback implements MqttCallback {

    private final MqttMessageHandler handler = new MqttMessageHandler();
    private final Gson gson = new Gson();

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("❌ Connessione MQTT persa: " + cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) {
        String payload = mqttMessage.toString();
        System.out.printf("📥 Ricevuto messaggio da topic '%s': %s%n", topic, payload);

        try {
            if (topic.equals("tratta/incidente")) {
                IncidenteMsg imsg = gson.fromJson(payload, IncidenteMsg.class);
                handler.handleMessage(topic, imsg);

            } else if (topic.startsWith("casello/guasto/")) {
                AutomobileMsg msg = gson.fromJson(payload, AutomobileMsg.class);
                handler.handleMessage(topic, msg);

            } else if (topic.startsWith("casello/pagamento/")) {
                PagamentoMsg pmsg = gson.fromJson(payload, PagamentoMsg.class);
                handler.handlePagamento(topic, pmsg);
            } else if (topic.equals("tratta/infrazione/velocita")) {
                InfrazioneVelocitaMsg vmsg = gson.fromJson(payload, InfrazioneVelocitaMsg.class);
                handler.handleInfrazioneVelocita(topic, vmsg);
            }   else if (topic.startsWith("casello/storico_passaggio/")) {
                StoricoPassaggioMsg smsg = gson.fromJson(payload, StoricoPassaggioMsg.class);
                handler.handleStoricoPassaggio(topic, smsg);
            } else {
                AutomobileMsg msg = gson.fromJson(payload, AutomobileMsg.class);
                handler.handleMessage(topic, msg);
            }
        } catch (Exception e) {
            System.out.println("❗ Errore nel parsing del messaggio: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("📤 Delivery completato per token: " + token.getMessageId());
    }
}