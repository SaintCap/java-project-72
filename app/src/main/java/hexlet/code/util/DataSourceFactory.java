package hexlet.code.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DataSourceFactory {

    private static final HikariDataSource DATA_SOURCE;

    static {

        var config = new HikariConfig();

        String databaseUrl = System.getenv("JDBC_DATABASE_URL");

        if (databaseUrl == null || databaseUrl.isBlank()) {

            config.setJdbcUrl(
                    "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1"
            );

            config.setUsername("sa");
            config.setPassword("");

        } else {
            config.setJdbcUrl(databaseUrl);
        }

        DATA_SOURCE = new HikariDataSource(config);
    }

    public static DataSource getDataSource() {
        return DATA_SOURCE;
    }
}
