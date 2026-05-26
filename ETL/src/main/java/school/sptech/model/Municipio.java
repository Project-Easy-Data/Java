package school.sptech.model;

public class Municipio extends Localidade{
    private UnidadeFederativa uf;

    public Municipio(Integer codigo, String nome, UnidadeFederativa uf) {
        super(codigo, nome);
        this.uf = uf;
    }

    public UnidadeFederativa getUf() {
        return uf;
    }

    public void setUf(UnidadeFederativa uf) {
        this.uf = uf;
    }
}