package school.sptech.service;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStrings;
import org.springframework.jdbc.core.JdbcTemplate;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.util.*;

public class ExcelService {
    private static final Logger log = LoggerFactory.getLogger(ExcelService.class);
    private final JdbcTemplate jdbc;
    private static final int TAMANHO_LOTE = 500;

    public ExcelService(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("DROP TABLE IF EXISTS municipios");
        jdbc.execute("CREATE TABLE municipios (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "ano INT, id_municipio INT, sigla_uf VARCHAR(2), " +
                "populacao_atendida_agua INT, populacao_atendida_esgoto INT, populacao_urbana INT, " +
                "populacao_urbana_residente_agua INT, populacao_urbana_atendida_agua INT, populacao_urbana_atendida_agua_ibge INT, " +
                "populacao_urbana_residente_esgoto INT, populacao_urbana_atendida_esgoto INT, populacao_urbana_residente_esgoto_ibge INT" +
                ")");
        log.info("Tabela criada.");
    }

    public void processar(InputStream inputStream) throws Exception {
        log.info("Iniciando leitura da planilha (modo streaming)...");


        OPCPackage pkg = OPCPackage.open(inputStream);
        try {
            XSSFReader reader = new XSSFReader(pkg);
            SharedStrings textos = reader.getSharedStringsTable();
            XMLReader parser = XMLReaderFactory.createXMLReader();
            PlanilhaHandler handler = new PlanilhaHandler(textos);
            parser.setContentHandler(handler);


            InputStream sheetData = reader.getSheetsData().next();
            parser.parse(new InputSource(sheetData));
            sheetData.close();


            handler.salvarLoteFinal();
            log.info("Fim da planilha. Registros inseridos: {}", handler.getTotal());
        } finally {
            pkg.close();
        }
    }


    private class PlanilhaHandler extends DefaultHandler {
        private final SharedStrings textos;
        private String valorCelula = "";
        private int indiceColuna = -1;
        private boolean ehTexto;
        private String[] linhaAtual = new String[13];
        private boolean primeiraLinha = true;
        private List<String[]> lote = new ArrayList<>();
        private int totalInserido = 0;

        public PlanilhaHandler(SharedStrings textos) {
            this.textos = textos;
        }

        public int getTotal() { return totalInserido; }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attrs) {
            if ("c".equals(qName)) {
                String tipo = attrs.getValue("t");
                ehTexto = "s".equals(tipo);
                valorCelula = "";
                indiceColuna++;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {

            valorCelula += new String(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if ("v".equals(qName)) {
                if (ehTexto && !valorCelula.isEmpty()) {
                    int idx = Integer.parseInt(valorCelula);
                    valorCelula = textos.getItemAt(idx).toString();
                }

                if (indiceColuna >= 0 && indiceColuna < 13) {
                    linhaAtual[indiceColuna] = valorCelula;
                }
            } else if ("row".equals(qName)) {
                if (primeiraLinha) {
                    primeiraLinha = false;
                } else if (linhaAtual[0] != null && !linhaAtual[0].isEmpty()) {

                    lote.add(linhaAtual.clone());
                    if (lote.size() >= TAMANHO_LOTE) {
                        salvarLote();
                    }
                }

                indiceColuna = -1;
                linhaAtual = new String[13];
            }
        }

        private void salvarLote() {
            String sql = "INSERT INTO municipios (" +
                    "ano, id_municipio, sigla_uf, " +
                    "populacao_atendida_agua, populacao_atendida_esgoto, populacao_urbana, " +
                    "populacao_urbana_residente_agua, populacao_urbana_atendida_agua, populacao_urbana_atendida_agua_ibge, " +
                    "populacao_urbana_residente_esgoto, populacao_urbana_atendida_esgoto, populacao_urbana_residente_esgoto_ibge" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            List<Object[]> batch = new ArrayList<>();
            for (String[] linha : lote) {
                batch.add(new Object[]{
                        parseInt(linha[0]), parseInt(linha[1]), linha[2],
                        parseInt(linha[3]), parseInt(linha[4]), parseInt(linha[5]),
                        parseInt(linha[6]), parseInt(linha[7]), parseInt(linha[8]),
                        parseInt(linha[9]), parseInt(linha[10]), parseInt(linha[11])
                });
            }
            jdbc.batchUpdate(sql, batch);
            totalInserido += lote.size();
            log.info("Lote inserido. Total até agora: {}", totalInserido);
            lote.clear();
        }


        void salvarLoteFinal() {
            if (!lote.isEmpty()) {
                salvarLote();
            }
        }

        private int parseInt(String s) {
            try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
        }
    }
}