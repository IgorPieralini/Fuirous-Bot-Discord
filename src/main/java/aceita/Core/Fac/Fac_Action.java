package aceita.Core.Fac;

import aceita.Main;
import aceita.MySQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;

public class Fac_Action extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if (!event.getGuild().getId().equals(Main.Variables.Guild_Factions.getId())) return;
        String sql = "SELECT * FROM factions WHERE user = '" + event.getUser().getId() + "'";
        Connection conn = MySQL.getConnection();
        String role_id;
        String tag = "...";
        String name = "...";
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return;
            role_id = rs.getString("role_id");
            tag = rs.getString("tag");
            name = rs.getString("name");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (role_id == null) return;
        Role role = event.getGuild().getRoleById(role_id);
        if (role == null) return;
        event.getGuild().addRoleToMember(event.getMember(), role).queue();

        String channelname = tag.toUpperCase() + " " + Fac_Command.formatName(name);

        Main.Variables.Guild_Factions.createTextChannel(channelname)
                .setParent(Main.Variables.Fac_Category)
                .addPermissionOverride(event.getMember(), EnumSet.of(Permission.VIEW_CHANNEL), null)
                .addPermissionOverride(Main.Variables.Guild_Factions.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                .addPermissionOverride(role, EnumSet.of(Permission.VIEW_CHANNEL), null)
                .queue();

        String deleteSQL = "DELETE FROM factions WHERE user = '" + event.getUser().getId() + "'";
        try {
            PreparedStatement stmt = conn.prepareStatement(deleteSQL);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
