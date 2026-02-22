package MQTT.messages;

public class InfrazioneVelocitaMsg {
    private int idBiglietto;
    private String targa;

    private String cap;
    private int idCasello;
    private String direzione;

    private int idTratta;            // se lo hai, altrimenti 0
    private double kmRilevazione;     // km sulla tratta (simulato)
    private int velocitaRilevata;
    private int velocitaMassima;

    private double importo;          // calcolato lato publisher (o lato server)
    private String timestamp;        // ISO

    public boolean isValid() {
        return idBiglietto > 0
                && targa != null && !targa.isBlank()
                && cap != null && !cap.isBlank()
                && idCasello > 0
                && direzione != null && !direzione.isBlank()
                && velocitaRilevata > 0
                && velocitaMassima > 0
                && velocitaRilevata > velocitaMassima;
    }

    // getter/setter
    public int getIdBiglietto() { return idBiglietto; }
    public void setIdBiglietto(int idBiglietto) { this.idBiglietto = idBiglietto; }

    public String getTarga() { return targa; }
    public void setTarga(String targa) { this.targa = targa; }

    public String getCap() { return cap; }
    public void setCap(String cap) { this.cap = cap; }

    public int getIdCasello() { return idCasello; }
    public void setIdCasello(int idCasello) { this.idCasello = idCasello; }

    public String getDirezione() { return direzione; }
    public void setDirezione(String direzione) { this.direzione = direzione; }

    public int getIdTratta() { return idTratta; }
    public void setIdTratta(int idTratta) { this.idTratta = idTratta; }

    public double getKmRilevazione() { return kmRilevazione; }
    public void setKmRilevazione(double kmRilevazione) { this.kmRilevazione = kmRilevazione; }

    public int getVelocitaRilevata() { return velocitaRilevata; }
    public void setVelocitaRilevata(int velocitaRilevata) { this.velocitaRilevata = velocitaRilevata; }

    public int getVelocitaMassima() { return velocitaMassima; }
    public void setVelocitaMassima(int velocitaMassima) { this.velocitaMassima = velocitaMassima; }

    public double getImporto() { return importo; }
    public void setImporto(double importo) { this.importo = importo; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}