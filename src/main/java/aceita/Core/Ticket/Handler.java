package aceita.Core.Ticket;

import aceita.Core.Ticket.Categories.*;
import aceita.MySQL;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Handler extends ListenerAdapter {

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getComponent().getId().equals("ticket-close")) return;
        event.getChannel().delete().queue();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMember() == null) return; 
        if (getChannelID(event.getMember().getUser()) == null) return;
        if (!event.getChannel().getId().equals(getChannelID(event.getMember().getUser()))) return;

        String sql = "SELECT * FROM tickets WHERE channel = '" + event.getChannel().getId() + "'";
        Connection conn = MySQL.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (getCategory(event.getMember().getUser()) == null) return;
        String category = getCategory(event.getMember().getUser());
        try {
            HandlerCheck(false, category, event.getMember().getUser(), event.getChannel().asTextChannel(), event.getMessage().getContentRaw());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void HandlerCheck(boolean start, String category, User user, TextChannel channel, String message) throws SQLException {
        if (category.equals("Dúvidas/Problemas")) {
            if (start) {
                Duvidas.Start(channel, user);
            } else {
                Duvidas.HandleResponse(channel, user, message);
            }
        } else if (category.equals("ConfirmarFacção")) {
            if (start) {
                Confirmar_Facção.Start(channel, user);
            } else {
                Confirmar_Facção.HandleResponse(channel, user, message);
            }
        } else if (category.equals("Parcerias")) {
            if (start) {
                Parcerias.Start(channel, user);
            } else {
                Parcerias.HandleResponse(channel, user, message);
            }
        } else if (category.equals("Revisões")) {
            if (start) {
                Revisões.Start(channel, user);
            } else {
                Revisões.HandleResponse(channel, user, message);
            }
        } else if (category.equals("RelatarBugs")) {
            if (start) {
                Relatar_Bug.Start(channel, user);
            } else {
                Relatar_Bug.HandleResponse(channel, user, message);
            }
        } else if (category.equals("Reportar")) {
            if (start) {
                Reportar.Start(channel, user);
            } else {
                Reportar.HandleResponse(channel, user, message);
            }
        } else if (category.equals("Outros")) {
            if (start) {
                Outros.Start(channel, user);
            } else {
                Outros.HandleResponse(channel, user, message);
            }
        }
    }

    public static String getCategory(User user) {
        String sql = "SELECT type FROM tickets WHERE user = '" + user.getId() + "'";
        Connection conn = MySQL.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;
            return rs.getString("type");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getChannelID(User user) {
        String sql = "SELECT channel FROM tickets WHERE user = '" + user.getId() + "'";
        Connection conn = MySQL.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;
            return rs.getString("channel");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
