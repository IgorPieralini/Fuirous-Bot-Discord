package aceita.Core.Ticket.Categories;

import aceita.Core.Ticket.EmbedHandler;
import aceita.Core.Ticket.Handler;
import aceita.MySQL;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Outros {

    private static Map<User, Integer> User_Steps = new HashMap<>();
    private static Map<User, Map<Integer, String>> User_Responses = new HashMap<>();

    public static final String[] Questions = {
            "Descreva a situação."
    };

    public static void Start(TextChannel channel, User user) {
        if (!User_Steps.containsKey(user)) return;
        User_Steps.put(user, 0);
        User_Responses.put(user, new HashMap<>());
        NextQuestion(channel, user);
    }

    public static void HandleResponse(TextChannel channel, User user, String response) throws SQLException {
        int step = User_Steps.get(user);
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

    private static void Finish(TextChannel channel, User user) {
        channel.sendMessage("✅・Aguarde! Um membro da Equipe Stay irá vir te atender. " + user.getAsMention()).queue();
        EmbedHandler.send(user, User_Responses.get(user));
        User_Responses.remove(user);
    }

}
