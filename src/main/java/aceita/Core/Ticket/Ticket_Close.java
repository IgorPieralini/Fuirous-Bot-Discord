package aceita.Core.Ticket;

import aceita.MySQL;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Ticket_Close extends ListenerAdapter {

    private static List<String> finalizando = new ArrayList<>();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("ticket-close")) {
            if (finalizando.contains(event.getChannelId())) {
                return;
            }
            finalizando.add(event.getChannelId());
            event.deferReply().setEphemeral(true).queue();

            String checkSQL = "SELECT * FROM tickets WHERE channel = '" + event.getChannelId() + "'";
            Connection conn = MySQL.getConnection();
            try {
                PreparedStatement stmt = conn.prepareStatement(checkSQL);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) {
                    event.getHook().editOriginal("❌・Esse canal não é um ticket.").queue();
                    return;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            String sql = "DELETE FROM tickets WHERE channel = '" + event.getChannelId() + "'";
            try {
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            event.getChannel().delete().queue();
        }
        finalizando.remove(event.getChannelId());
    }

}
