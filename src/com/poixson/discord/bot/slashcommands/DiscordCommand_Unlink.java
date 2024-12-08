package com.poixson.discord.bot.slashcommands;

import static com.poixson.discord.DiscordPlugin.CHAT_PREFIX;

import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;

import com.poixson.discord.DiscordPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;


public class DiscordCommand_Unlink {

	protected final DiscordPlugin plugin;



	public DiscordCommand_Unlink(final DiscordPlugin plugin, final DiscordApi discord) {
		this.plugin = plugin;
	}



	public SlashCommandBuilder getSlashCommandBuilder() {
		return new SlashCommandBuilder()
			.setName("unlink")
			.setDescription("Unlink your discord and minecraft accounts")
			.setDefaultEnabledForEveryone();
	}



	public void onSlashCommandCreate(final SlashCommandCreateEvent event) {
		if (!this.plugin.isBotReady()) return;
		final SlashCommandInteraction interaction = event.getSlashCommandInteraction();
		final User user = interaction.getUser();
		final long user_id = user.getId();
		final Player player = this.plugin.getPlayerByDiscordUserId(user_id);
		// not linked
		if (player == null) {
			interaction.createImmediateResponder()
				.setFlags(MessageFlag.EPHEMERAL)
				.append(String.format(
					"```diff\n- %s Your account is not linked to discord ```",
					Character.toString(0x1F327)
				))
				.respond();
		// is linked
		} else {
			// has permission
			if (player.hasPermission("discord.cmd.unlink")) {
				// unlinked
				if (this.plugin.unregisterLinkedDiscord(player)) {
					interaction.createImmediateResponder()
						.setFlags(MessageFlag.EPHEMERAL)
						.append(String.format(
							"```diff\n- %s Unlinked your discord account ```",
							Character.toString(0x1F64C)
						))
						.respond();
					player.sendMessage(CHAT_PREFIX.append(Component.text(
						"Unlinked your discord account").color(NamedTextColor.YELLOW)));
					this.log().info("Unlinked discord account: " + player.getName());
				// unlink failed
				} else {
					interaction.createImmediateResponder()
						.setFlags(MessageFlag.EPHEMERAL)
						.append(String.format(
							"```diff\n- %s Failed to unlink your discord account ```",
							Character.toString(0x1F327)
						))
						.respond();
					player.sendMessage(CHAT_PREFIX.append(Component.text(
						"Failed to unlink your discord account").color(NamedTextColor.DARK_RED)));
					this.log().info("Failed to unlink from discord: " + player.getName());
				}
			// no permission
			} else {
				interaction.createImmediateResponder()
					.setFlags(MessageFlag.EPHEMERAL)
					.append(String.format(
						"```diff\n- %s You don't have permission to unlink from discord ```",
						Character.toString(0x1F327)
					))
					.respond();
				player.sendMessage(CHAT_PREFIX.append(Component.text(
					"You don't have permission to unlink from discord").color(NamedTextColor.RED)));
			}
		}
	}



	public Logger log() {
		return this.plugin.log();
	}



}
