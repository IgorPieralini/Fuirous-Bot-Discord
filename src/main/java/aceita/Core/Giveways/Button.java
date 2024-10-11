package aceita.Core.Giveways;

import aceita.MySQL;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Button extends ListenerAdapter {

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().contains("giveways_leave")) {
            event.deferReply().setEphemeral(true).queue();
            String[] data = event.getComponentId().split("-");
            String code = data[1];

            boolean leaved = false;
            try {
                String sql = "SELECT * FROM giveways_members WHERE code = '" + code + "' AND user = '" + event.getUser().getId() + "'";
                Connection conn = MySQL.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String sql2 = "DELETE FROM giveways_members WHERE code = '" + code + "' AND user = '" + event.getUser().getId() + "'";
                    PreparedStatement stmt2 = conn.prepareStatement(sql2);
                    stmt2.executeUpdate();
                    leaved = true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (leaved) {
                String message = "...";
                try {
                    String sql = "SELECT * FROM giveways WHERE code = '" + code + "'";
                    Connection conn = MySQL.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        message = rs.getString("message");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                Giveways giveway = Giveways.getGiveway(code);
                TextChannel channel = giveway.getChannel();
                if (channel == null) return;

                RestAction<Message> action = channel.retrieveMessageById(message);
                action.queue(
                        msg -> {
                            msg.editMessageEmbeds(msg.getEmbeds().get(0)).setActionRow(
                                    net.dv8tion.jda.api.interactions.components.buttons.Button.success("giveways-" + code, "Participar (" + getMembers(code) + ")")
                            ).queue();
                        }
                );
                event.getHook().editOriginal("🎁・Você saiu do sorteio.").queue();
            } else {
                event.getHook().editOriginal("🎁・Você não está participando do sorteio.").queue();
            }
            return;
        }
        if (event.getComponentId().contains("giveways")) {
            event.deferReply().setEphemeral(true).queue();
            String[] data = event.getComponentId().split("-");
            String code = data[1];

            boolean exist = false;
            try {
                String sql = "SELECT * FROM giveways WHERE code = '" + code + "'";
                Connection conn = MySQL.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) exist = true;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (!exist) {
                event.getHook().editOriginal("🎁・Sorteio finalizado ou não encontrado.").queue();
                return;
            }

            boolean participando = isParticipando(event, code);

            if (participando) {
                event.getHook().editOriginal("🎁・Você já está participando deste sorteio.").setActionRow(
                        net.dv8tion.jda.api.interactions.components.buttons.Button.danger("giveways_leave-" + code, "Clique aqui para sair")
                ).queue();
                return;
            }

            try {
                String sql = "INSERT INTO giveways_members (user, code) VALUES (?,?)";
                Connection conn = MySQL.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, event.getUser().getId());
                stmt.setString(2, code);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            event.getMessage().editMessageEmbeds(event.getMessage().getEmbeds().get(0)).setActionRow(
                    net.dv8tion.jda.api.interactions.components.buttons.Button.success("giveways-" + code, "Participar (" + getMembers(code) + ")")
            ).queue();

            event.getHook().editOriginal("🎁・Você se juntou ao sorteio.").setActionRow(
                    net.dv8tion.jda.api.interactions.components.buttons.Button.danger("giveways_leave-" + code, "Clique aqui para sair")
            ).queue();
        }
    }

    private static boolean isParticipando(ButtonInteractionEvent event, String code) {
        boolean participando = false;
        try {
            String sql = "SELECT * FROM giveways_members WHERE user = '" + event.getUser().getId() + "' AND code = '" + code + "'";
            Connection conn = MySQL.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) participando = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return participando;
    }
    public static int getMembers(String code) {
        int amount = 0;
        try {
            String sql = "SELECT * FROM giveways_members WHERE code = '" + code + "'";
            Connection conn = MySQL.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) amount++;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return amount;
    }
}
