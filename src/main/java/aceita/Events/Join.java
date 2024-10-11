package aceita.Events;

import aceita.ConfigManager;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Join extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent event) {
        System.out.println("User joined: " + event.getUser().getAsTag());
    }

}
