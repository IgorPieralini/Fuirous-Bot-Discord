package aceita.Core.Ticket.Categories;

import aceita.Core.Ticket.EmbedHandler;
import aceita.Core.Ticket.Handler;
import aceita.MySQL;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Parcerias {

    private static Map<User, Integer> User_Steps = new HashMap<>();
    private static Map<User, Map<Integer, String>> User_Responses = new HashMap<>();

    public static final String[] Questions = {
            "Qual é o seu nick? (nome dentro do jogo)",
            "Quantos inscritos você possuí?",
            "Envie o link do seu canal."
    };

    public static void Start(TextChannel channel, User user) {
        if (!User_Steps.containsKey(user)) return;
        User_Steps.put(user, 0);
        User_Responses.put(user, new HashMap<>());
        NextQuestion(channel, user);
    }

    public static void HandleResponse(TextChannel channel, User user, String response) throws SQLException {
        int step = User_Steps.get(user);
        if (step == 1) {
            if (response.matches("[0-9]+")) {
                User_Responses.get(user).put(step, response);
                int parsedInt = Integer.parseInt(response);
                if (parsedInt < 250) {
                    Button button = Button.danger("ticket-close", "Finalizar Ticket");
                    channel.sendMessage("❌・Você não possuí inscritos suficientes.").addActionRow(button).queue();
                    String sql = "DELETE FROM tickets WHERE user = '" + user.getId() + "'";
                    Connection conn = MySQL.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.executeUpdate();
                    return;
                }
                step++;
                User_Steps.put(user, step);
                NextQuestion(channel, user);
            } else {
                channel.sendMessage("❌・Por favor, digite apenas números.").queue();
                return;
            }
        } else {
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
    }

    private static void NextQuestion(TextChannel channel, User user) {
        int step = User_Steps.get(user);
        channel.sendMessage(Questions[step]).queue();
    }

    private static void Finish(TextChannel channel, User user) {
        channel.sendMessage("✅・Aguarde! Um membro da Equipe Superior Stay irá vir te atender. " + user.getAsMention()).queue();
        EmbedHandler.send(user, User_Responses.get(user));
        User_Responses.remove(user);
    }

}
