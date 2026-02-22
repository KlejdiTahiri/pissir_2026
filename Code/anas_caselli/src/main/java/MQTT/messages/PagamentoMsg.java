package MQTT.messages;

import java.time.Instant;

public class PagamentoMsg {
    private int idBiglietto;
    private String targa;

    private String capIn;
    private int idCaselloIn;
    private String direzioneIn;

    private String capOut;
    private int idCaselloOut;
    private String direzioneOut;

    private int distanza;     // km tratta (o “distanza” calcolata)
    private double importo;   // prezzo tratta

    // "CONTANTI" | "TELEPASS" | "EVASIONE"
    private String metodo;
    private boolean pagato;

    private String timestamp = Instant.now().toString();

    public PagamentoMsg() {}

    public int getIdBiglietto() { return idBiglietto; }
    public void setIdBiglietto(int idBiglietto) { this.idBiglietto = idBiglietto; }

    public String getTarga() { return targa; }
    public void setTarga(String targa) { this.targa = targa; }

    public String getCapIn() { return capIn; }
    public void setCapIn(String capIn) { this.capIn = capIn; }

    public int getIdCaselloIn() { return idCaselloIn; }
    public void setIdCaselloIn(int idCaselloIn) { this.idCaselloIn = idCaselloIn; }

    public String getDirezioneIn() { return direzioneIn; }
    public void setDirezioneIn(String direzioneIn) { this.direzioneIn = direzioneIn; }

    public String getCapOut() { return capOut; }
    public void setCapOut(String capOut) { this.capOut = capOut; }

    public int getIdCaselloOut() { return idCaselloOut; }
    public void setIdCaselloOut(int idCaselloOut) { this.idCaselloOut = idCaselloOut; }

    public String getDirezioneOut() { return direzioneOut; }
    public void setDirezioneOut(String direzioneOut) { this.direzioneOut = direzioneOut; }

    public int getDistanza() { return distanza; }
    public void setDistanza(int distanza) { this.distanza = distanza; }

    public double getImporto() { return importo; }
    public void setImporto(double importo) { this.importo = importo; }

    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }

    public boolean isPagato() { return pagato; }
    public void setPagato(boolean pagato) { this.pagato = pagato; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}