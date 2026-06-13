package hexlet.code;

import gg.jte.resolve.DirectoryCodeResolver;
import hexlet.code.util.DataSourceFactory;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.output.StringOutput;

import java.nio.file.Path;
import java.sql.SQLException;

import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;

import java.net.URI;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    // FIX: template engine is created once and reused, not recreated per request
    private static TemplateEngine createTemplateEngine() {
        var codeResolver = new DirectoryCodeResolver(
                Path.of("src/main/resources/templates")
        );
        return TemplateEngine.create(codeResolver, ContentType.Html);
    }

    public static Javalin getApp() {

        var dataSource = DataSourceFactory.getDataSource();

        try (var conn = dataSource.getConnection();
             var stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS urls (
                    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    name VARCHAR(255) UNIQUE NOT NULL,
                    created_at TIMESTAMP NOT NULL
                )
            """);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // FIX: create the template engine once here, not inside the lambda
        var templateEngine = createTemplateEngine();

        var app = Javalin.create(config -> {

            config.fileRenderer((filePath, model, context) -> {

                var output = new StringOutput();

                Map<String, Object> map = modelToMap(model);
                templateEngine.render(filePath, map, output);

                return output.toString();
            });
        });

        app.get("/", ctx -> {
            var model = new HashMap<String, Object>();
            // FIX: cast to String so JTE type-matches @param String flash correctly
            model.put("flash", java.util.Objects.toString(ctx.consumeSessionAttribute("flash"), ""));
            ctx.render("index.jte", model);
        });

        app.post("/urls", ctx -> {

            String rawUrl = ctx.formParam("url");

            try {

                String normalized = normalizeUrl(rawUrl);

                var existing = UrlRepository.findByName(normalized);

                if (existing.isPresent()) {

                    ctx.sessionAttribute(
                            "flash",
                            "Страница уже существует"
                    );

                    ctx.redirect("/urls/" + existing.get().getId());

                    return;
                }

                Url url = new Url();

                url.setName(normalized);
                url.setCreatedAt(
                        new Timestamp(System.currentTimeMillis())
                );

                UrlRepository.save(url);

                ctx.sessionAttribute(
                        "flash",
                        "Страница успешно добавлена"
                );

                ctx.redirect("/urls/" + url.getId());

            } catch (Exception e) {

                var model = new HashMap<String, Object>();

                model.put(
                       "flash",
                        "Некорректный URL"
                );

                ctx.status(422);

                ctx.render("index.jte", model);
            }
        });

        app.get("/urls", ctx -> {

            var urls = UrlRepository.getEntities();

            var model = new HashMap<String, Object>();

            model.put("urls", urls);

            model.put("flash", java.util.Objects.toString(ctx.consumeSessionAttribute("flash"), ""));

            ctx.render("urls/index.jte", model);
        });

        app.get("/urls/{id}", ctx -> {

            Long id = Long.valueOf(
                    ctx.pathParam("id")
            );

            var url = UrlRepository.find(id)
                    .orElseThrow();

            var model = new HashMap<String, Object>();

            model.put("url", url);

            model.put("flash", java.util.Objects.toString(ctx.consumeSessionAttribute("flash"), ""));

            ctx.render("urls/show.jte", model);
        });

        return app;
    }

    static String normalizeUrl(String rawUrl) {
        try {
            String url = rawUrl.trim();

            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }

            URI uri = new URI(url);

            String protocol = uri.getScheme();
            String host = uri.getHost();
            int port = uri.getPort();

            if (port == -1 ||
                    (port == 80 && "http".equals(protocol)) ||
                    (port == 443 && "https".equals(protocol))) {
                return protocol + "://" + host;
            }

            return protocol + "://" + host + ":" + port;
        } catch (Exception e) {
            throw new RuntimeException("Invalid URL: " + rawUrl, e);
        }
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

        app.start(7070);
        LOGGER.info("Application started on port 7070");
    }
}
