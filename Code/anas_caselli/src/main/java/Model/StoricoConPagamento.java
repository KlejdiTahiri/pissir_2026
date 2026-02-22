package Model;
import java.time.LocalDateTime;

public class StoricoConPagamento {
    private Long id;
    private String targa;
    private String capUscita;
    private Integer idCaselloUscita;
    private String direzioneUscita;
    private LocalDateTime dataUscita;
    private Double prezzoPagato;
    private String metodoPagamentoStorico;

    // Dati da tabella pagamenti
    private LocalDateTime dataPagamento;
    private Double importo;
    private String metodoPagamento;
    private Integer statoPagamento;

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTarga() {
        return targa;
    }

    public void setTarga(String targa) {
        this.targa = targa;
    }

    public String getCapUscita() {
        return capUscita;
    }

    public void setCapUscita(String capUscita) {
        this.capUscita = capUscita;
    }

    public Integer getIdCaselloUscita() {
        return idCaselloUscita;
    }

    public void setIdCaselloUscita(Integer idCaselloUscita) {
        this.idCaselloUscita = idCaselloUscita;
    }

    public String getDirezioneUscita() {
        return direzioneUscita;
    }

    public void setDirezioneUscita(String direzioneUscita) {
        this.direzioneUscita = direzioneUscita;
    }

    public LocalDateTime getDataUscita() {
        return dataUscita;
    }

    public void setDataUscita(LocalDateTime dataUscita) {
        this.dataUscita = dataUscita;
    }

    public Double getPrezzoPagato() {
        return prezzoPagato;
    }

    public void setPrezzoPagato(Double prezzoPagato) {
        this.prezzoPagato = prezzoPagato;
    }

    public String getMetodoPagamentoStorico() {
        return metodoPagamentoStorico;
    }

    public void setMetodoPagamentoStorico(String metodoPagamentoStorico) {
        this.metodoPagamentoStorico = metodoPagamentoStorico;
    }

    public LocalDateTime getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDateTime dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public Double getImporto() {
        return importo;
    }

    public void setImporto(Double importo) {
        this.importo = importo;
    }

    public String getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(String metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }

    public Integer getStatoPagamento() {
        return statoPagamento;
    }

    public void setStatoPagamento(Integer statoPagamento) {
        this.statoPagamento = statoPagamento;
    }
}

