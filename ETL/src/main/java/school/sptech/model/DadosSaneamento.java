package school.sptech.model;

public class DadosSaneamento {
    private Integer anoReferencia;
    private Integer agua;
    private Integer esgoto;
    private Integer residuos;
    private Double drenagem;
    private Municipio municipio;

    public DadosSaneamento(Integer anoReferencia, Integer agua, Integer esgoto, Integer residuos, Double drenagem, Municipio municipio) {
        this.anoReferencia = anoReferencia;
        this.agua = agua;
        this.esgoto = esgoto;
        this.residuos = residuos;
        this.drenagem = drenagem;
        this.municipio = municipio;
    }


    public Integer getAnoReferencia() {
        return anoReferencia;
    }

    public void setAnoReferencia(Integer anoReferencia) {
        this.anoReferencia = anoReferencia;
    }

    public Integer getAgua() {
        return agua;
    }

    public void setAgua(Integer agua) {
        this.agua = agua;
    }

    public Integer getEsgoto() {
        return esgoto;
    }

    public void setEsgoto(Integer esgoto) {
        this.esgoto = esgoto;
    }

    public Integer getResiduos() {
        return residuos;
    }

    public void setResiduos(Integer residuos) {
        this.residuos = residuos;
    }

    public Double getDrenagem() {
        return drenagem;
    }

    public void setDrenagem(Double drenagem) {
        this.drenagem = drenagem;
    }

    public Municipio getMunicipio() {
        return municipio;
    }

    public void setMunicipio(Municipio municipio) {
        this.municipio = municipio;
    }
}
