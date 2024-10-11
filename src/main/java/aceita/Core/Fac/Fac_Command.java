package aceita.Core.Fac;

import aceita.Main;
import aceita.MySQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

public class Fac_Command extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("fac")) {
            String choice = event.getOption("opção").getAsString();
            event.deferReply().setEphemeral(true).queue();
            String tag = "...";
            String name = "...";
            try {
                String sql = "SELECT * FROM tickets WHERE channel = '" + event.getChannelId() + "'";
                PreparedStatement stmt = MySQL.getConnection().prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) {
                    event.getHook().editOriginal("❌・Esse canal não é um ticket.").queue();
                    return;
                }
                String check = rs.getString("other");
                if (check == null) {
                    event.getHook().editOriginal("❌・Comando disponível apenas em tickets de confirmar facção.").queue();
                    return;
                }
                String[] data = check.split(":::");
                tag = data[0];
                name = data[1];
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if (choice.equals("negar")) {
                event.getHook().editOriginal("✅・Facção não confirmada.").queue();
                event.getChannel().sendMessage("❌・Sua facção não pode ser confirmada.").queue();
            } else if (choice.equals("confirmar")) {
                event.getHook().editOriginal("✅・Facção confirmada.").queue();
                event.getChannel().sendMessage("✅・Sua facção foi confirmada.").queue();

                EmbedBuilder embed = new EmbedBuilder();
                String message = Main.Variables.Fac_Message.replace("%tag%", tag).replace("%name%", name);
                embed.setDescription(message);
                embed.setTimestamp(Instant.now());
                embed.setColor(Color.ORANGE);

                Main.Variables.Fac_Channel.sendMessageEmbeds(embed.build()).queue();

                Guild guild = Main.Variables.Guild_Factions;

                String roleName = tag.toUpperCase() + " " + formatName(name);

                createInvite(guild, event.getChannel().asTextChannel());

                guild.createRole().setName(roleName).setMentionable(true).queue(role -> {
                    String updateSQL = "UPDATE factions SET role_id = '" + role.getId() + "' WHERE ticket = '" + event.getChannel().getId() + "'";
                    try {
                        Connection conn2 = MySQL.getConnection();
                        PreparedStatement stmt = conn2.prepareStatement(updateSQL);
                        stmt.executeUpdate();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

    private void createInvite(Guild guild, TextChannel responseChannel) {
        TextChannel defaultChannel = guild.getDefaultChannel().asTextChannel();
        defaultChannel.createInvite()
                .setMaxAge(3600)
                .setMaxUses(1)
                .queue(invite -> {
                    String inviteUrl = invite.getUrl();
                    responseChannel.sendMessage("🏷️・Por favor, entre no servidor abaixo\n" + inviteUrl).queue();
                });
    }

    public static String formatName(String original) {
        if (original == null) {
            return null;
        }
        if (original.length() == 0) {
            return original;
        }
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }

}
