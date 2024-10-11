package aceita.Core.Giveways;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.time.Instant;

import static aceita.Core.Giveways.Util.addTimeToCurrent;
import static aceita.Core.Giveways.Util.createCode;

public class Command extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("giveways")) {
            TextInput name = TextInput.create("name", "Nome do Sorteio", TextInputStyle.SHORT)
                    .setMinLength(1)
                    .setRequired(true)
                    .build();

            TextInput description = TextInput.create("description", "Descrição do Sorteio", TextInputStyle.PARAGRAPH)
                    .setMinLength(1)
                    .setRequired(true)
                    .build();

            TextInput winners = TextInput.create("winners", "Quantidade de Ganhadores", TextInputStyle.SHORT)
                    .setMinLength(1)
                    .setRequired(true)
                    .build();

            TextInput award = TextInput.create("award", "Prêmio do Sorteio", TextInputStyle.SHORT)
                    .setMinLength(1)
                    .setRequired(true)
                    .build();

            TextInput time = TextInput.create("time", "Tempo do Sorteio", TextInputStyle.SHORT)
                    .setPlaceholder("Ex: 10m, 1h, 1d")
                    .setMinLength(1)
                    .setRequired(true)
                    .build();

            Modal modal = Modal.create("giveways", "✨・Sorteio")
                    .addComponents(ActionRow.of(name), ActionRow.of(description), ActionRow.of(winners), ActionRow.of(award), ActionRow.of(time))
                    .build();
            event.replyModal(modal).queue();
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getModalId().equals("giveways")) {
            event.deferReply().setEphemeral(true).queue();
            String name = event.getValue("name").getAsString();
            String description = event.getValue("description").getAsString();
            String winnersString = event.getValue("winners").getAsString();
            String award = event.getValue("award").getAsString();
            String time = event.getValue("time").getAsString();
            if (name.isEmpty() || description.isEmpty() || winnersString.isEmpty() || award.isEmpty()) {
                event.getHook().editOriginal("❌・Todos os campos devem ser preenchidos.").queue();
                return;
            }
            int winners = 0;
            if (winnersString.matches("[0-9]+")) {
                winners = Integer.parseInt(winnersString);
            } else {
                event.getHook().editOriginal("❌・A quantidade de ganhadores deve ser um número.").queue();
                return;
            }
            if (winners < 1) {
                event.getHook().editOriginal("❌・A quantidade de ganhadores deve ser maior que 0.").queue();
                return;
            }
            Instant futureInstant = addTimeToCurrent(time);
            if (futureInstant.getEpochSecond() <= Instant.now().getEpochSecond()) {
                event.getHook().editOriginal("❌・Use os exemplos abaixo para setar o tempo: " +
                        "\n- 1**s** (Segundos)" +
                        "\n- 1**m** (Minutos)" +
                        "\n- 1**h** (Horas)" +
                        "\n- 1**d** (Dias)" +
                        "\n- 1**semana** (Semanas)" +
                        "\n- 1**mes** (Meses)" +
                        "\n- 1**ano** (Anos)").queue();
                return;
            }

            Giveways.createGiveway(event.getChannel().asTextChannel(), name, description, winners, award, time);
            event.getHook().editOriginal("✅・Sorteio criado com sucesso.").queue();
        }
    }

}
