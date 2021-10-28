package me.c0wg0d.namesync;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;

public class JoinListener implements Listener {

	private SettingsManager settings = SettingsManager.getInstance();
    public static Permission perms = null;

	@EventHandler(priority = EventPriority.LOWEST)
	public void onLogin(PlayerJoinEvent e)
	{
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        if(!NameSyncApi.containsUUID(uuid))
        {
            NameSyncApi.addUUID(player);
        }
        else if(!NameSyncApi.nameFromUUID(uuid).equals(player.getName()))
        {
            NameSyncApi.updateName(player);
        }

		try {
			String host = settings.getConfig().getString("mysql.host");
			String port = settings.getConfig().getString("mysql.port");
			String user = settings.getConfig().getString("mysql.user");
			String password = settings.getConfig().getString("mysql.password");
			String database = settings.getConfig().getString("mysql.database");
			String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
			Connection connection = DriverManager.getConnection(url, user, password);

			String xfUserTable = settings.getConfig().getString("xenforo.user_table");
			String xfUsernameColumn = settings.getConfig().getString("xenforo.username_column");
			String xfUuidColumn = settings.getConfig().getString("xenforo.uuid_column");
			String xfGroupColumn = settings.getConfig().getString("xenforo.group_column");

			PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + xfUserTable + " WHERE " + xfUuidColumn + " = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			statement.setString(1, uuid.toString());
			ResultSet results = statement.executeQuery();

			String xfGroups = "";
			String currXfUsername = "";
			boolean xfHasUuid = true;
			boolean needsUpdate = false;

			if(!results.first() && !results.next()) {
				Bukkit.getServer().getLogger().log(Level.WARNING, "[NameSync] Player [" + player.getName() + "] does not have a UUID in Xenforo!");
				needsUpdate = true;
				xfHasUuid = false;
				statement = connection.prepareStatement("SELECT * FROM " + xfUserTable + " WHERE " + xfUsernameColumn + " = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				statement.setString(1, player.getName());
				results = statement.executeQuery();
			}

			if(!results.first() && !results.next()) {
				Bukkit.getServer().getLogger().log(Level.WARNING, "[NameSync] Player [" + player.getName() + "] does not exist in Xenforo!");
			}
			else {
				results.first();
				xfGroups = results.getString(xfGroupColumn);
				currXfUsername = results.getString(xfUsernameColumn);
			}
			results.close();
            RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
            perms = rsp.getProvider();

			// Username
			if(currXfUsername != player.getName()) {
				needsUpdate = true;
			}

			// VIP
            String vipGroupId = settings.getConfig().getString("groups.vip");

			if(perms.playerInGroup(player.getPlayer(), "vip")) {
				if(!xfGroups.contains(vipGroupId)) {
					needsUpdate = true;

					if(xfGroups.isEmpty()) {
						xfGroups = vipGroupId;
					}
					else {
						xfGroups += "," + vipGroupId;
					}
				}
			}
			else {
				// Also check if we need to remove the group from xenforo
				if(xfGroups.contains(vipGroupId)) {
					needsUpdate = true;

					if(xfGroups.contains(",")) {
						xfGroups = xfGroups.replace(vipGroupId + ",", "");
						xfGroups = xfGroups.replace("," + vipGroupId, "");
					}
					else {
						xfGroups = xfGroups.replace(vipGroupId, "");
					}
				}
			}

			// Helper
			String helperGroupId = settings.getConfig().getString("groups.helper");

			if(perms.playerInGroup(player.getPlayer(), "helper")) {
				if(!xfGroups.contains(helperGroupId)) {
					needsUpdate = true;

					if(xfGroups.isEmpty()) {
						xfGroups = helperGroupId;
					}
					else {
						xfGroups += "," + helperGroupId;
					}
				}
			}
			else {
				// Also check if we need to remove the group from xenforo
				if(xfGroups.contains(helperGroupId)) {
					needsUpdate = true;

					if(xfGroups.contains(",")) {
						xfGroups = xfGroups.replace(helperGroupId + ",", "");
						xfGroups = xfGroups.replace("," + helperGroupId, "");
					}
					else {
						xfGroups = xfGroups.replace(helperGroupId, "");
					}
				}
			}

			// Moderator
			String moderatorGroupId = settings.getConfig().getString("groups.moderator");

			if(perms.playerInGroup(player.getPlayer(), "moderator")) {
				if(!xfGroups.contains(moderatorGroupId)) {
					needsUpdate = true;

					if(xfGroups.isEmpty()) {
						xfGroups = moderatorGroupId;
					}
					else {
						xfGroups += "," + moderatorGroupId;
					}
				}
			}
			else {
				// Also check if we need to remove the group from xenforo
				if(xfGroups.contains(moderatorGroupId)) {
					needsUpdate = true;

					if(xfGroups.contains(",")) {
						xfGroups = xfGroups.replace(moderatorGroupId + ",", "");
						xfGroups = xfGroups.replace("," + moderatorGroupId, "");
					}
					else {
						xfGroups = xfGroups.replace(moderatorGroupId, "");
					}
				}
			}

			// Elder
			String elderGroupId = settings.getConfig().getString("groups.elder");

			if(perms.playerInGroup(player.getPlayer(), "elder")) {
				if(!xfGroups.contains(elderGroupId)) {
					needsUpdate = true;

					if(xfGroups.isEmpty()) {
						xfGroups = elderGroupId;
					}
					else {
						xfGroups += "," + elderGroupId;
					}
				}
			}
			else {
				// Also check if we need to remove the group from xenforo
				if(xfGroups.contains(elderGroupId)) {
					needsUpdate = true;

					if(xfGroups.contains(",")) {
						xfGroups = xfGroups.replace(elderGroupId + ",", "");
						xfGroups = xfGroups.replace("," + elderGroupId, "");
					}
					else {
						xfGroups = xfGroups.replace(elderGroupId, "");
					}
				}
			}

			// Update the user in Xenforo
			if(needsUpdate) {
				// If they don't have a UUID, add it based on their username
				String sql = "UPDATE " + xfUserTable + " SET " + xfGroupColumn + " = ?, " + xfUuidColumn + " = ?, " + xfUsernameColumn + " = ? WHERE " + xfUsernameColumn + " = ?";

				// If they have a UUID, update their username
				if(xfHasUuid) {
					sql = "UPDATE " + xfUserTable + " SET " + xfGroupColumn + " = ?, " + xfUuidColumn + " = ?, " + xfUsernameColumn + " = ? WHERE " + xfUuidColumn + " = ?";
				}
				PreparedStatement update = connection.prepareStatement(sql);
				update.setString(1, xfGroups);
				update.setString(2, uuid.toString());
				update.setString(3, player.getName());
				update.setString(4, player.getName());
				if(xfHasUuid) {
					update.setString(4, uuid.toString());
				}

				Bukkit.getServer().getLogger().log(Level.INFO, "[NameSync] Setting Xenforo groups to [" + xfGroups + "] for player [" + player.getName() + "] with UUID [" + uuid.toString() + "]" );
				update.executeUpdate();
				update.close();
			}

		}
		catch (SQLException ex) {
			ex.printStackTrace();
		}
    }
}