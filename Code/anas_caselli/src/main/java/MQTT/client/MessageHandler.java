package MQTT.client;

import MQTT.messages.AutomobileMsg;
import MQTT.messages.IncidenteMsg;

/**
 * Interfaccia per gestire i diversi tipi di messaggi MQTT
 */
public interface MessageHandler {

    /**
     * Gestisce messaggi di tipo AutomobileMsg (ingresso, uscita, guasto)
     */
    void handleMessage(String topic, AutomobileMsg msg);

    /**
     * Gestisce messaggi di tipo IncidenteMsg
     */
    void handleMessage(String topic, IncidenteMsg msg);
}