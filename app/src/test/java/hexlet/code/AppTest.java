package hexlet.code;

import hexlet.code.model.Url;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import io.javalin.testtools.JavalinTest;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Timestamp;

import static hexlet.code.util.Utils.normalizeUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AppTest {

    private static MockWebServer mockServer;

    @BeforeAll
    static void startMockServer() throws Exception {
        mockServer = new MockWebServer();
        mockServer.start();
    }

    @AfterAll
    static void stopMockServer() throws Exception {
        mockServer.shutdown();
    }

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
    void testUrlsIndex() {
        JavalinTest.test(App.getApp(), (server, client) -> {
            var response = client.get("/urls");
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
    void testCreateInvalidUrl() throws Exception {
        JavalinTest.test(App.getApp(), (server, client) -> {
            var body = new FormBody.Builder()
                    .add("url", "not a valid url")
                    .build();

            var request = new Request.Builder()
                    .url("http://localhost:" + server.port() + "/urls")
                    .post(body)
                    .build();

            var response = client.request(request);

            assertEquals(422, response.code());

            assertTrue(UrlRepository.getEntities().isEmpty());
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
    void testShowUrl() throws Exception {
        var url = new Url();
        url.setName("https://example.com");
        url.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        UrlRepository.save(url);

        JavalinTest.test(App.getApp(), (server, client) -> {
            var response = client.get("/urls/" + url.getId());

            assertEquals(200, response.code());
            assertTrue(response.body().string().contains("https://example.com"));
        });
    }

    @Test
    void testUrlCheck() throws Exception {
        String html = """
                <html>
                    <head>
                        <title>Test page title</title>
                        <meta name="description" content="Test page description">
                    </head>
                    <body>
                        <h1>Test H1 header</h1>
                    </body>
                </html>
                """;

        mockServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(html)
        );

        String mockUrl = mockServer.url("/").toString();

        JavalinTest.test(App.getApp(), (server, client) -> {
            var body = new FormBody.Builder()
                    .add("url", mockUrl)
                    .build();

            var createRequest = new Request.Builder()
                    .url("http://localhost:" + server.port() + "/urls")
                    .post(body)
                    .build();

            client.request(createRequest);

            var url = UrlRepository.findByName(normalizeUrl(mockUrl))
                    .orElseThrow();

            var checkResponse = client.post("/urls/" + url.getId() + "/checks");

            assertEquals(200, checkResponse.code());

            var checks = UrlCheckRepository.findByUrlId(url.getId());

            assertEquals(1, checks.size());

            var check = checks.get(0);

            assertNotNull(check);
            assertEquals(200, check.getStatusCode());
            assertEquals("Test page title", check.getTitle());
            assertEquals("Test H1 header", check.getH1());
            assertEquals("Test page description", check.getDescription());
        });
    }

    @Test
    void testNormalize() {
        assertEquals("https://hexlet.io", normalizeUrl("https://hexlet.io/courses"));
        assertEquals("http://example.com:8080", normalizeUrl("http://example.com:8080/path?q=1"));
    }
}
