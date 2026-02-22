package MQTT.messages;

public class StoricoPassaggioMsg {
    private int idBiglietto;
    private String targa;

    private String capIn;
    private int idCaselloIn;
    private String direzioneIn;

    private String capOut;
    private int idCaselloOut;
    private String direzioneOut;

    private String dataIngresso;
    private String dataUscita;

    private int chilometri;
    private double velocitaMedia;

    private int telepass; // 1/0
    private double prezzoPagato;
    private String metodoPagamento;

    public boolean isValid() {
        return targa != null && !targa.isBlank()
                && capIn != null && !capIn.isBlank()
                && capOut != null && !capOut.isBlank()
                && direzioneIn != null && !direzioneIn.isBlank()
                && direzioneOut != null && !direzioneOut.isBlank()
                && idCaselloIn > 0 && idCaselloOut > 0
                && dataIngresso != null && !dataIngresso.isBlank()
                && dataUscita != null && !dataUscita.isBlank()
                && chilometri >= 0
                && metodoPagamento != null && !metodoPagamento.isBlank();
    }

    // getters/setters
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

    public String getDataIngresso() { return dataIngresso; }
    public void setDataIngresso(String dataIngresso) { this.dataIngresso = dataIngresso; }

    public String getDataUscita() { return dataUscita; }
    public void setDataUscita(String dataUscita) { this.dataUscita = dataUscita; }

    public int getChilometri() { return chilometri; }
    public void setChilometri(int chilometri) { this.chilometri = chilometri; }

    public double getVelocitaMedia() { return velocitaMedia; }
    public void setVelocitaMedia(double velocitaMedia) { this.velocitaMedia = velocitaMedia; }

    public int getTelepass() { return telepass; }
    public void setTelepass(int telepass) { this.telepass = telepass; }

    public double getPrezzoPagato() { return prezzoPagato; }
    public void setPrezzoPagato(double prezzoPagato) { this.prezzoPagato = prezzoPagato; }

    public String getMetodoPagamento() { return metodoPagamento; }
    public void setMetodoPagamento(String metodoPagamento) { this.metodoPagamento = metodoPagamento; }
}