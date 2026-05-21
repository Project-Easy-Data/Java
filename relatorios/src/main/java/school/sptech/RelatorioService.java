package school.sptech;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class RelatorioService {

    private final JdbcTemplate jdbcTemplate;

    public RelatorioService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Relatorio> getRelatorio(String ano, String uf) {

        String sql =
                "SELECT ano, id_municipio, sigla_uf, " +
                        "populacao_atendida_esgoto, populacao_urbana_residente_esgoto_ibge, " +
                        "populacao_atendida_agua, populacao_urbana_atendida_agua_ibge " +
                        "FROM municipio_agua_esgoto " +
                        "WHERE ano = ? AND sigla_uf = ? " +
                        "LIMIT 3000";

        return jdbcTemplate.query(sql, new Object[]{ano, uf}, (rs, rowNum) -> {
            Relatorio r = new Relatorio();
            r.setAno(rs.getString("ano"));
            r.setIdMunicipio(rs.getString("id_municipio"));
            r.setSiglaUf(rs.getString("sigla_uf"));
            r.setPopulacaoAtentidaEsgoto(rs.getInt("populacao_atendida_esgoto"));
            r.setPopulacaoUrbanaResidenteEsgotoIbge(rs.getInt("populacao_urbana_residente_esgoto_ibge"));
            r.setPopulacaoAtendidaAgua(rs.getInt("populacao_atendida_agua"));
            r.setPopulacaoUrbanaAtendidaAguaIbge(rs.getInt("populacao_urbana_atendida_agua_ibge"));
            return r;
        });
    }

    public void exportarRelatorio(String ano, String uf) throws IOException {

        List<Relatorio> dados = getRelatorio(ano, uf);
        String caminho = "/home/andre/Área de trabalho/relatorio_EasayData_dash.xlsx";

        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(caminho)) {

            Sheet sheet = workbook.createSheet("Municípios");

            Row cab = sheet.createRow(0);
            cab.createCell(0).setCellValue("Ano");
            cab.createCell(1).setCellValue("ID Município");
            cab.createCell(2).setCellValue("UF");
            cab.createCell(3).setCellValue("Pop. Esgoto");
            cab.createCell(4).setCellValue("Pop. Urb. Res. Esgoto");
            cab.createCell(5).setCellValue("Pop. Água");
            cab.createCell(6).setCellValue("Pop. Urb. Água (IBGE)");

            int row = 1;

            for (Relatorio r : dados) {
                Row linha = sheet.createRow(row++);
                linha.createCell(0).setCellValue(r.getAno());
                linha.createCell(1).setCellValue(r.getIdMunicipio());
                linha.createCell(2).setCellValue(r.getSiglaUf());
                linha.createCell(3).setCellValue(r.getPopulacaoAtentidaEsgoto());
                linha.createCell(4).setCellValue(r.getPopulacaoUrbanaResidenteEsgotoIbge());
                linha.createCell(5).setCellValue(r.getPopulacaoAtendidaAgua());
                linha.createCell(6).setCellValue(r.getPopulacaoUrbanaAtendidaAguaIbge());
            }

            workbook.write(fos);

            System.out.println("Relatório gerado a partir do dashboard EasyData: " + caminho);
        }
    }
}

//Aplicar no botão do dash:

//@GetMapping("/relatorio/exportar")
//public ResponseEntity<Void> exportar(@RequestParam String ano,
//                                     @RequestParam String uf) throws IOException {
//    relatorioService.exportarRelatorio(ano, uf);
//    return ResponseEntity.ok().build();
//}