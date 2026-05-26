package school.sptech.service;

import school.sptech.model.DadosSaneamento;
import school.sptech.model.Municipio;

import java.io.InputStream;
import java.util.*;

public class DadosSaneamentoService {

    public static ResultadoCarregamento carregarTodasBases(InputStream base01, InputStream base02) throws Exception {
        Base01Service s1 = new Base01Service();
        s1.processar(base01);

        Base02Service s2 = new Base02Service();
        List<DadosSaneamento> lista02 = s2.processar(base02, s1.mapaMunicipios);

        List<DadosSaneamento> todos = new ArrayList<>(s1.dadosSaneamento);
        todos.addAll(lista02);

        return new ResultadoCarregamento(s1.mapaMunicipios, todos);
    }

    public static class ResultadoCarregamento {
        public final Map<Integer, Municipio> mapaMunicipios;
        public final List<DadosSaneamento> dadosSaneamento;

        public ResultadoCarregamento(Map<Integer, Municipio> mapa, List<DadosSaneamento> dados) {
            this.mapaMunicipios = mapa;
            this.dadosSaneamento = dados;
        }
    }
}