package hexlet.code.repository;

import hexlet.code.util.DataSourceFactory;

import javax.sql.DataSource;

public abstract class BaseRepository {

    protected static final DataSource DATA_SOURCE =
            DataSourceFactory.getDataSource();
}