package aceita.Core.Status;

import aceita.Main;
import aceita.ConfigManager;
import aceita.MySQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.List;

public class Status_Actions extends ListenerAdapter {

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (event.getComponent().getId().equals("StatusSelectMenu")) {
            StringSelectMenu.Builder selectMenu = StringSelectMenu.create("StatusSelectMenu")
                    .setPlaceholder("Selecione uma opção")
                    .addOption("Online", "Online", "Clique para selecionar o status Online", Emoji.fromUnicode("U+1F7E2"))
                    .addOption("Offline", "Offline", "Clique para selecionar o status Offline", Emoji.fromUnicode("U+1F534"))
                    .addOption("Manutenção", "Maintenance", "Clique para selecionar o status Manutenção", Emoji.fromUnicode("U+1F7E0"));
            event.getMessage().editMessageComponents().setActionRow(selectMenu.build()).queue();

            String option = event.getSelectedOptions().get(0).getValue();
            event.deferReply().setEphemeral(true).queue();
            ConfigManager config = new ConfigManager("config.yml");
            String channelID = config.get("Config.Status.Channel");
            VoiceChannel channel = Main.Variables.Guild_Global.getVoiceChannelById(channelID);
            String style = config.get("Config.Status.Style." + option);
            if (style == null) {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("📬・Status");
                embed.setDescription("❌・Não foi possível definir o status, por favor tente novamente mais tarde");
                embed.setColor(Color.RED);
                event.getHook().editOriginalEmbeds(embed.build()).queue();
                return;
            }
            if (channel == null) {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("📬・Status");
                embed.setDescription("❌・Não foi possível definir o status, por favor tente novamente mais tarde");
                embed.setColor(Color.RED);
                event.getHook().editOriginalEmbeds(embed.build()).queue();
                return;
            }

            recreateChannel(channel.getGuild(), channel, style);

            config.set("Config.Status.Active", option);
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("📬・Status");
            embed.setDescription("✨・Status definido para '" + style + "'");
            embed.setColor(Color.ORANGE);
            event.getHook().editOriginalEmbeds(embed.build()).queue();
            config.save();
        }
    }

    private void recreateChannel(Guild guild, VoiceChannel channel, String name) {
        Category category = channel.getParentCategory();
        List<Permission> permissions = channel.getPermissionOverrides().stream()
                .flatMap(override -> override.getAllowed().stream())
                .toList();

        if (!permissions.isEmpty()) {
            EnumSet<Permission> permissionSet = EnumSet.copyOf(permissions);

            guild.createVoiceChannel(name)
                    .setParent(category)
                    .setPosition(channel.getPosition())
                    .addPermissionOverride(guild.getPublicRole(), permissionSet, null)
                    .queue(newChannel -> {
                        copyPermissions(channel, newChannel);
                        channel.delete().queue();
                        ConfigManager config = new ConfigManager("config.yml");
                        config.set("Config.Status.Channel", newChannel.getId());
                        config.save();
                    });
        } else {
            guild.createVoiceChannel(name)
                    .setParent(category)
                    .setPosition(channel.getPosition())
                    .queue(newChannel -> {
                        copyPermissions(channel, newChannel);
                        channel.delete().queue();
                        ConfigManager config = new ConfigManager("config.yml");
                        config.set("Config.Status.Channel", newChannel.getId());
                        config.save();
                    });
        }
    }

    private void copyPermissions(VoiceChannel oldChannel, VoiceChannel newChannel) {
        oldChannel.getPermissionOverrides().forEach(override -> {
            if (override.getRole() != null) {
                newChannel.upsertPermissionOverride(override.getRole())
                        .grant(override.getAllowed())
                        .deny(override.getDenied())
                        .queue();
            } else if (override.getMember() != null) {
                newChannel.upsertPermissionOverride(override.getMember())
                        .grant(override.getAllowed())
                        .deny(override.getDenied())
                        .queue();
            }
        });
    }

}
