package hexlet.code.controller;

import hexlet.code.model.Url;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.Utils;
import io.javalin.http.Context;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Objects;

public class UrlController {

    public static void root(Context ctx) {
        var model = new HashMap<String, Object>();
        model.put("flash", Objects.toString(ctx.consumeSessionAttribute("flash"), ""));
        ctx.render("index.jte", model);
    }

    public static void create(Context ctx) {
        String rawUrl = ctx.formParam("url");

        try {
            String normalized = Utils.normalizeUrl(rawUrl);
            var existing = UrlRepository.findByName(normalized);

            if (existing.isPresent()) {
                ctx.sessionAttribute("flash", "Страница уже существует");
                ctx.redirect("/urls/" + existing.get().getId());
                return;
            }

            Url url = new Url();
            url.setName(normalized);
            url.setCreatedAt(new Timestamp(System.currentTimeMillis()));

            UrlRepository.save(url);

            ctx.sessionAttribute("flash", "Страница успешно добавлена");
            ctx.redirect("/urls/" + url.getId());

        } catch (Exception e) {
            var model = new HashMap<String, Object>();
            model.put("flash", "Некорректный URL");
            ctx.status(422);
            ctx.render("index.jte", model);
        }
    }

    public static void index(Context ctx) throws SQLException {
        var urls = UrlRepository.getEntities();
        var latestChecks = UrlCheckRepository.findLatestChecksByUrlId();
        var model = new HashMap<String, Object>();
        model.put("urls", urls);
        model.put("latestChecks", latestChecks);
        model.put("flash", Objects.toString(ctx.consumeSessionAttribute("flash"), ""));
        ctx.render("urls/index.jte", model);
    }

    public static void show(Context ctx) throws SQLException {
        Long id = Long.valueOf(ctx.pathParam("id"));

        var url = UrlRepository.find(id).orElseThrow();
        var checks = UrlCheckRepository.findByUrlId(id);
        var model = new HashMap<String, Object>();

        model.put("checks", checks);
        model.put("url", url);
        model.put("flash", Objects.toString(ctx.consumeSessionAttribute("flash"), ""));

        ctx.render("urls/show.jte", model);
    }
}
