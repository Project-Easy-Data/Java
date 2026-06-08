import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Slack {
    private static final String URL_WEBHOOK_SLACK = System.getenv("SLACK_WEBHOOK_URL");

    public static void enviarMensagemSlack(String texto) {
        try {
            if (URL_WEBHOOK_SLACK == null || URL_WEBHOOK_SLACK.isBlank()) {
                throw new RuntimeException("SLACK_WEBHOOK_URL nao configurada.");
            }

            HttpClient cliente = HttpClient.newHttpClient();

            String textoSeguro = texto
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n");

            String jsonBody = String.format("{\"text\":\"%s\"}", textoSeguro);

            HttpRequest requisicao = HttpRequest.newBuilder()
                    .uri(URI.create(URL_WEBHOOK_SLACK))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> resposta = cliente.send(
                    requisicao,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (resposta.statusCode() == 200) {
                System.out.println("Notificacao enviada para o Slack com sucesso!");
            } else {
                System.err.println("Falha ao enviar para o Slack. Status: " + resposta.statusCode());
                System.err.println(resposta.body());
            }
        } catch (Exception erro) {
            System.err.println("Erro ao conectar com o Slack: " + erro.getMessage());
        }
    }

    private static boolean verificarStatusSlack() {
        try {
            String urlStatus = System.getenv().getOrDefault(
                    "BACKEND_URL",
                    "http://app:3000"
            ) + "/slack/status";

            HttpClient cliente = HttpClient.newHttpClient();
            HttpRequest requisicao = HttpRequest.newBuilder()
                    .uri(URI.create(urlStatus))
                    .GET()
                    .build();

            HttpResponse<String> resposta = cliente.send(
                    requisicao,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (resposta.statusCode() == 200) {
                String body = resposta.body();
                return body.contains("\"ativo\":true");
            }
        } catch (Exception e) {
            System.err.println("Erro ao verificar status do Slack: " + e.getMessage());
        }
        return true; // Por padrão, assume que está ativo se não conseguir verificar
    }

    public static void main(String[] args) {
        // Verificar se o Slack está ativo antes de processar
        if (!verificarStatusSlack()) {
            System.out.println("Slack desativado pelo usuário. Ignorando envio de notificações.");
            return;
        }

        String urlConexao = System.getenv().getOrDefault(
                "DB_URL",
                "jdbc:mysql://container-banco:3306/EasyData?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
        );

        String usuario = System.getenv().getOrDefault("DB_USER", "Easy");
        String senha = System.getenv().getOrDefault("DB_PASSWORD", "Easydata@2026");

        try (
                Connection conexao = DriverManager.getConnection(urlConexao, usuario, senha);
                Statement comando = conexao.createStatement()
        ) {
            String sql = "SELECT *\n" +
                    "FROM (\n" +
                    "    SELECT id, data, tipo, motivo\n" +
                    "    FROM Log\n" +
                    "    ORDER BY id DESC\n" +
                    "    LIMIT 15\n" +
                    ") ultimos\n" +
                    "ORDER BY id ASC;";
            
            ResultSet resultado = comando.executeQuery(sql);

            StringBuilder mensagemSlack = new StringBuilder();
            mensagemSlack.append("*Ultimos logs do BD EasyData:*\n");

            boolean encontrouLog = false;

            while (resultado.next()) {
                encontrouLog = true;

                int id = resultado.getInt("id");
                String data = resultado.getString("data");
                String tipo = resultado.getString("tipo");
                String motivo = resultado.getString("motivo");


                mensagemSlack
                        .append("- #")
                        .append(id)
                        .append(" | ")
                        .append(data)
                        .append(" | ")
                        .append(tipo)
                        .append(" | ")
                        .append(motivo)
                        .append("\n");
            }

            if (!encontrouLog) {
                mensagemSlack.append("Nenhum log encontrado.");
            }

            enviarMensagemSlack(mensagemSlack.toString());

        } catch (Exception e) {
            e.printStackTrace();
            enviarMensagemSlack("Falha ao buscar logs no banco: " + e.getMessage());
        }
    }
}
