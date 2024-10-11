package aceita.Core.Ticket.Categories;

import aceita.Core.Ticket.EmbedHandler;
import aceita.Core.Ticket.Handler;
import aceita.MySQL;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.internal.utils.config.sharding.PresenceProviderConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Confirmar_Facção {

    private static Map<User, Integer> User_Steps = new HashMap<>();
    private static Map<User, Map<Integer, String>> User_Responses = new HashMap<>();

    public static final String[] Questions = {
            "Qual o nome da facção?",
            "Qual a TAG?",
            "Quantos Membros?",
            "Envie o link do discord."
    };

    public static void Start(TextChannel channel, User user) {
        User_Steps.put(user, 0);
        User_Responses.put(user, new HashMap<>());
        NextQuestion(channel, user);
    }

    public static void HandleResponse(TextChannel channel, User user, String response) throws SQLException {
        if (!User_Steps.containsKey(user)) return;
        int step = User_Steps.get(user);
        if (step == 1) {
            if (response.length() > 3) {
                channel.sendMessage("❌・O nome da facção não pode ter mais de 3 caracteres!").queue();
                return;
            }
        }
        User_Responses.get(user).put(step, response);
        step++;
        if (step < Questions.length) {
            User_Steps.put(user, step);
            NextQuestion(channel, user);
        } else {
            User_Steps.remove(user);
            Finish(channel, user);
        }
    }

    private static void NextQuestion(TextChannel channel, User user) {
        int step = User_Steps.get(user);
        channel.sendMessage(Questions[step]).queue();
    }

    private static void Finish(TextChannel channel, User user) throws SQLException {
        channel.sendMessage("✅・Aguarde! Um membro da Equipe Superior Stay irá vir te atender. " + user.getAsMention()).queue();
        EmbedHandler.send(user, User_Responses.get(user));

        Map<Integer, String> responses = User_Responses.get(user);

        String name = responses.get(0);
        String tag = responses.get(1);
        String members = responses.get(2);
        String discord_link = responses.get(3);

        Connection conn = MySQL.getConnection();

        try {
            String sql = "SELECT * FROM factions WHERE user = '" + user.getId() + "'";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String SQL = "DELETE FROM factions WHERE user = '" + user.getId() + "'";
                PreparedStatement stmt2 = conn.prepareStatement(SQL);
                stmt2.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String sqlUpdate = "INSERT INTO factions (user, ticket, name, tag, members, discord_link) VALUES (" +
                "'" + user.getId() + "', '" + channel.getId() + "', '" + name + "', '" + tag + "', '" + members + "', '" + discord_link + "')";
        PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate);
        stmtUpdate.executeUpdate();

        try {
            String sql = "UPDATE tickets SET other = '" + tag + ":::" + name + "' WHERE user = '" + user.getId() + "'";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        User_Responses.remove(user);
    }

}
