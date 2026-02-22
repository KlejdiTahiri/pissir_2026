package Model;

import java.time.LocalDateTime;

public class Biglietto {
    private Long id;
    private String targa;
    private String capIngresso;
    private Integer idCaselloIngresso;
    private String direzioneIngresso;
    private LocalDateTime dataIngresso;
    private Boolean valido;

    // Getters e setters
    public void setId(Long id) { this.id = id; }
    public void setTarga(String targa) { this.targa = targa; }
    public void setCapIngresso(String capIngresso) { this.capIngresso = capIngresso; }
    public void setIdCaselloIngresso(Integer idCaselloIngresso) { this.idCaselloIngresso = idCaselloIngresso; }
    public void setDirezioneIngresso(String direzioneIngresso) { this.direzioneIngresso = direzioneIngresso; }
    public void setDataIngresso(LocalDateTime dataIngresso) { this.dataIngresso = dataIngresso; }
    public void setValido(Boolean valido) { this.valido = valido; }

    // (Getters opzionali se ti servono)

    public Long getId() { return id; }
    public String getTarga() { return targa; }
    public String getCapIngresso() { return capIngresso; }
    public Integer getIdCaselloIngresso() { return idCaselloIngresso; }
    public String getDirezioneIngresso() { return direzioneIngresso; }
    public LocalDateTime getDataIngresso() { return dataIngresso; }
    public Boolean getValido() { return valido; }


}
