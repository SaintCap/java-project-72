package hexlet.code;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static Javalin getApp() {
        var app = Javalin.create(config -> {
            config.plugins.enableDevLogging(); // логирование запросов
        });

        // Корневой маршрут
        app.get("/", ctx -> ctx.result("Hello World"));

        return app;
    }

    public static void main(String[] args) {
        var app = getApp();

        app.start(7070); // порт можно любой
        LOGGER.info("Application started on port 7070");
    }
}
