package hexlet.code;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.output.StringOutput;
import gg.jte.resolve.ResourceCodeResolver;
import hexlet.code.controller.UrlCheckController;
import hexlet.code.controller.UrlController;
import hexlet.code.util.DataSourceFactory;
import io.javalin.Javalin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    private static TemplateEngine createTemplateEngine() {
        var codeResolver = new ResourceCodeResolver(
                "templates", App.class.getClassLoader()
        );
        return TemplateEngine.create(codeResolver, ContentType.Html);
    }

    private static String readResourceFile(String fileName) throws IOException {
        var classLoader = App.class.getClassLoader();
        try (var inputStream = classLoader.getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + fileName);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void initSchema(DataSource dataSource) {
        try (var conn = dataSource.getConnection();
             var stmt = conn.createStatement()) {

            var sql = readResourceFile("schema.sql");

            for (var statement : sql.split(";")) {
                if (!statement.isBlank()) {
                    stmt.execute(statement);
                }
            }

        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Javalin getApp() {

        var dataSource = DataSourceFactory.getDataSource();
        initSchema(dataSource);

        var templateEngine = createTemplateEngine();

        var app = Javalin.create(config -> {
            config.fileRenderer((filePath, model, context) -> {
                var output = new StringOutput();
                Map<String, Object> map = modelToMap(model);
                templateEngine.render(filePath, map, output);
                return output.toString();
            });
        });

        app.get("/", UrlController::root);
        app.post("/urls", UrlController::create);
        app.get("/urls", UrlController::index);
        app.get("/urls/{id}", UrlController::show);
        app.post("/urls/{id}/checks", UrlCheckController::create);

        return app;
    }

    private static Map<String, Object> modelToMap(Object model) {
        Map<String, Object> result = new HashMap<>();

        if (model instanceof Map<?, ?> rawMap) {
            rawMap.forEach((key, value) -> {
                if (key instanceof String) {
                    result.put((String) key, value);
                }
            });
        }

        return result;
    }

    public static void main(String[] args) {
        var app = getApp();

        app.start(8000);
        LOGGER.info("Application started on port 8000");
    }
}
