package school.sptech.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import school.sptech.model.*;

import java.io.InputStream;
import java.util.*;

public class Base01Service {
    private static final int ANO_PADRAO = 2021;

    public Map<Integer, Municipio> mapaMunicipios = new HashMap<>();
    public List<DadosSaneamento> dadosSaneamento = new ArrayList<>();

    public void processar(InputStream inputStream) throws Exception {
        IOUtils.setByteArrayMaxOverride(500_000_000);

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            Map<String, Integer> cabecalho = new LinkedHashMap<>();
            Row headerRow = sheet.getRow(0);
            for (Cell cell : headerRow) {
                cabecalho.put(cell.toString().trim(), cell.getColumnIndex());
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String codIbgeStr = getCellValue(row, cabecalho, "Código IBGE");
                if (codIbgeStr.isEmpty()) continue;

                int codIbge = Integer.parseInt(codIbgeStr);
                String nomeCidade = getCellValue(row, cabecalho, "Cidade");
                String siglaUf = getCellValue(row, cabecalho, "UF");
                String nomeEstado = getCellValue(row, cabecalho, "Estado");

                // Cria a UF e o Município
                UnidadeFederativa uf = new UnidadeFederativa(0, nomeEstado, siglaUf);
                Municipio municipio = new Municipio(codIbge, nomeCidade, uf);
                mapaMunicipios.put(codIbge, municipio);

                // Leitura dos dados de saneamento
                Integer agua = parseInt(getCellValue(row, cabecalho, "População Total Atendida com Abastecimento de Água (habitantes)"));
                Integer esgoto = parseInt(getCellValue(row, cabecalho, "População Total Atendida com Esgotamento Sanitário (habitantes)"));
                Integer residuos = parseInt(getCellValue(row, cabecalho, "População Total Atendida por Coleta de Resíduos Domiciliares (habitantes)"));

                // Leitura dos dados de drenagem e risco
                Double coberturaRedes = parseDouble(getCellValue(row, cabecalho, "Taxa de cobertura de vias públicas com redes ou canais pluviais subterrâneos, na área urbana (%)"));
                Double coberturaPav = parseDouble(getCellValue(row, cabecalho, "Taxa de cobertura de vias públicas com pavimentação e meio-fio, na área urbana (%)"));
                Integer domRisco = parseInt(getCellValue(row, cabecalho, "Quantidade de Domicílios sujeitos a Risco de Inundação"));
                Double parcDomRisco = parseDouble(getCellValue(row, cabecalho, "Parcela de Domicílios com Risco de Inundação"));
                Double parcPopRisco = parseDouble(getCellValue(row, cabecalho, "Parcela da população com Risco de Eventos Hidrológicos"));
                Integer eventos = parseInt(getCellValue(row, cabecalho, "Quantidade de Enxurradas, Alagamentos e Inundação nos últimos 5 anos"));
                Boolean alerta = "Sim".equalsIgnoreCase(getCellValue(row, cabecalho, "Existem sistemas de alerta de riscos hidrológicos (alagamentos, enxurradas, inundações)"));

                Drenagem drenagem = new Drenagem(coberturaRedes, coberturaPav,
                        domRisco, parcDomRisco, parcPopRisco, eventos, alerta);
                Double indiceDrenagem = drenagem.calcularIndiceDrenagem();

                DadosSaneamento dados = new DadosSaneamento(ANO_PADRAO, agua, esgoto, residuos, indiceDrenagem, municipio);
                dadosSaneamento.add(dados);
            }
        }
    }

    private String getCellValue(Row row, Map<String, Integer> cabecalho, String nomeColuna) {
        Integer idx = cabecalho.get(nomeColuna);
        if (idx == null) return "";
        Cell cell = row.getCell(idx);
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    private Integer parseInt(String s) {
        if (s.isEmpty()) return 0;
        try { return (int) Double.parseDouble(s.replace(",", ".")); }
        catch (NumberFormatException e) { return 0; }
    }

    private Double parseDouble(String s) {
        if (s.isEmpty()) return 0.0;
        try { return Double.parseDouble(s.replace(",", ".")); }
        catch (NumberFormatException e) { return 0.0; }
    }
}