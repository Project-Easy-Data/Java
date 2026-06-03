package school.sptech;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import school.sptech.config.DBConexao;
import school.sptech.service.BaseService;
import school.sptech.service.S3Service;

import java.io.FileInputStream;
import java.io.InputStream;

public class Main {
    public static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            // LOCAL
            //String pathBase01 = System.getenv().getOrDefault(
            //        "BASE01_PATH",
            //        "/home/andre/Downloads/municipio_saneamento_atualizado.xlsx"
            //);
            //InputStream base01 = new FileInputStream(pathBase01);

            // AWS
            S3Service s3Service = new S3Service();
            InputStream base01 = s3Service.obterArquivo("municipio_saneamento_atualizado.xlsx");

            DBConexao db = new DBConexao();

            log.info("Carregando base...");
            Long inicio = System.currentTimeMillis();

            BaseService service = new BaseService(db.getConexao());
            service.processar(base01);

            Long fim = System.currentTimeMillis();
            Long tempoTotal = fim - inicio;

            log.info("ETL concluído em {} segundos!", tempoTotal / 1000);
        } catch (Exception e) {
            log.error("Erro: ", e);
        }
    }
}