package school.sptech;

public class Relatorio {

    private String ano;
    private String idMunicipio;
    private String siglaUf;
    private int populacaoAtentidaEsgoto;
    private int populacaoUrbanaResidenteEsgotoIbge;
    private int populacaoAtendidaAgua;
    private int populacaoUrbanaAtendidaAguaIbge;

    public Relatorio() {}

    public Relatorio(String ano, String idMunicipio, String siglaUf) {
        this.ano = ano;
        this.idMunicipio = idMunicipio;
        this.siglaUf = siglaUf;
    }

    public String getAno() {
        return ano;
    }
    public void setAno(String ano) {
        this.ano = ano;
    }

    public String getIdMunicipio() {
        return idMunicipio;
    }
    public void setIdMunicipio(String idMunicipio) {
        this.idMunicipio = idMunicipio;
    }

    public String getSiglaUf() {
        return siglaUf;
    }
    public void setSiglaUf(String siglaUf) {
        this.siglaUf = siglaUf;
    }

    public int getPopulacaoAtentidaEsgoto() {
        return populacaoAtentidaEsgoto;
    }
    public void setPopulacaoAtentidaEsgoto(int v) {
        this.populacaoAtentidaEsgoto = v;
    }

    public int getPopulacaoUrbanaResidenteEsgotoIbge() {
        return populacaoUrbanaResidenteEsgotoIbge;
    }
    public void setPopulacaoUrbanaResidenteEsgotoIbge(int v) {
        this.populacaoUrbanaResidenteEsgotoIbge = v;
    }

    public int getPopulacaoAtendidaAgua() {
        return populacaoAtendidaAgua;
    }
    public void setPopulacaoAtendidaAgua(int v) {
        this.populacaoAtendidaAgua = v;
    }

    public int getPopulacaoUrbanaAtendidaAguaIbge() {
        return populacaoUrbanaAtendidaAguaIbge;
    }
    public void setPopulacaoUrbanaAtendidaAguaIbge(int v) {
        this.populacaoUrbanaAtendidaAguaIbge = v;
    }
}