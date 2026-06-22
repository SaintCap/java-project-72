package hexlet.code.controller;

import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import io.javalin.http.Context;

import kong.unirest.Unirest;

import org.jsoup.Jsoup;

import java.sql.SQLException;
import java.sql.Timestamp;

public class UrlCheckController {

    public static void create(Context ctx) throws SQLException {
        Long urlId = Long.valueOf(ctx.pathParam("id"));
        var url = UrlRepository.find(urlId).orElseThrow();

        try {
            var response = Unirest.get(url.getName()).asString();
            var document = Jsoup.parse(response.getBody());

            var statusOfResponse = response.getStatus();
            if (statusOfResponse >= 400 && statusOfResponse <= 600) {
                ctx.sessionAttribute("flash", "Произошла ошибка при проверке");
                ctx.redirect("/urls/" + urlId);
                return;
            }

            var check = new UrlCheck();
            check.setUrlId(urlId);
            check.setStatusCode(statusOfResponse);
            check.setTitle(document.title());

            var h1 = document.selectFirst("h1");
            check.setH1(h1 != null ? h1.text() : null);

            var description = document.selectFirst("meta[name=description]");
            check.setDescription(description != null ? description.attr("content") : null);

            check.setCreatedAt(new Timestamp(System.currentTimeMillis()));

            UrlCheckRepository.save(check);

            ctx.sessionAttribute("flash", "Страница успешно проверена");

        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Произошла ошибка при проверке");
        }

        ctx.redirect("/urls/" + urlId);
    }
}
