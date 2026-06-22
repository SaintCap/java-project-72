package hexlet.code.repository;

import hexlet.code.model.UrlCheck;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UrlCheckRepository extends BaseRepository {

    private static UrlCheck buildUrlCheck(ResultSet rs)
            throws SQLException {

        UrlCheck check = new UrlCheck();

        check.setId(rs.getLong("id"));
        check.setStatusCode(rs.getInt("status_code"));
        check.setTitle(rs.getString("title"));
        check.setH1(rs.getString("h1"));
        check.setDescription(rs.getString("description"));
        check.setUrlId(rs.getLong("url_id"));
        check.setCreatedAt(rs.getTimestamp("created_at"));

        return check;
    }

    public static Optional<UrlCheck> find(Long id)
            throws SQLException {

        String sql = """
                SELECT *
                FROM url_checks
                WHERE id = ?
                """;

        try (
                Connection conn = DATA_SOURCE.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(buildUrlCheck(rs));
                }

                return Optional.empty();
            }
        }
    }

    public static List<UrlCheck> findByUrlId(Long urlId)
            throws SQLException {

        String sql = """
                SELECT *
                FROM url_checks
                WHERE url_id = ?
                ORDER BY created_at DESC
                """;

        try (
                Connection conn = DATA_SOURCE.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setLong(1, urlId);

            try (ResultSet rs = stmt.executeQuery()) {

                List<UrlCheck> checks = new ArrayList<>();

                while (rs.next()) {
                    checks.add(buildUrlCheck(rs));
                }

                return checks;
            }
        }
    }

    public static Map<Long, UrlCheck> findLatestChecksByUrlId()
            throws SQLException {

        String sql = """
                SELECT *
                FROM url_checks
                ORDER BY created_at ASC
                """;

        try (
                Connection conn = DATA_SOURCE.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()
        ) {
            Map<Long, UrlCheck> urlIdToLatestCheck = new HashMap<>();

            while (rs.next()) {
                UrlCheck check = buildUrlCheck(rs);
                urlIdToLatestCheck.put(check.getUrlId(), check);
            }

            return urlIdToLatestCheck;
        }
    }

    public static void save(UrlCheck check)
            throws SQLException {

        String sql = """
                INSERT INTO url_checks(
                    status_code,
                    title,
                    h1,
                    description,
                    url_id,
                    created_at
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection conn = DATA_SOURCE.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )
        ) {
            stmt.setInt(1, check.getStatusCode());
            stmt.setString(2, check.getTitle());
            stmt.setString(3, check.getH1());
            stmt.setString(4, check.getDescription());
            stmt.setLong(5, check.getUrlId());
            stmt.setTimestamp(6, check.getCreatedAt());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    check.setId(
                            generatedKeys.getLong(1)
                    );
                }
            }
        }
    }
}
