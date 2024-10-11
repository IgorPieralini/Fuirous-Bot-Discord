package aceita.Core.Giveways;

import aceita.Main;
import aceita.MySQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static aceita.Core.Giveways.Util.*;

public class Giveways {

    private String name;
    private String description;
    private int winners;
    private String award;
    private long time;
    private String code;
    private TextChannel channel;

    public static Map<String, Giveways> giveways = new HashMap<>();
    public Giveways(String name, String description, int winners, String award, long time, String code, TextChannel channel) {
        this.name = name;
        this.description = description;
        this.winners = winners;
        this.award = award;
        this.time = time;
        this.code = code;
        this.channel = channel;
    }

    public String getName() {return name;}
    public String getCode() {return code;}
    public long getTime() {return time;}
    public String getDescription() {return description;}
    public int getWinners() {return winners;}
    public String getAward() {return award;}
    public TextChannel getChannel() {return channel;}
    public void removeTime() {
        this.time = this.time - 1;
        try {
            String sql = "SELECT * FROM giveways WHERE code = '" + code + "'";
            PreparedStatement statement = MySQL.getConnection().prepareStatement(sql);
            ResultSet result = statement.executeQuery();
            if (!result.next()) return;
            int time = result.getInt("time");
            time = time - 1;
            String sql2 = "UPDATE giveways SET time = '" + time + "' WHERE code = '" + code + "'";
            PreparedStatement statement2 = MySQL.getConnection().prepareStatement(sql2);
            statement2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static Giveways getGiveway(String code) {return giveways.get(code);}

    public static void createGiveway(TextChannel channel, String name, String description, int winners, String award, String time) {
        String code = createCode(10);

        Instant futureInstant = addTimeToCurrent(time);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🎁・Novo Sorteio");
        embed.setColor(Color.ORANGE);
        embed.setDescription(description + "\n\n" +
                "🏷️・Clique no botão abaixo para participar.");
        embed.setTimestamp(futureInstant);

        channel.sendMessageEmbeds(embed.build()).setActionRow(
                Button.success("giveways-" + code, "Participar (0)")
        ).queue(message -> {
            String sql = "INSERT INTO giveways (code, name, description, winners, award, message, channel, time) VALUES (?,?,?,?,?,?,?,?)";
            long timeLong = convertToSeconds(time);
            try {
                Connection connection = MySQL.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, code);
                statement.setString(2, name);
                statement.setString(3, description);
                statement.setInt(4, winners);
                statement.setString(5, award);
                statement.setString(6, message.getId());
                statement.setString(7, channel.getId());
                statement.setLong(8, timeLong);
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            Giveways giveway = new Giveways(name, description, winners, award, timeLong, code, channel);
            giveways.put(code, giveway);
        });
    }
    public static void loadAll() {
        try {
            String sql = "SELECT * FROM giveways";
            Connection connection = MySQL.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.execute();
            ResultSet result = statement.getResultSet();
            while (result.next()) {
                String code = result.getString("code");
                String name = result.getString("name");
                String description = result.getString("description");
                String winnersString = result.getString("winners");
                String award = result.getString("award");
                String timeString = result.getString("time");
                String channelID = result.getString("channel");
                TextChannel channel = Main.Bot.getTextChannelById(channelID);
                int winners = Integer.parseInt(winnersString);
                int time = Integer.parseInt(timeString);
                Giveways giveway = new Giveways(name, description, winners, award, time, code, channel);
                giveways.put(code, giveway);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}