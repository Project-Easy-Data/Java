package school.sptech.config;

import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;

public class DBConexao {
    private DataSource conexao;

    public DBConexao() {
        BasicDataSource ds = new BasicDataSource();

        String url = System.getenv().getOrDefault(
                "DB_URL",
                "jdbc:mysql://container-banco:3306/EasyData?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
        );

        String user = System.getenv().getOrDefault("DB_USER", "Easy");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "Easydata@2026");

        ds.setUrl(url);
        ds.setUsername(user);
        ds.setPassword(password);
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setInitialSize(5);
        ds.setMaxTotal(10);

        this.conexao = ds;
    }

    public DataSource getConexao() {
        return this.conexao;
    }
}