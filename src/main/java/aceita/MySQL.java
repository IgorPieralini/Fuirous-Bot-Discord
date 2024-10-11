package aceita;

import aceita.Core.Giveways.Giveways;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQL {

    private static Connection conn;

    public static synchronized Connection getConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                return conn;
            }
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:database.db";
            File file = new File("database.db");
            if (!file.exists()) {
                file.createNewFile();
            }
            conn = DriverManager.getConnection(url);
        } catch (SQLException | IOException | ClassNotFoundException e) {
            System.out.println("Ocorreu um erro ao obter conexão com a database.");
            e.printStackTrace();
            System.exit(-1);
        }
        return conn;
    }

    public static void load() throws SQLException {
        Connection conn = getConnection();
        String ticketsCreate = "CREATE TABLE IF NOT EXISTS tickets (" +
                "user VARCHAR(255) PRIMARY KEY, " +
                "channel VARCHAR(255), " +
                "type VARCHAR(255), " +
                "other VARCHAR(255) DEFAULT NULL, " +
                "start BOOLEAN DEFAULT FALSE " +
                ")";
        Statement ticketsSTMT = conn.createStatement();
        ticketsSTMT.executeUpdate(ticketsCreate);

        String factionCreate = "CREATE TABLE IF NOT EXISTS factions (" +
                "user VARCHAR(255) PRIMARY KEY, " +
                "ticket VARCHAR(255), " +
                "name VARCHAR(255), " +
                "tag VARCHAR(255), " +
                "members VARCHAR(255), " +
                "discord_link VARCHAR(255)," +
                "role_id VARCHAR(255) DEFAULT NULL " +
                ")";
        Statement factionSTMT = conn.createStatement();
        factionSTMT.executeUpdate(factionCreate);

        String givewaysCreate = "CREATE TABLE IF NOT EXISTS giveways (" +
                "code VARCHAR(255) PRIMARY KEY, " +
                "name VARCHAR(255), " +
                "description VARCHAR(500), " +
                "winners VARCHAR(255), " +
                "award VARCHAR(255), " +
                "message VARCHAR(255)," +
                "channel VARCHAR(255), " +
                "time VARCHAR(255) " +
                ")";
        Statement givewaysSTMT = conn.createStatement();
        givewaysSTMT.executeUpdate(givewaysCreate);

        String givewaysParticipantesCreate = "CREATE TABLE IF NOT EXISTS giveways_members (" +
                "user VARCHAR(255) PRIMARY KEY, " +
                "code VARCHAR(255) " +
                ")";
        Statement givewaysParticipantesSTMT = conn.createStatement();
        givewaysParticipantesSTMT.executeUpdate(givewaysParticipantesCreate);
    }

}
