package school.sptech.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import school.sptech.model.DadosSaneamento;
import school.sptech.model.Drenagem;
import school.sptech.model.Municipio;
import school.sptech.model.UnidadeFederativa;

import javax.sql.DataSource;
import java.io.InputStream;
import java.util.*;

public class BaseService {
    private static final Logger log = LoggerFactory.getLogger(BaseService.class);
    private final JdbcTemplate jdbc;

    public BaseService(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);

        jdbc.execute("DROP TABLE IF EXISTS dados_saneamento");
        jdbc.execute("DROP TABLE IF EXISTS municipio");
        jdbc.execute("DROP TABLE IF EXISTS unidade_federativa");

        jdbc.execute("CREATE TABLE unidade_federativa (" +
                "id    INT AUTO_INCREMENT PRIMARY KEY, " +
                "codigo INT, " +
                "nome  VARCHAR(100) NOT NULL, " +
                "sigla VARCHAR(2) NOT NULL UNIQUE" +
                ")");

        jdbc.execute("CREATE TABLE municipio (" +
                "id               INT AUTO_INCREMENT PRIMARY KEY, " +
                "codigo_ibge      INT NOT NULL UNIQUE, " +
                "nome             VARCHAR(150) NOT NULL, " +
                "sigla_uf         VARCHAR(2) NOT NULL, " +
                "populacao_total  INT, " +
                "populacao_urbana INT, " +
                "populacao_rural  INT, " +
                "regiao           VARCHAR(50), " +
                "FOREIGN KEY (sigla_uf) REFERENCES unidade_federativa(sigla)" +
                ")");

        jdbc.execute("CREATE TABLE dados_saneamento (" +
                "id                       INT AUTO_INCREMENT PRIMARY KEY, " +
                "id_municipio             INT NOT NULL UNIQUE, " +
                "agua_urbana              INT, " +
                "agua_rural               INT, " +
                "esgoto_urbano            INT, " +
                "esgoto_rural             INT, " +
                "residuos_urbano          INT, " +
                "residuos_rural           INT, " +
                "cobertura_redes_pluviais DOUBLE, " +
                "cobertura_pavimentacao   DOUBLE, " +
                "parcela_domicilios_risco DOUBLE, " +
                "eventos_inundacao        INT, " +
                "sistema_alerta           BOOLEAN, " +
                "indice_drenagem          DOUBLE, " +
                "FOREIGN KEY (id_municipio) REFERENCES municipio(id)" +
                ")");

