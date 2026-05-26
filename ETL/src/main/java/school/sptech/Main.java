package school.sptech;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import school.sptech.service.DadosSaneamentoService;
import school.sptech.service.DadosSaneamentoService.ResultadoCarregamento;

import java.io.FileInputStream;
import java.io.InputStream;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            
            InputStream base01 = new FileInputStream("/home/andre/Downloads/municipio_saneamento_atualizado.xlsx");
            InputStream base02 = new FileInputStream("/home/andre/Downloads/br_mdr_snis_municipio_agua_esgoto_atualizado.xlsx");

            log.info("Carregando bases...");
            ResultadoCarregamento resultado = DadosSaneamentoService.carregarTodasBases(base01, base02);

            log.info("Municípios carregados: {}", resultado.mapaMunicipios.size());
            log.info("Registros de saneamento: {}", resultado.dadosSaneamento.size());

            resultado.dadosSaneamento.stream()
                    .limit(5)
                    .forEach(d -> System.out.println(
                            "Ano: " + d.getAnoReferencia() +
                                    " | Cidade: " + d.getMunicipio().getNome() +
                                    " | UF: " + d.getMunicipio().getUf().getSigla() +
                                    " | Água: " + d.getAgua() +
                                    " | Esgoto: " + d.getEsgoto() +
                                    " | Resíduos: " + d.getResiduos() +
                                    " | Drenagem: " + d.getDrenagem()
                    ));

            log.info("Teste concluído com sucesso!");
        } catch (Exception e) {
            log.error("Erro: ", e);
        }
    }
}