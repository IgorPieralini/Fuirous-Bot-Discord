package aceita.Core.Config;

import aceita.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.awt.*;

public class Config_Command extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("config")) {
            event.deferReply().setEphemeral(true).queue();

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("⚙️・Configurações");
            embed.setColor(Color.ORANGE);
            embed.setDescription("✨・Selecione uma opção abaixo na qual você deseja configurar o bot.");

            StringSelectMenu.Builder selectMenu = StringSelectMenu.create("ConfigSelectMenu")
                    .setPlaceholder("Selecione uma opção")
                    .addOption("Cargo de Entrada", "CargoDeEntrada", "Clique para configurar o cargo de entrada", Emoji.fromUnicode("U+1F4EC"))
                    .addOption("Canal de Status", "CanalDeStatus", "Clique para configurar o canal de status", Emoji.fromUnicode("U+1F527"))
                    .addOption("Facções", "Facções", "Clique para configurar o canal de facções", Emoji.fromFormatted("🏷️"));

            event.getHook().editOriginalEmbeds(embed.build()).setActionRow(selectMenu.build()).queue();
        }
    }

}
