package hexlet.code;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.output.StringOutput;
import gg.jte.resolve.DirectoryCodeResolver;
import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.DataSourceFactory;
import io.javalin.Javalin;

import kong.unirest.Unirest;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

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

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS url_checks (
                    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    status_code INTEGER NOT NULL,
                    title VARCHAR(255),
                    h1 VARCHAR(255),
                    description TEXT,
                    url_id BIGINT NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    CONSTRAINT fk_url_checks_url
                        FOREIGN KEY (url_id)
                        REFERENCES urls(id)
                        ON DELETE CASCADE
                )
            """);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

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

            var url = UrlRepository.find(id).orElseThrow();
            var checks = UrlCheckRepository.findByUrlId(id);
            var model = new HashMap<String, Object>();

            model.put("checks", checks);
            model.put("url", url);
            model.put("flash", java.util.Objects.toString(ctx.consumeSessionAttribute("flash"), ""));

            ctx.render("urls/show.jte", model);
        });

        app.post("/urls/{id}/checks", ctx -> {

            Long urlId = Long.valueOf(ctx.pathParam("id"));

            var url = UrlRepository.find(urlId)
                    .orElseThrow();

            try {

                var response = Unirest.get(url.getName())
                        .asString();

                var document = Jsoup.parse(response.getBody());

                var check = new UrlCheck();

                check.setUrlId(urlId);
                check.setStatusCode(response.getStatus());
                check.setTitle(document.title());

                var h1 = document.selectFirst("h1");
                check.setH1(h1 != null ? h1.text() : null);

                var description = document.selectFirst("meta[name=description]");
                check.setDescription(
                        description != null
                                ? description.attr("content")
                                : null
                );

                check.setCreatedAt(
                        new Timestamp(System.currentTimeMillis())
                );

                UrlCheckRepository.save(check);

                ctx.sessionAttribute(
                        "flash",
                        "Страница успешно проверена"
                );

            } catch (Exception e) {

                ctx.sessionAttribute(
                        "flash",
                        "Произошла ошибка при проверке"
                );
            }

            ctx.redirect("/urls/" + urlId);
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
