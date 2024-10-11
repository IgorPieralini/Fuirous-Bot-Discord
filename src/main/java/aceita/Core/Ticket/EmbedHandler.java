package aceita.Core.Ticket;

import aceita.Core.Ticket.Categories.*;
import aceita.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.util.Map;


public class EmbedHandler {

    public static void send(User user, Map<Integer, String> responses) {
        String category = Handler.getCategory(user);
        String ticketID = Handler.getChannelID(user);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(user.getEffectiveName(), null, user.getEffectiveAvatarUrl());
        embed.setColor(Color.ORANGE);
        embed.setTitle("✨・Novo Questionário!");
        embed.addField("📮・Categoria", category, true);
        embed.addField("👤・Usuário", user.getAsMention(), true);
        embed.addField("🏷️・Ticket", "<#" + ticketID + ">", true);

        String[] Questions = getQuestions(category);
        int number = 1;
        for (Map.Entry<Integer, String> entry : responses.entrySet()) {
            String question = number + ". " + Questions[entry.getKey()];
            embed.addField(question, entry.getValue(), false);
            number++;
        }

        TextChannel channel = Main.Variables.Ticket_Channel_Logs;

        channel.sendMessageEmbeds(embed.build()).queue();
    }

    private static String[] getQuestions(String category) {
        String[] Questions = {};
        if (category.equals("Dúvidas/Problemas")) {
            Questions = Duvidas.Questions;
        } else if (category.equals("ConfirmarFacção")) {
            Questions = Confirmar_Facção.Questions;
        } else if (category.equals("Parcerias")) {
            Questions = Parcerias.Questions;
        } else if (category.equals("Revisões")) {
            Questions = Revisões.Questions;
        } else if (category.equals("RelatarBugs")) {
            Questions = Relatar_Bug.Questions;
        } else if (category.equals("Reportar")) {
            Questions = Reportar.Questions;
        } else if (category.equals("Outros")) {
            Questions = Outros.Questions;
        }
        return Questions;
    }

}
