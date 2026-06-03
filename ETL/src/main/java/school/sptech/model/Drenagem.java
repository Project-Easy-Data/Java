package school.sptech.model;

public class Drenagem {
    private Double coberturaRedesPluviais;
    private Double coberturaPavimentacao;
    private Integer domiciliosEmRisco;
    private Double parcelaDomiciliosEmRisco;
    private Double parcelaPopulacaoRiscoHidrologico;
    private Integer eventosInundacao5Anos;
    private Boolean sistemaDeAlerta;

    public Drenagem(Double coberturaRedesPluviais, Double coberturaPavimentacao,
                    Integer domiciliosEmRisco, Double parcelaDomiciliosEmRisco,
                    Double parcelaPopulacaoRiscoHidrologico,
                    Integer eventosInundacao5Anos, Boolean sistemaDeAlerta) {
        this.coberturaRedesPluviais = coberturaRedesPluviais;
        this.coberturaPavimentacao = coberturaPavimentacao;
        this.domiciliosEmRisco = domiciliosEmRisco;
        this.parcelaDomiciliosEmRisco = parcelaDomiciliosEmRisco;
        this.parcelaPopulacaoRiscoHidrologico = parcelaPopulacaoRiscoHidrologico;
        this.eventosInundacao5Anos = eventosInundacao5Anos;
        this.sistemaDeAlerta = sistemaDeAlerta;
    }

    public Double calcularIndiceDrenagem() {
        return (coberturaRedesPluviais + coberturaPavimentacao) / 2.0;
    }

    public Double getCoberturaRedesPluviais() { return coberturaRedesPluviais; }
    public void setCoberturaRedesPluviais(Double v) { this.coberturaRedesPluviais = v; }

    public Double getCoberturaPavimentacao() { return coberturaPavimentacao; }
    public void setCoberturaPavimentacao(Double v) { this.coberturaPavimentacao = v; }

    public Integer getDomiciliosEmRisco() { return domiciliosEmRisco; }
    public void setDomiciliosEmRisco(Integer v) { this.domiciliosEmRisco = v; }

    public Double getParcelaDomiciliosEmRisco() { return parcelaDomiciliosEmRisco; }
    public void setParcelaDomiciliosEmRisco(Double v) { this.parcelaDomiciliosEmRisco = v; }

    public Double getParcelaPopulacaoRiscoHidrologico() { return parcelaPopulacaoRiscoHidrologico; }
    public void setParcelaPopulacaoRiscoHidrologico(Double v) { this.parcelaPopulacaoRiscoHidrologico = v; }

    public Integer getEventosInundacao5Anos() { return eventosInundacao5Anos; }
    public void setEventosInundacao5Anos(Integer v) { this.eventosInundacao5Anos = v; }

    public Boolean getSistemaDeAlerta() { return sistemaDeAlerta; }
    public void setSistemaDeAlerta(Boolean v) { this.sistemaDeAlerta = v; }
}
