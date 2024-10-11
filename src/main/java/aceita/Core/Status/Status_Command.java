package aceita.Core.Status;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.awt.*;

public class Status_Command extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("status")) {
            event.deferReply().setEphemeral(true).queue();
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("📬・Status");
            embed.setDescription("✨・Selecione uma opção abaixo na qual você deseja definir o status.");
            embed.setColor(Color.ORANGE);

            StringSelectMenu.Builder selectMenu = StringSelectMenu.create("StatusSelectMenu")
                    .setPlaceholder("Selecione uma opção")
                    .addOption("Online", "Online", "Clique para selecionar o status Online", Emoji.fromUnicode("U+1F7E2"))
                    .addOption("Offline", "Offline", "Clique para selecionar o status Offline", Emoji.fromUnicode("U+1F534"))
                    .addOption("Manutenção", "Maintenance", "Clique para selecionar o status Manutenção", Emoji.fromUnicode("U+1F7E0"));

            event.getHook().editOriginalEmbeds(embed.build()).setActionRow(selectMenu.build()).queue();
        }
    }

}
