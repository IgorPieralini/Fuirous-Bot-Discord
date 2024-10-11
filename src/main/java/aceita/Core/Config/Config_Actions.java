package aceita.Core.Config;

import aceita.Main;
import aceita.ConfigManager;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

public class Config_Actions extends ListenerAdapter {

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (event.getComponent().getId().equals("ConfigSelectMenu")) {
            StringSelectMenu.Builder selectMenu = StringSelectMenu.create("ConfigSelectMenu")
                    .setPlaceholder("Selecione uma opção")
                    .addOption("Cargo de Entrada", "CargoDeEntrada", "Clique para configurar o cargo de entrada", Emoji.fromUnicode("U+1F4EC"))
                    .addOption("Canal de Status", "CanalDeStatus", "Clique para configurar o canal de status", Emoji.fromUnicode("U+1F527"))
                    .addOption("Facções", "Facções", "Clique para configurar o canal de facções", Emoji.fromFormatted("🏷️"));
            event.getMessage().editMessageComponents().setActionRow(selectMenu.build()).queue();

            String option = event.getSelectedOptions().get(0).getValue();
            if (option.equals("CargoDeEntrada")) {
                TextInput reason = TextInput.create("cargo", "Coloque o ID do cargo", TextInputStyle.SHORT)
                        .setPlaceholder("ID do Cargo")
                        .setMinLength(1)
                        .setRequired(true)
                        .setStyle(TextInputStyle.SHORT)
                        .build();

                Modal modal = Modal.create("CargoDeEntrada", "⚙️・Configurações do Bot")
                        .addActionRows(ActionRow.of(reason))
                        .build();
                event.replyModal(modal).queue();
            } else if (option.equals("CanalDeStatus")) {
                TextInput reason = TextInput.create("canal", "Coloque o ID do canal", TextInputStyle.SHORT)
                        .setPlaceholder("ID do Canal")
                        .setMinLength(1)
                        .setRequired(true)
                        .setStyle(TextInputStyle.SHORT)
                        .build();

                Modal modal = Modal.create("CanalDeStatus", "⚙️・Configurações do Bot")
                        .addActionRows(ActionRow.of(reason))
                        .build();
                event.replyModal(modal).queue();
            } else if (option.equals("Facções")) {
                TextInput reason = TextInput.create("canal", "Coloque o ID do canal", TextInputStyle.SHORT)
                        .setPlaceholder("ID do Canal")
                        .setMinLength(1)
                        .setRequired(true)
                        .setStyle(TextInputStyle.SHORT)
                        .build();

                Modal modal = Modal.create("Facções", "⚙️・Configurações do Bot")
                        .addActionRows(ActionRow.of(reason))
                        .build();
                event.replyModal(modal).queue();
            }
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getModalId().equals("CargoDeEntrada")) {
            event.deferReply().setEphemeral(true).queue();
            String cargo = event.getValue("cargo").getAsString();
            if (cargo.length() < 18) {
                event.getHook().editOriginal("❌・ID do Cargo inválido.").queue();
                return;
            }
            Role role = Main.Variables.Guild_Global.getRoleById(cargo);
            if (role == null) {
                event.getHook().editOriginal("❌・Cargo não encontrado.").queue();
                return;
            }

            ConfigManager config = new ConfigManager("config.yml");
            config.set("Config.JoinRole", cargo);
            config.save();

            event.getHook().editOriginal("✅・Cargo de Entrada setado para " + role.getAsMention() + ".").queue();
        } else if (event.getModalId().equals("CanalDeStatus")) {
            event.deferReply().setEphemeral(true).queue();
            String canal = event.getValue("canal").getAsString();
            if (canal.length() < 18) {
                event.getHook().editOriginal("❌・ID do Canal inválido.").queue();
                return;
            }
            Channel channel = Main.Variables.Guild_Global.getGuildChannelById(canal);
            if (channel == null) {
                event.getHook().editOriginal("❌・Canal não encontrado.").queue();
                return;
            }
            ConfigManager config = new ConfigManager("config.yml");
            config.set("Config.Status.Channel", canal);
            config.save();

            event.getHook().editOriginal("✅・Canal de Status setado para " + channel.getAsMention() + ".").queue();
        } else if (event.getModalId().equals("Facções")) {
            event.deferReply().setEphemeral(true).queue();
            String canal = event.getValue("canal").getAsString();
            if (canal.length() < 18) {
                event.getHook().editOriginal("❌・ID do Canal inválido.").queue();
                return;
            }
            Channel channel = Main.Variables.Guild_Global.getGuildChannelById(canal);
            if (channel == null) {
                event.getHook().editOriginal("❌・Canal não encontrado.").queue();
                return;
            }
            ConfigManager config = new ConfigManager("config.yml");
            config.set("Config.Factions.Channel", canal);
            config.save();

            event.getHook().editOriginal("✅・Canal de Facções setado para " + channel.getAsMention() + ".").queue();
        }
    }

}
