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

        var app = Javalin.create(config -> {

            config.fileRenderer((filePath, model, context) -> {

                var output = new StringOutput();

                createTemplateEngine().render(filePath, model, output);

                return output.toString();
            });
        });

        app.get("/", ctx -> {
            ctx.render("index.jte");
        });

        return app;
    }

    public static void main(String[] args) {
        var app = getApp();

        app.start(7070);
        LOGGER.info("Application started on port 7070");
    }
}
