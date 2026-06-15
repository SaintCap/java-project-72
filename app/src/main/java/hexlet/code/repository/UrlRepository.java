package hexlet.code.repository;

import hexlet.code.model.Url;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UrlRepository extends BaseRepository {

    private static Url buildUrl(ResultSet rs) throws SQLException {

        Url url = new Url();

        url.setId(rs.getLong("id"));
        url.setName(rs.getString("name"));
        url.setCreatedAt(rs.getTimestamp("created_at"));

        return url;
    }

    public static Optional<Url> find(Long id) throws SQLException {

        String sql = """
                SELECT *
                FROM urls
                WHERE id = ?
                """;

        // FIX: set parameters before opening the try-with-resources block,
        // then include ResultSet in the resource list so it is closed automatically
        try (
                Connection conn = DATA_SOURCE.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setLong(1, id);

            // FIX: ResultSet now inside try-with-resources
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(buildUrl(rs));
                }
                return Optional.empty();
            }
        }
    }

    public static Optional<Url> findByName(String name)
            throws SQLException {

        String sql = """
                SELECT *
                FROM urls
                WHERE name = ?
                """;

        try (
                Connection conn = DATA_SOURCE.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, name);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(buildUrl(rs));
                }
                return Optional.empty();
            }
        }
    }

    public static List<Url> getEntities()
            throws SQLException {

        String sql = """
                SELECT *
                FROM urls
                ORDER BY created_at DESC
                """;

        try (
                Connection conn = DATA_SOURCE.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()
        ) {
            List<Url> urls = new ArrayList<>();

            while (rs.next()) {
                urls.add(buildUrl(rs));
            }

            return urls;
        }
    }

    public static void save(Url url)
            throws SQLException {

        String sql = """
                INSERT INTO urls(name, created_at)
                VALUES (?, ?)
                """;

        try (
                Connection conn = DATA_SOURCE.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )
        ) {
            stmt.setString(1, url.getName());

            stmt.setTimestamp(
                    2,
                    url.getCreatedAt()
            );

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    url.setId(
                            generatedKeys.getLong(1)
                    );
                }
            }
        }
    }

}
