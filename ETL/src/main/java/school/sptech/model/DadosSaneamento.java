package school.sptech.model;

public class DadosSaneamento {

    private Municipio municipio;

    // Água
    private Integer aguaUrbana;
    private Integer aguaRural;

    // Esgoto
    private Integer esgotoUrbano;
    private Integer esgotoRural;

    // Resíduos
    private Integer residuosUrbano;
    private Integer residuosRural;

    // Drenagem
    private Double coberturaRedesPluviais;
    private Double coberturaPavimentacao;
    private Double parcelaDomiciliosRisco;
    private Integer eventosInundacao;
    private Boolean sistemaAlerta;
    private Double indiceDrenagem;

    public DadosSaneamento(Municipio municipio,
                           Integer aguaUrbana, Integer aguaRural,
                           Integer esgotoUrbano, Integer esgotoRural,
                           Integer residuosUrbano, Integer residuosRural,
                           Double coberturaRedesPluviais, Double coberturaPavimentacao,
                           Double parcelaDomiciliosRisco, Integer eventosInundacao,
                           Boolean sistemaAlerta, Double indiceDrenagem) {
        this.municipio = municipio;
        this.aguaUrbana = aguaUrbana;
        this.aguaRural = aguaRural;
        this.esgotoUrbano = esgotoUrbano;
        this.esgotoRural = esgotoRural;
        this.residuosUrbano = residuosUrbano;
        this.residuosRural = residuosRural;
        this.coberturaRedesPluviais = coberturaRedesPluviais;
        this.coberturaPavimentacao = coberturaPavimentacao;
        this.parcelaDomiciliosRisco = parcelaDomiciliosRisco;
        this.eventosInundacao = eventosInundacao;
        this.sistemaAlerta = sistemaAlerta;
        this.indiceDrenagem = indiceDrenagem;
    }


    public Integer getAguaTotal() {
        return (aguaUrbana != null ? aguaUrbana : 0)
                + (aguaRural  != null ? aguaRural  : 0);
    }

    public Integer getEsgotoTotal() {
        return (esgotoUrbano != null ? esgotoUrbano : 0)
                + (esgotoRural  != null ? esgotoRural  : 0);
    }

    public Integer getResiduosTotal() {
        return (residuosUrbano != null ? residuosUrbano : 0)
                + (residuosRural  != null ? residuosRural  : 0);
    }


    public Double getDeficitAgua() {
        if (municipio == null || municipio.getPopulacaoTotal() == null
                || municipio.getPopulacaoTotal() == 0) return null;
        return (1.0 - (double) getAguaTotal() / municipio.getPopulacaoTotal()) * 100.0;
    }

    public Double getDeficitEsgoto() {
        if (municipio == null || municipio.getPopulacaoTotal() == null
                || municipio.getPopulacaoTotal() == 0) return null;
        return (1.0 - (double) getEsgotoTotal() / municipio.getPopulacaoTotal()) * 100.0;
    }

    public Double getDeficitResiduos() {
        if (municipio == null || municipio.getPopulacaoTotal() == null
                || municipio.getPopulacaoTotal() == 0) return null;
        return (1.0 - (double) getResiduosTotal() / municipio.getPopulacaoTotal()) * 100.0;
    }


    public Double getDeficitGeral() {
        Double da = getDeficitAgua();
        Double de = getDeficitEsgoto();
        Double dr = getDeficitResiduos();
        if (da == null || de == null || dr == null) return null;
        return (da + de + dr) / 3.0;
    }

    public String getClassificacaoRisco() {
        if (parcelaDomiciliosRisco == null) return "Desconhecido";
        if (parcelaDomiciliosRisco >= 10.0) return "Alto";
        if (parcelaDomiciliosRisco >= 3.0)  return "Médio";
        return "Baixo";
    }


    public Municipio getMunicipio() { return municipio; }
    public void setMunicipio(Municipio municipio) { this.municipio = municipio; }

    public Integer getAguaUrbana() { return aguaUrbana; }
    public void setAguaUrbana(Integer v) { this.aguaUrbana = v; }

    public Integer getAguaRural() { return aguaRural; }
    public void setAguaRural(Integer v) { this.aguaRural = v; }

    public Integer getEsgotoUrbano() { return esgotoUrbano; }
    public void setEsgotoUrbano(Integer v) { this.esgotoUrbano = v; }

    public Integer getEsgotoRural() { return esgotoRural; }
    public void setEsgotoRural(Integer v) { this.esgotoRural = v; }

    public Integer getResiduosUrbano() { return residuosUrbano; }
    public void setResiduosUrbano(Integer v) { this.residuosUrbano = v; }

    public Integer getResiduosRural() { return residuosRural; }
    public void setResiduosRural(Integer v) { this.residuosRural = v; }

    public Double getCoberturaRedesPluviais() { return coberturaRedesPluviais; }
    public void setCoberturaRedesPluviais(Double v) { this.coberturaRedesPluviais = v; }

    public Double getCoberturaPavimentacao() { return coberturaPavimentacao; }
    public void setCoberturaPavimentacao(Double v) { this.coberturaPavimentacao = v; }

    public Double getParcelaDomiciliosRisco() { return parcelaDomiciliosRisco; }
    public void setParcelaDomiciliosRisco(Double v) { this.parcelaDomiciliosRisco = v; }

    public Integer getEventosInundacao() { return eventosInundacao; }
    public void setEventosInundacao(Integer v) { this.eventosInundacao = v; }

    public Boolean getSistemaAlerta() { return sistemaAlerta; }
    public void setSistemaAlerta(Boolean v) { this.sistemaAlerta = v; }

    public Double getIndiceDrenagem() { return indiceDrenagem; }
    public void setIndiceDrenagem(Double v) { this.indiceDrenagem = v; }
}