        log.info("Tabelas criadas com sucesso.");
    }

    public void processar(InputStream inputStream) throws Exception {
        IOUtils.setByteArrayMaxOverride(500_000_000);
        log.info("Iniciando leitura da planilha...");

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            Map<String, Integer> cab = new LinkedHashMap<>();
            Integer inseridos = 0;

            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    for (Cell cell : row) {
                        cab.put(cell.toString().trim(), cell.getColumnIndex());
                    }
                    log.info("Cabeçalho lido. Colunas encontradas: {}", cab.size());
                    continue;
                }

                String codIbgeStr = getCellValue(row, cab, "Código IBGE");
                if (codIbgeStr.isEmpty()) continue;



                UnidadeFederativa uf = new UnidadeFederativa(
                        0,
                        getCellValue(row, cab, "Estado"),
                        getCellValue(row, cab, "UF")
                );

                Municipio municipio = new Municipio(
                        parseInt(codIbgeStr),
                        getCellValue(row, cab, "Cidade"),
                        uf,
                        parseInt(getCellValue(row, cab, "População em 2021")),
                        parseInt(getCellValue(row, cab, "População Urbana - Habitantes")),
                        parseInt(getCellValue(row, cab, "População Rural - Habitantes")),
                        getCellValue(row, cab, "Região")
                );

                Drenagem drenagem = new Drenagem(
                        parseDouble(getCellValue(row, cab, "Taxa de cobertura de vias públicas com redes ou canais pluviais subterrâneos, na área urbana (%)")),
                        parseDouble(getCellValue(row, cab, "Taxa de cobertura de vias públicas com pavimentação e meio-fio, na área urbana (%)")),
                        parseInt(getCellValue(row, cab, "Quantidade de Domicílios sujeitos a Risco de Inundação")),
                        parseDouble(getCellValue(row, cab, "Parcela de Domicílios com Risco de Inundação")),
                        parseDouble(getCellValue(row, cab, "Parcela da população com Risco de Eventos Hidrológicos")),
                        parseInt(getCellValue(row, cab, "Quantidade de Enxurradas, Alagamentos e Inundação nos últimos 5 anos")),
                        "Sim".equalsIgnoreCase(getCellValue(row, cab, "Existem sistemas de alerta de riscos hidrológicos (alagamentos, enxurradas, inundações)"))
                );

                DadosSaneamento dados = new DadosSaneamento(
                        municipio,
                        parseInt(getCellValue(row, cab, "População Urbana Atendida com Abastecimento de Água (habitantes)")),
                        parseInt(getCellValue(row, cab, "População Rural Atendida com Abastecimento de Água (habitantes)")),
                        parseInt(getCellValue(row, cab, "População Urbana Atendida com Esgotamento Sanitário (habitantes)")),
                        parseInt(getCellValue(row, cab, "População Rural Atendida com Esgotamento Sanitário (habitantes)")),
                        parseInt(getCellValue(row, cab, "População Urbana Atendida por Coleta de Resíduos Domiciliares (habitantes)")),
                        parseInt(getCellValue(row, cab, "População Rural Atendida por Coleta de Resíduos Domiciliares (habitantes)")),
                        drenagem.getCoberturaRedesPluviais(),
                        drenagem.getCoberturaPavimentacao(),
                        drenagem.getParcelaDomiciliosEmRisco(),
                        drenagem.getEventosInundacao5Anos(),
                        drenagem.getSistemaDeAlerta(),
                        drenagem.calcularIndiceDrenagem()
                );

                // Inserindo no banco
                jdbc.update("INSERT IGNORE INTO unidade_federativa (codigo, nome, sigla) VALUES (?, ?, ?)",
                        uf.getCodigo(),
                        uf.getNome(),
                        uf.getSigla());

                jdbc.update("INSERT IGNORE INTO municipio (codigo_ibge, nome, sigla_uf, populacao_total, populacao_urbana, populacao_rural, regiao) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?)",
                        municipio.getCodigo(),
                        municipio.getNome(),
                        municipio.getUf().getSigla(),
                        municipio.getPopulacaoTotal(),
                        municipio.getPopulacaoUrbana(),
                        municipio.getPopulacaoRural(),
                        municipio.getRegiao());

                Integer idMunicipio = jdbc.queryForObject(
                        "SELECT id FROM municipio WHERE codigo_ibge = ?",
                        Integer.class, municipio.getCodigo());

                jdbc.update("INSERT IGNORE INTO dados_saneamento " +
                                "(id_municipio, agua_urbana, agua_rural, esgoto_urbano, esgoto_rural, " +
                                "residuos_urbano, residuos_rural, cobertura_redes_pluviais, cobertura_pavimentacao, " +
                                "parcela_domicilios_risco, eventos_inundacao, sistema_alerta, indice_drenagem) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        idMunicipio,
                        dados.getAguaUrbana(),
                        dados.getAguaRural(),
                        dados.getEsgotoUrbano(),
                        dados.getEsgotoRural(),
                        dados.getResiduosUrbano(),
                        dados.getResiduosRural(),
                        dados.getCoberturaRedesPluviais(),
                        dados.getCoberturaPavimentacao(),
                        dados.getParcelaDomiciliosRisco(),
                        dados.getEventosInundacao(),
                        dados.getSistemaAlerta(),
                        dados.getIndiceDrenagem());

                inseridos++;

                if (inseridos % 500 == 0) {
                    log.info("Registros inseridos até agora: {}", inseridos);
                }
            }

            log.info("Fim da planilha. Total inserido: {}", inseridos);
        }
    }

    private String getCellValue(Row row, Map<String, Integer> cab, String coluna) {
        Integer idx = cab.get(coluna);
        if (idx == null) return "";
        Cell cell = row.getCell(idx);
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    private Integer parseInt(String s) {
        if (s == null || s.isEmpty()) return 0;
        try { return (int) Double.parseDouble(s.replace(",", ".")); }
        catch (NumberFormatException e) { return 0; }
    }

    private Double parseDouble(String s) {
        if (s == null || s.isEmpty()) return 0.0;
        try { return Double.parseDouble(s.replace(",", ".")); }
        catch (NumberFormatException e) { return 0.0; }
    }
}