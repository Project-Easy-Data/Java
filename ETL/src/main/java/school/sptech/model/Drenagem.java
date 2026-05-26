package school.sptech.model;

public class Drenagem {
    private Double coberturaRedesPluviais;
    private Double coberturaPavimentacaoMeioFio;
    private Integer domiciliosEmRisco;
    private Double parcelaDomiciliosEmRisco;
    private Double parcelaPopulacaoRiscoHidrologico;
    private Integer eventosInundacao5Anos;
    private Boolean sistemaDeAlerta;

    public Drenagem(Double coberturaRedesPluviais, Double coberturaPavimentacaoMeioFio, Integer domiciliosEmRisco, Double parcelaDomiciliosEmRisco, Double parcelaPopulacaoRiscoHidrologico, Integer eventosInundacao5Anos, Boolean sistemaDeAlerta) {
        this.coberturaRedesPluviais = coberturaRedesPluviais;
        this.coberturaPavimentacaoMeioFio = coberturaPavimentacaoMeioFio;
        this.domiciliosEmRisco = domiciliosEmRisco;
        this.parcelaDomiciliosEmRisco = parcelaDomiciliosEmRisco;
        this.parcelaPopulacaoRiscoHidrologico = parcelaPopulacaoRiscoHidrologico;
        this.eventosInundacao5Anos = eventosInundacao5Anos;
        this.sistemaDeAlerta = sistemaDeAlerta;
    }

    public Double calcularIndiceDrenagem() {
        return (coberturaRedesPluviais + coberturaPavimentacaoMeioFio) / 2.0;
    }

    public Double getCoberturaRedesPluviais() {
        return coberturaRedesPluviais;
    }

    public void setCoberturaRedesPluviais(Double coberturaRedesPluviais) {
        this.coberturaRedesPluviais = coberturaRedesPluviais;
    }

    public Double getCoberturaPavimentacaoMeioFio() {
        return coberturaPavimentacaoMeioFio;
    }

    public void setCoberturaPavimentacaoMeioFio(Double coberturaPavimentacaoMeioFio) {
        this.coberturaPavimentacaoMeioFio = coberturaPavimentacaoMeioFio;
    }

    public Integer getDomiciliosEmRisco() {
        return domiciliosEmRisco;
    }

    public void setDomiciliosEmRisco(Integer domiciliosEmRisco) {
        this.domiciliosEmRisco = domiciliosEmRisco;
    }

    public Double getParcelaDomiciliosEmRisco() {
        return parcelaDomiciliosEmRisco;
    }

    public void setParcelaDomiciliosEmRisco(Double parcelaDomiciliosEmRisco) {
        this.parcelaDomiciliosEmRisco = parcelaDomiciliosEmRisco;
    }

    public Double getParcelaPopulacaoRiscoHidrologico() {
        return parcelaPopulacaoRiscoHidrologico;
    }

    public void setParcelaPopulacaoRiscoHidrologico(Double parcelaPopulacaoRiscoHidrologico) {
        this.parcelaPopulacaoRiscoHidrologico = parcelaPopulacaoRiscoHidrologico;
    }

    public Integer getEventosInundacao5Anos() {
        return eventosInundacao5Anos;
    }

    public void setEventosInundacao5Anos(Integer eventosInundacao5Anos) {
        this.eventosInundacao5Anos = eventosInundacao5Anos;
    }

    public Boolean getSistemaDeAlerta() {
        return sistemaDeAlerta;
    }

    public void setSistemaDeAlerta(Boolean sistemaDeAlerta) {
        this.sistemaDeAlerta = sistemaDeAlerta;
    }
}
