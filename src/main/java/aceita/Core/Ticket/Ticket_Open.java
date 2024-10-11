package aceita.Core.Ticket;

import aceita.ConfigManager;
import aceita.Main;
import aceita.MySQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;

import static aceita.Core.Ticket.Handler.HandlerCheck;

public class Ticket_Open extends ListenerAdapter {

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (event.getComponent().getId().equals("TicketSelectMenu")) {
            String option = event.getSelectedOptions().get(0).getValue();

            boolean exist = false;
            String channel = "...";
            try {
                String sql = "SELECT * FROM tickets WHERE user = '" + event.getUser().getId() + "';";
                Connection conn = MySQL.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    exist = true;
                    channel = rs.getString("channel");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (exist) {
                event.reply("❌・Você já possui um ticket aberto! <#" + channel + ">").setEphemeral(true).queue();
                return;
            }

            StringSelectMenu.Builder selectMenu2 = StringSelectMenu.create("TicketSelectMenu")
                    .setPlaceholder("Selecione uma opção")
                    .addOption("Dúvidas/Problemas", "Dúvidas/Problemas", "Teve alguma dúvida ou problema?", Emoji.fromFormatted("❓"))
                    .addOption("Confirmar Facção", "ConfirmarFacção", "Confirme sua facção", Emoji.fromFormatted("⚔️"))
                    .addOption("Parcerias", "Parcerias", "Uma possível parceria?", Emoji.fromFormatted("🤝"))
                    .addOption("Revisões", "Revisões", "Punição errada? Peça uma revisão.", Emoji.fromFormatted("❌"))
                    .addOption("Relatar Bugs", "RelatarBugs", "Relate algum bug do servidor.", Emoji.fromFormatted("🛠️"))
                    .addOption("Reportar", "Reportar", "Reporte um jogador.", Emoji.fromFormatted("📮"))
                    .addOption("Outros", "Outros", "Fale conosco sobre algo diferente.", Emoji.fromFormatted("🏷️"));
            event.getMessage().editMessageComponents().setActionRow(selectMenu2.build()).queue();
            event.deferReply().setEphemeral(true).queue();

            Category category = getCategory(option);

            Main.Variables.Guild_Global.createTextChannel(event.getUser().getName())
                    .setParent(category)
                    .addPermissionOverride(event.getMember(), EnumSet.of(Permission.VIEW_CHANNEL), null)
                    .addPermissionOverride(Main.Variables.Guild_Global.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                    .addPermissionOverride(Main.Variables.Ticket_StaffRole, EnumSet.of(Permission.VIEW_CHANNEL), null)
                    .queue(createdChannel -> {
                        EmbedBuilder embedBuilder = new EmbedBuilder();
                        embedBuilder.setAuthor(event.getUser().getName(), null, event.getUser().getAvatarUrl());
                        embedBuilder.setDescription("🙋‍♂️・Você abriu o seu ticket na categoria **" + option + "**, responda a algumas perguntas abaixo e um staff entrará em contato com você.");
                        embedBuilder.setColor(Color.ORANGE);
                        embedBuilder.setThumbnail(event.getGuild().getIconUrl());
                        createdChannel.sendMessageEmbeds(embedBuilder.build())
                                .setContent("||" + event.getUser().getAsMention() + "||")
                                .queue();

                        try {
                            String sql = "DELETE FROM tickets WHERE user = '" + event.getUser().getId() + "'";
                            Connection conn = MySQL.getConnection();
                            PreparedStatement preparedStatement = conn.prepareStatement(sql);
                            preparedStatement.executeUpdate();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                        try {
                            String sql2 = "INSERT INTO tickets (user, channel, type) VALUES ('" + event.getUser().getId() + "','" + createdChannel.getId() + "','" + option + "')";
                            Connection conn = MySQL.getConnection();
                            PreparedStatement preparedStatement = conn.prepareStatement(sql2);
                            preparedStatement.executeUpdate();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                        try {
                            HandlerCheck(true, option, event.getMember().getUser(), createdChannel, event.getMessage().getContentRaw());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                        event.getHook().setEphemeral(true).editOriginal("✅・Ticket aberto com sucesso! " + createdChannel.getAsMention()).queue();
                    }, error -> System.out.println("Erro ao criar canal de texto: " + error.getMessage()));
        }
    }


    public static Category getCategory(String option) {
        ConfigManager config = new ConfigManager("config.yml");
        Category category = null;
        switch (option) {
            case "Dúvidas/Problemas":
                category = Main.Variables.Guild_Global.getCategoryById(config.get("Ticket.Categories.Duvidas/Problemas"));
                return category;
            case "ConfirmarFacção":
                category = Main.Variables.Guild_Global.getCategoryById(config.get("Ticket.Categories.ConfirmarFacção"));
                return category;
            case "Parcerias":
                category = Main.Variables.Guild_Global.getCategoryById(config.get("Ticket.Categories.Parcerias"));
                return category;
            case "Revisões":
                category = Main.Variables.Guild_Global.getCategoryById(config.get("Ticket.Categories.Revisões"));;
                return category;
            case "RelatarBugs":
                category = Main.Variables.Guild_Global.getCategoryById(config.get("Ticket.Categories.RelatarBugs"));
                return category;
            case "Reportar":
                category = Main.Variables.Guild_Global.getCategoryById(config.get("Ticket.Categories.Reportar"));
                return category;
            case "Outros":
                category = Main.Variables.Guild_Global.getCategoryById(config.get("Ticket.Categories.Outros"));
                return category;
            default:
                return null;
        }
    }

}
