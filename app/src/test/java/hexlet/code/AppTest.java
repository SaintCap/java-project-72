package hexlet.code;

import hexlet.code.repository.UrlRepository;
import io.javalin.testtools.JavalinTest;
import okhttp3.FormBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import okhttp3.Request;

import java.sql.Connection;
import java.sql.DriverManager;

import static hexlet.code.App.normalizeUrl;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    @BeforeEach
    void clearDb() throws Exception {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1", "sa", "")) {
            conn.createStatement().execute("DROP ALL OBJECTS");
        }
        App.getApp();
    }

    @Test
    void testRootPage() {
        JavalinTest.test(App.getApp(), (server, client) -> {
            var response = client.get("/");
            assertEquals(200, response.code());
        });
    }

    @Test
    void testCreateUrl() throws Exception {

        JavalinTest.test(App.getApp(), (server, client) -> {

            var body = new FormBody.Builder()
                    .add("url", "https://cybershoke.net/ru/cs2")
                    .build();

            var request = new Request.Builder()
                    .url("http://localhost:" + server.port() + "/urls")
                    .post(body)
                    .build();

            var response = client.request(request);

            assertEquals(200, response.code());

            var created = UrlRepository.findByName("https://cybershoke.net");

            assertTrue(created.isPresent());
        });
    }

    @Test
    void testDuplicateUrlRedirectsToExistingPage() throws Exception {
        JavalinTest.test(App.getApp(), (server, client) -> {
            var body = new FormBody.Builder()
                    .add("url", "https://cybershoke.net/ru/cs2")
                    .build();

            var request = new Request.Builder()
                    .url("http://localhost:" + server.port() + "/urls")
                    .post(body)
                    .build();

            var response = client.request(request);

            var existing = UrlRepository.findByName("https://cybershoke.net");

            assertTrue(existing.isPresent());

            assertEquals(200, response.code());

            var page = client.get("/urls/" + existing.get().getId());

            assertEquals(200, page.code());
        });
    }

    @Test
    void testUrlsIndex() {
        JavalinTest.test(App.getApp(), (server, client) -> {
            var response = client.get("/urls");
            assertEquals(200, response.code());
        });
    }

    @Test
    void testNormalize() throws Exception {
        System.out.println(
                normalizeUrl("https://hexlet.io/courses")
        );
    }
}

