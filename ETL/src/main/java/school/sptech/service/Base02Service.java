package school.sptech.service;

import org.apache.poi.ss.usermodel.*;
import school.sptech.model.*;

import java.io.InputStream;
import java.util.*;

public class Base02Service {

    public List<DadosSaneamento> processar(InputStream inputStream, Map<Integer, Municipio> mapaMunicipios) throws Exception {

        List<DadosSaneamento> lista = new ArrayList<>();

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

                String anoStr = getCellValue(row, cabecalho, "ano");
                if (anoStr.isEmpty()) continue;

                Integer ano = (int) Double.parseDouble(anoStr);
                Integer idMunicipio = (int) Double.parseDouble(getCellValue(row, cabecalho, "id_municipio"));
                Integer agua = (int) Double.parseDouble(getCellValue(row, cabecalho, "populacao_atendida_agua").replace(",", "."));
                Integer esgoto = (int) Double.parseDouble(getCellValue(row, cabecalho, "populacao_atentida_esgoto").replace(",", "."));
                String siglaUf = getCellValue(row, cabecalho, "sigla_uf");

                Municipio municipio = mapaMunicipios.get(idMunicipio);
                if (municipio == null) {
                    municipio = new Municipio(idMunicipio, "Desconhecido", new UnidadeFederativa(0, "Desconhecida", siglaUf));
                }
                lista.add(new DadosSaneamento(ano, agua, esgoto, 0, 0.0, municipio));
            }
        }
        return lista;
    }

    private String getCellValue(Row row, Map<String, Integer> cabecalho, String nomeColuna) {
        Integer idx = cabecalho.get(nomeColuna);
        if (idx == null) return "";
        Cell cell = row.getCell(idx);
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }
}