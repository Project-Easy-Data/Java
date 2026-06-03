package school.sptech.model;

public class Municipio extends Localidade {
    private UnidadeFederativa uf;
    private Integer populacaoTotal;
    private Integer populacaoUrbana;
    private Integer populacaoRural;
    private String regiao;

    public Municipio(Integer codigoIbge, String nome, UnidadeFederativa uf,
                     Integer populacaoTotal, Integer populacaoUrbana,
                     Integer populacaoRural, String regiao) {
        super(codigoIbge, nome);
        this.uf = uf;
        this.populacaoTotal = populacaoTotal;
        this.populacaoUrbana = populacaoUrbana;
        this.populacaoRural = populacaoRural;
        this.regiao = regiao;
    }

    public UnidadeFederativa getUf() { return uf; }
    public void setUf(UnidadeFederativa uf) { this.uf = uf; }

    public Integer getPopulacaoTotal() { return populacaoTotal; }
    public void setPopulacaoTotal(Integer populacaoTotal) { this.populacaoTotal = populacaoTotal; }

    public Integer getPopulacaoUrbana() { return populacaoUrbana; }
    public void setPopulacaoUrbana(Integer populacaoUrbana) { this.populacaoUrbana = populacaoUrbana; }

    public Integer getPopulacaoRural() { return populacaoRural; }
    public void setPopulacaoRural(Integer populacaoRural) { this.populacaoRural = populacaoRural; }

    public String getRegiao() { return regiao; }
    public void setRegiao(String regiao) { this.regiao = regiao; }
}
