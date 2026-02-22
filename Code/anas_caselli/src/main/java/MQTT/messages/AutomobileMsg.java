package MQTT.messages;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDateTime;

/**
 * Classe per rappresentare i messaggi MQTT ricevuti dai caselli
 */
public class AutomobileMsg extends MqttMsg {

    @SerializedName("targa")
    private String targa;

    @SerializedName("cap")
    private String cap;

    @SerializedName("idCasello")
    private int idCasello;

    @SerializedName("direzione")
    private String direzione;

    @SerializedName("tipoGuasto")
    private String tipoGuasto;

    @SerializedName("descrizione")
    private String descrizione;

    @SerializedName("idBiglietto")
    private Integer idBiglietto;

    // Costruttori
    public AutomobileMsg() {}

    public AutomobileMsg(String targa, String cap, int idCasello, String direzione) {
        this.targa = targa;
        this.cap = cap;
        this.idCasello = idCasello;
        this.direzione = direzione;
        this.timestamp = LocalDateTime.now().toString();
    }

    // Getters and Setters
    public String getTarga() {
        return targa;
    }

    public void setTarga(String targa) {
        this.targa = targa;
    }

    public String getCap() {
        return cap;
    }

    public void setCap(String cap) {
        this.cap = cap;
    }

    public int getIdCasello() {
        return idCasello;
    }

    public void setIdCasello(int idCasello) {
        this.idCasello = idCasello;
    }

    public String getDirezione() {
        return direzione;
    }

    public void setDirezione(String direzione) {
        this.direzione = direzione;
    }

    public Integer getIdBiglietto() {
        return idBiglietto;
    }

    public void setIdBiglietto(Integer idBiglietto) {
        this.idBiglietto = idBiglietto;
    }

    public String getTipoGuasto() {
        return tipoGuasto;
    }

    public void setTipoGuasto(String tipoGuasto) {
        this.tipoGuasto = tipoGuasto;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    // Metodi di validazione
    @Override
    public boolean isValid() {
        return isValidForIngresso();
    }

    public boolean isValidForIngresso() {
        return targa != null && !targa.trim().isEmpty() &&
                cap != null && !cap.trim().isEmpty() &&
                idCasello > 0 &&
                direzione != null && !direzione.trim().isEmpty();
    }

    public boolean isValidForUscita() {
        return isValidForIngresso();
    }

    public boolean isValidForGuasto() {
        return cap != null && !cap.trim().isEmpty() &&
                idCasello > 0 &&
                direzione != null && !direzione.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "AutomobileMsg{" +
                "targa='" + targa + '\'' +
                ", cap='" + cap + '\'' +
                ", idCasello=" + idCasello +
                ", direzione='" + direzione + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", tipoGuasto='" + tipoGuasto + '\'' +
                ", descrizione='" + descrizione + '\'' +
                ", idBiglietto=" + idBiglietto +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AutomobileMsg message = (AutomobileMsg) o;

        if (idCasello != message.idCasello) return false;
        if (targa != null ? !targa.equals(message.targa) : message.targa != null) return false;
        if (cap != null ? !cap.equals(message.cap) : message.cap != null) return false;
        return direzione != null ? direzione.equals(message.direzione) : message.direzione == null;
    }

    @Override
    public int hashCode() {
        int result = targa != null ? targa.hashCode() : 0;
        result = 31 * result + (cap != null ? cap.hashCode() : 0);
        result = 31 * result + idCasello;
        result = 31 * result + (direzione != null ? direzione.hashCode() : 0);
        return result;
    }
}