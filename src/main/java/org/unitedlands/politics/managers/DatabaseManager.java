package org.unitedlands.politics.managers;

import java.sql.SQLException;

import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.models.ReputationScoreEntry;
import org.unitedlands.politics.models.SchemaVersion;
import org.unitedlands.politics.services.ReputationScoreEntryService;
import org.unitedlands.utils.Logger;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseManager {

    private final UnitedPolitics plugin;

    private HikariDataSource hikariDataSource;
    private ConnectionSource connectionSource;

    private ReputationScoreEntryService reputationScoreEntryService;
    // private WarEventRecordDbService warEventRecordDbService;
    // private WarScoreRecordDbService warScoreRecordDbService;
    // private SiegeChunkDbService siegeChunkDbService;

    public DatabaseManager(UnitedPolitics plugin) {
        this.plugin = plugin;
    }

    public void initialize() {

        var fileConfig = plugin.getConfig();

        String host = fileConfig.getString("mysql.host");
        int port = fileConfig.getInt("mysql.port");
        String database = fileConfig.getString("mysql.database");
        String username = fileConfig.getString("mysql.username");
        String password = fileConfig.getString("mysql.password");

        String jdbcUrl = String.format(
                "jdbc:mysql://%s:%d/%s?useSSL=%s&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                host,
                port,
                database,
                plugin.getConfig().getBoolean("developer-mode") ? "false" : "true");

        try {

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);

            // Connection pool settings
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(600000); // 10 minutes
            config.setMaxLifetime(1800000); // 30 minutes
            config.setConnectionTimeout(5000); // 5 seconds
            config.setValidationTimeout(3000); // 3 seconds

            // Validation query
            config.setConnectionTestQuery("SELECT 1");
            config.setLeakDetectionThreshold(15000); // Warn if connection held 15+ seconds

            hikariDataSource = new HikariDataSource(config);
            connectionSource = new DataSourceConnectionSource(hikariDataSource, jdbcUrl);

            Logger.log("Connected to MySQL database with HikariCP.", "UnitedPolitics");

            verifySchemaVersion();
            registerServices();

            Logger.log("DatabaseManager initialized successfully.", "UnitedPolitics");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void registerServices() throws SQLException {
        this.reputationScoreEntryService = new ReputationScoreEntryService(getDao(ReputationScoreEntry.class));
        // this.warEventRecordDbService = new WarEventRecordDbService(getDao(WarEventRecord.class));
        // this.warScoreRecordDbService = new WarScoreRecordDbService(getDao(WarScoreRecord.class));
        // this.siegeChunkDbService = new SiegeChunkDbService(getDao(SiegeChunk.class));
    }

    private void verifySchemaVersion() throws SQLException {
        Dao<SchemaVersion, Integer> versionDao = getDao(SchemaVersion.class);
        SchemaVersion version = versionDao.queryForId(1);

        if (version == null) {
            version = new SchemaVersion(1);
            versionDao.create(version);
        }

        applyMigrations(versionDao, version);
    }

    private void applyMigrations(Dao<SchemaVersion, Integer> versionDao, SchemaVersion version) throws SQLException {
        // Example for future migrations on production server

        // if (version.getVersion() < 2) {
        // // Migration 1 → 2: Add new field to `PlayerData`

        // versionDao.executeRaw("ALTER TABLE test_data ADD COLUMN new_field
        // VARCHAR(255) DEFAULT NULL;");

        // version.setVersion(2);
        // versionDao.update(version);
        // }
    }

    public <T, ID> Dao<T, ID> getDao(Class<T> clazz) throws SQLException {

        // In developer mode, drop the table if it exists
        if (plugin.getConfig().getBoolean("developer-mode"))
            TableUtils.dropTable(connectionSource, clazz, true);

        TableUtils.createTableIfNotExists(connectionSource, clazz);
        return DaoManager.createDao(connectionSource, clazz);
    }

    public void close() {
        try {
            if (connectionSource != null) {
                connectionSource.close();
                Logger.log("Disconnected from MySQL database.", "UnitedPolitics");
            }
            if (hikariDataSource != null) {
                hikariDataSource.close();
                Logger.log("HikariCP connection closed.", "UnitedPolitics");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ReputationScoreEntryService getReputationScoreEntryService() {
        return reputationScoreEntryService;
    }

    // public WarEventRecordDbService getWarEventRecordDbService() {
    //     return warEventRecordDbService;
    // }

    // public WarScoreRecordDbService getWarScoreRecordDbService() {
    //     return warScoreRecordDbService;
    // }

    // public SiegeChunkDbService getSiegeChunkDbService() {
    //     return siegeChunkDbService;
    // }

    public ConnectionSource getConnectionSource() {
        return connectionSource;
    }

}
