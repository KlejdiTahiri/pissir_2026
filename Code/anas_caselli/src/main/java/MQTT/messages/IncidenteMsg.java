package MQTT.messages;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDateTime;

/**
 * Classe per rappresentare i messaggi MQTT di incidente
 */
public class IncidenteMsg extends MqttMsg {

    @SerializedName("idBiglietto")
    private int idBiglietto;

    @SerializedName("targa")
    private String targa;

    @SerializedName("cap")
    private String cap;

    @SerializedName("idCasello")
    private int idCasello;

    @SerializedName("direzione")
    private String direzione;

    @SerializedName("incidente")
    private boolean incidente;

    @SerializedName("descrizione")
    private String descrizione;

    @SerializedName("gravita")
    private String gravita; // BASSA, MEDIA, ALTA

    @SerializedName("idTratta")
    private int idTratta;

    // Costruttori
    public IncidenteMsg() {}

    public IncidenteMsg(int idBiglietto, String targa, String cap, int idCasello,
                        String direzione, String descrizione, String gravita, int idTratta) {
        this.idBiglietto = idBiglietto;
        this.targa = targa;
        this.cap = cap;
        this.idCasello = idCasello;
        this.direzione = direzione;
        this.incidente = true;
        this.descrizione = descrizione;
        this.gravita = gravita;
        this.idTratta = idTratta;
        this.timestamp = LocalDateTime.now().toString();
    }

    // Getters and Setters
    public int getIdBiglietto() {
        return idBiglietto;
    }

    public void setIdBiglietto(int idBiglietto) {
        this.idBiglietto = idBiglietto;
    }

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

    public boolean isIncidente() {
        return incidente;
    }

    public void setIncidente(boolean incidente) {
        this.incidente = incidente;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getGravita() {
        return gravita;
    }

    public void setGravita(String gravita) {
        this.gravita = gravita;
    }

    public int getIdTratta() {
        return idTratta;
    }

    public void setIdTratta(int idTratta) {
        this.idTratta = idTratta;
    }

    @Override
    public boolean isValid() {
        return idBiglietto > 0
                && targa != null && !targa.isBlank()
                && cap != null && !cap.isBlank()
                && idCasello > 0
                && direzione != null && !direzione.isBlank()
                && idTratta > 0
                && incidente == true;
    }

    @Override
    public String toString() {
        return "IncidenteMsg{" +
                "idBiglietto=" + idBiglietto +
                ", targa='" + targa + '\'' +
                ", cap='" + cap + '\'' +
                ", idCasello=" + idCasello +
                ", direzione='" + direzione + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", incidente=" + incidente +
                ", descrizione='" + descrizione + '\'' +
                ", gravita='" + gravita + '\'' +
                ", idTratta=" + idTratta +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IncidenteMsg that = (IncidenteMsg) o;

        if (idBiglietto != that.idBiglietto) return false;
        if (idCasello != that.idCasello) return false;
        if (idTratta != that.idTratta) return false;
        if (targa != null ? !targa.equals(that.targa) : that.targa != null) return false;
        return cap != null ? cap.equals(that.cap) : that.cap == null;
    }

    @Override
    public int hashCode() {
        int result = idBiglietto;
        result = 31 * result + (targa != null ? targa.hashCode() : 0);
        result = 31 * result + (cap != null ? cap.hashCode() : 0);
        result = 31 * result + idCasello;
        result = 31 * result + idTratta;
        return result;
    }
}