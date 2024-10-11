package aceita;

import aceita.Core.Config.Config_Actions;
import aceita.Core.Config.Config_Command;
import aceita.Core.Fac.Fac_Action;
import aceita.Core.Fac.Fac_Command;
import aceita.Core.Giveways.Button;
import aceita.Core.Giveways.Command;
import aceita.Core.Giveways.Giveways;
import aceita.Core.Status.Status_Actions;
import aceita.Core.Status.Status_Command;
import aceita.Core.Ticket.Categories.*;
import aceita.Core.Ticket.Handler;
import aceita.Core.Ticket.Ticket_Close;
import aceita.Core.Ticket.Ticket_Open;
import aceita.Events.Join;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static aceita.Core.Giveways.Giveways.giveways;

public class Main {

    public static JDA Bot;
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void main(String[] args) throws InterruptedException, SQLException {

        saveFile("config.yml");
        MySQL.load();

        ConfigManager config = new ConfigManager("config.yml");

        if (config.get("Bot.Token") == null) {
            System.out.println("Error");
            System.out.println("Faltam informações no arquivo de configuração!");
            System.exit(-1);
        }

        JDA bot = JDABuilder.createDefault(config.get("Bot.Token"))
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .enableIntents(GatewayIntent.GUILD_MESSAGES)
                .enableIntents(GatewayIntent.GUILD_MESSAGE_REACTIONS)
                .enableIntents(GatewayIntent.GUILD_VOICE_STATES)
                .enableIntents(GatewayIntent.GUILD_PRESENCES)
                .enableIntents(GatewayIntent.DIRECT_MESSAGES)
                .enableIntents(GatewayIntent.DIRECT_MESSAGE_REACTIONS)
                .enableIntents(GatewayIntent.GUILD_INVITES)
                .enableIntents(GatewayIntent.GUILD_WEBHOOKS)
                .enableIntents(GatewayIntent.GUILD_MESSAGE_TYPING)
                .enableIntents(GatewayIntent.DIRECT_MESSAGE_TYPING)
                .build();

        OptionData fac_choice = new OptionData(OptionType.STRING, "opção", "[ " + bot.getSelfUser().getName() + " ] Confirmar uma facção", true)
           .addChoice("Confirmar", "confirmar")
           .addChoice("Negar", "negar");

        bot.updateCommands().addCommands(
                Commands.slash("config", "[ " + bot.getSelfUser().getName() + " ] Configurar o Bot"),
                Commands.slash("status", "[ " + bot.getSelfUser().getName() + " ] Definir o Status"),
                Commands.slash("ticket-close", "[ " + bot.getSelfUser().getName() + " ] Fechar Ticket"),
                Commands.slash("giveways", "[ " + bot.getSelfUser().getName() + " ] Criar um Sorteio"),
                Commands.slash("fac", "[ " + bot.getSelfUser().getName() + " ] Confirmar uma Facção")
                   .addOptions(fac_choice)
        ).queue();

        bot.addEventListener(new Config_Command());
        bot.addEventListener(new Config_Actions());
        bot.addEventListener(new Join());
        bot.addEventListener(new Status_Actions());
        bot.addEventListener(new Status_Command());
        bot.addEventListener(new Ticket_Close());
        bot.addEventListener(new Ticket_Open());
        bot.addEventListener(new Handler());
        bot.addEventListener(new Fac_Command());
        bot.addEventListener(new Fac_Action());
        bot.addEventListener(new Button());
        bot.addEventListener(new Command());

        bot.awaitReady();

        Main.Bot = bot;
        Variables.load();

        bot.getPresence().setActivity(Activity.customStatus(config.get("Bot.Activity.Message").replaceAll("%member_amount%", Variables.Guild_Global.getMemberCount() + "").replaceAll("%bot_name%", bot.getSelfUser().getName())));

        String styleNow = config.get("Config.Status.Active");
        if (Variables.CanalDeStatus != null) {
            Variables.CanalDeStatus.getManager().setName(config.get("Config.Status.Style." + styleNow)).queue();
        }

        if (Variables.Ticket_Channel != null) {
            MessageHistory history = new MessageHistory(Variables.Ticket_Channel);
            history.retrievePast(100).queue(messages -> {
                if (messages.isEmpty()) return;
                for (Message message : messages) {
                    Variables.Ticket_Channel.deleteMessageById(message.getId()).queue();
                }
            });

            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(Color.ORANGE);
            embed.setDescription("✨・Selecione uma opção abaixo para abrir um ticket.");

            StringSelectMenu.Builder selectMenu = StringSelectMenu.create("TicketSelectMenu")
                    .setPlaceholder("Selecione uma opção")
                    .addOption("Dúvidas/Problemas", "Dúvidas/Problemas", "Teve alguma dúvida ou problema?", Emoji.fromFormatted("❓"))
                    .addOption("Confirmar Facção", "ConfirmarFacção", "Confirme sua facção", Emoji.fromFormatted("⚔️"))
                    .addOption("Parcerias", "Parcerias", "Uma possível parceria?", Emoji.fromFormatted("🤝"))
                    .addOption("Revisões", "Revisões", "Punição errada? Peça uma revisão.", Emoji.fromFormatted("❌"))
                    .addOption("Relatar Bugs", "RelatarBugs", "Relate algum bug do servidor.", Emoji.fromFormatted("🛠️"))
                    .addOption("Reportar", "Reportar", "Reporte um jogador.", Emoji.fromFormatted("📮"))
                    .addOption("Outros", "Outros", "Fale conosco sobre algo diferente.", Emoji.fromFormatted("🏷️"));

            Thread.sleep(450);

            Variables.Ticket_Channel.sendMessageEmbeds(embed.build()).setActionRow(selectMenu.build()).queue();

        }

        Giveways.loadAll();
        loadTickets();

        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                for (Giveways giveway : giveways.values()) {
                    giveway.removeTime();
                    long time = giveway.getTime();
                    if (time <= 0) {
                        giveways.remove(giveway.getCode());
                        String award = giveway.getAward();
                        int winners = giveway.getWinners();
                        String name = giveway.getName();
                        TextChannel channel = giveway.getChannel();

                        List<String> users = new ArrayList<>();
                        try {
                            String sql = "SELECT * FROM giveways_members WHERE code = '" + giveway.getCode() + "'";
                            PreparedStatement statement = MySQL.getConnection().prepareStatement(sql);
                            ResultSet result = statement.executeQuery();
                            while (result.next()) {
                                users.add(result.getString("user"));
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        Guild guild = Main.Variables.Guild_Global;
                        Random random2 = new Random(100000);
                        Random random = new Random(random2.nextInt(1, 100000000));
                        StringBuilder winnersMSG = new StringBuilder();
                        AtomicInteger setado = new AtomicInteger();
                        while (setado.get() < winners) {
                            int index = random.nextInt(0, users.size());
                            String winnerString = users.get(index);
                            guild.retrieveMemberById(winnerString).queue(
                                    winner -> {
                                        users.remove(winnerString);
                                        setado.getAndIncrement();
                                        winnersMSG.append(winner.getAsMention()).append(" ");
                                    });
                        }
                        if (setado.get() == 0) {
                            channel.sendMessage("🎉・Sorteio **" + name + "** encerrado! Ninguém ganhou o prêmio **" + award + "**").queue();
                        } else {
                            channel.sendMessage("🎉・Sorteio **" + name + "** encerrado! Ganhador(es) do Prêmio **" + award + "** é o(s) " + winnersMSG).queue();
                        }

                        try {
                            String sql = "DELETE FROM giveways WHERE code = '" + giveway.getCode() + "'";
                            PreparedStatement statement = MySQL.getConnection().prepareStatement(sql);
                            statement.executeUpdate();
                            String sql2 = "DELETE FROM giveways_members WHERE code = '" + giveway.getCode() + "'";
                            PreparedStatement statement2 = MySQL.getConnection().prepareStatement(sql2);
                            statement2.executeUpdate();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }, 0, 1, TimeUnit.SECONDS);

    }

    private static void loadTickets() {
        try {
            String sql = "SELECT * FROM tickets";
            Connection conn = MySQL.getConnection();
            PreparedStatement statement = conn.prepareStatement(sql);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                String channelID = result.getString("channel");
                String userID = result.getString("user");
                String type = result.getString("type");
                Variables.Guild_Global.retrieveMemberById(userID).queue(
                        member -> {
                            TextChannel channel = Variables.Guild_Global.getTextChannelById(channelID);
                            if (channel != null) {
                                channel.sendMessage("👋・Olá " + member.getAsMention() + "! O questionario terá que ser refeito, eu fui reiniciado...").queue();
                                switch (type) {
                                    case "Dúvidas/Problemas" -> Duvidas.Start(channel, member.getUser());
                                    case "ConfirmarFacção" -> Confirmar_Facção.Start(channel, member.getUser());
                                    case "Parcerias" -> Parcerias.Start(channel, member.getUser());
                                    case "Revisões" -> Revisões.Start(channel, member.getUser());
                                    case "RelatarBugs" -> Relatar_Bug.Start(channel, member.getUser());
                                    case "Reportar" -> Reportar.Start(channel, member.getUser());
                                    case "Outros" -> Outros.Start(channel, member.getUser());
                                }
                            }
                        });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void saveFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) return;
        try (InputStream inputStream = Main.class.getResourceAsStream("/" + filePath)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Resource file not found: " + filePath);
            }
            Files.copy(inputStream, new File(filePath).toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class Variables {

        // Config Geral
        public static String Token;
        public static Guild Guild_Global;
        public static Guild Guild_Factions;
        public static Role CargoDeEntrada;
        public static GuildChannel CanalDeStatus;

        // Config Ticket
        public static TextChannel Ticket_Channel;
        public static TextChannel Ticket_Channel_Logs;
        public static Category Ticket_Category;
        public static Role Ticket_StaffRole;

        // Facções
        public static TextChannel Fac_Channel;
        public static Category Fac_Category;
        public static String Fac_Message;

        public static void load() {
            ConfigManager config = new ConfigManager("config.yml");
            Token = config.get("Bot.Token");
            Guild_Global = Bot.getGuildById(config.get("Bot.Guilds.Global"));
            Guild_Factions = Bot.getGuildById(config.get("Bot.Guilds.Factions"));
            CargoDeEntrada = Guild_Global.getRoleById(config.get("Config.JoinRole"));
            CanalDeStatus = Guild_Global.getGuildChannelById(config.get("Config.Status.Channel"));

            Ticket_Channel_Logs = Guild_Global.getTextChannelById(config.get("Ticket.Logs"));
            Ticket_Channel = Guild_Global.getTextChannelById(config.get("Ticket.Channel"));
            Ticket_Category = Guild_Global.getCategoryById(config.get("Ticket.Category"));
            Ticket_StaffRole = Guild_Global.getRoleById(config.get("Ticket.StaffRole"));

            Fac_Channel = Guild_Global.getTextChannelById(config.get("Config.Factions.Channel"));
            Fac_Category = Guild_Factions.getCategoryById(config.get("Config.Factions.Category"));
            Fac_Message = config.get("Config.Factions.Message");
        }

    }

}