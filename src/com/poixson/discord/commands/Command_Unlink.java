package com.poixson.discord.commands;

import static com.poixson.discord.DiscordPlugin.CHAT_PREFIX;

import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.poixson.discord.DiscordPlugin;
import com.poixson.tools.commands.pxnCommandRoot;


public class Command_Unlink extends pxnCommandRoot {

	protected final DiscordPlugin plugin;



	public Command_Unlink(final DiscordPlugin plugin) {
		super(
			plugin,
			"discord", // namespace
			"Unlink your discord and minecraft accounts", // desc
			null, // usage
			"discord.cmd.unlink", // perm
			new String[] { // labels
				"unlink"
			}
		);
		this.plugin = plugin;
	}



	@Override
	public boolean onCommand(final CommandSender sender, final String[] args) {
		if (!(sender instanceof Player)) return false;
		final Player player = (Player) sender;
		if (!player.hasPermission("discord.cmd.unlink")) return false;
		if (this.plugin.unregisterLinkedDiscord(player)) {
			player.sendMessage(String.format("%s%sUnlinked your discord account", CHAT_PREFIX, ChatColor.RED));
			this.log().info("Unlinked discord account: " + player.getName());
		} else {
			player.sendMessage(String.format("%s%sYour account is not linked to discord", CHAT_PREFIX, ChatColor.DARK_RED));
		}
		return true;
	}



	@Override
	public List<String> onTabComplete(final CommandSender sender, final String[] args) {
		return null;
	}



	public Logger log() {
		return this.plugin.log();
	}



}
