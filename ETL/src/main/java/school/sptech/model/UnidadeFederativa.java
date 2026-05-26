package school.sptech.model;

public class UnidadeFederativa extends Localidade{
    private String sigla;

    public UnidadeFederativa(Integer codigo, String nome, String sigla) {
        super(codigo, nome);
        this.sigla = sigla;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }
}
