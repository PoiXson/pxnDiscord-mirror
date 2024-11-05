package com.poixson.discord.commands;

import static com.poixson.discord.DiscordPlugin.CHAT_PREFIX;

import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.poixson.discord.DiscordPlugin;
import com.poixson.tools.commands.pxnCommandRoot;
import com.poixson.utils.MathUtils;


public class Command_Link extends pxnCommandRoot {

	protected final DiscordPlugin plugin;



	public Command_Link(final DiscordPlugin plugin) {
		super(
			plugin,
			"discord", // namespace
			"Link your discord and minecraft accounts", // desc
			null, // usage
			"discord.cmd.link", // perm
			new String[] { // labels
				"link"
			}
		);
		this.plugin = plugin;
	}



	@Override
	public boolean onCommand(final CommandSender sender, final String[] args) {
		if (!(sender instanceof Player)) return false;
		if (!this.plugin.isBotReady()) {
			sender.sendMessage(CHAT_PREFIX + "Discord bot not ready");
			return true;
		}
		final Player player = (Player) sender;
		if (!player.hasPermission("discord.cmd.link")) return false;
		// account already linked
		if (this.plugin.isLinked(player)) {
			player.sendMessage(String.format("%s%sAccount already linked!", CHAT_PREFIX, ChatColor.RED));
		} else
		// /link
		if (args.length == 0) {
			final long code = this.plugin.registerPlayerCode(player);
			// failed to create code
			if (code < 0L) {
				player.sendMessage(String.format("%s%sFailed to create a code!", CHAT_PREFIX, ChatColor.RED));
				this.log().info("Failed to create a discord link code for player: " + player.getName());
			// created code
			} else {
				player.sendMessage(String.format(
					"%s%sLinking Your Account\n" +
					"%s%sType /link %s%d%s in game to complete the linking process.",
					CHAT_PREFIX, ChatColor.AQUA,
					CHAT_PREFIX, ChatColor.AQUA,
					ChatColor.GREEN, Long.valueOf(code), ChatColor.AQUA
				));
				this.log().info("Created discord link code: " + player.getName());
			}
		// /link <code>
		} else {
			final long code = MathUtils.ToLong(args[0]);
			final long user_id = this.plugin.getUserIdByCode(code);
			// invalid code
			if (user_id <= 0L) {
				player.sendMessage(String.format("%s%sInvalid code!", CHAT_PREFIX, ChatColor.RED));
				this.log().info("Player provided invalid code: " + player.getName());
			// link accounts
			} else {
				this.plugin.registerLinkedDiscord(player, user_id);
				player.sendMessage(String.format(
					"%sLinked to your discord account\n%sJoin the %s%s%s voice channel for proximity chat.",
					CHAT_PREFIX, CHAT_PREFIX,
					ChatColor.GOLD, this.plugin.getVoiceChannel(),
					ChatColor.RESET
				));
				this.log().info("Linked discord account for player: " + player.getName());
			}
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
