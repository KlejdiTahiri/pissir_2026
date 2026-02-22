package MQTT.messages;

import com.google.gson.annotations.SerializedName;

/**
 * Classe base astratta per tutti i messaggi MQTT
 */
public abstract class MqttMsg {

    @SerializedName("timestamp")
    protected String timestamp;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Metodo astratto per validare il messaggio
     */
    public abstract boolean isValid();
}