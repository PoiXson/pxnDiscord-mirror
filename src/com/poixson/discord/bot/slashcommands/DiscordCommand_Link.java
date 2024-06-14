package com.poixson.discord.bot.slashcommands;

import static com.poixson.discord.DiscordPlugin.CHAT_PREFIX;

import java.awt.Color;
import java.util.Optional;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOptionBuilder;
import org.javacord.api.interaction.SlashCommandOptionType;

import com.poixson.discord.DiscordPlugin;


public class DiscordCommand_Link {

	protected final DiscordPlugin plugin;



	public DiscordCommand_Link(final DiscordPlugin plugin, final DiscordApi discord) {
		this.plugin = plugin;
	}



	public SlashCommandBuilder getSlashCommandBuilder() {
		return new SlashCommandBuilder()
			.setName("link")
			.setDescription("Link your discord and minecraft accounts")
			.addOption(new SlashCommandOptionBuilder()
				.setName("code")
				.setDescription("Linking verification code")
				.setType(SlashCommandOptionType.LONG)
				.setRequired(false)
					.build())
			.setDefaultEnabledForEveryone();
	}



	public void onSlashCommandCreate(final SlashCommandCreateEvent event) {
		if (!this.plugin.isBotReady()) return;
		final SlashCommandInteraction interaction = event.getSlashCommandInteraction();
		final User user = interaction.getUser();
		final long user_id = user.getId();
		// account already linked
		if (this.plugin.isLinked(user_id)) {
			interaction.createImmediateResponder()
				.setFlags(MessageFlag.EPHEMERAL)
				.append(String.format("```diff\n- %s Account already linked! ```", Character.toString(0x26A0)))
				.respond();
		} else {
			final Optional<Long> arg = interaction.getArgumentLongValueByName("code");
			// /link
			if (arg.isEmpty()) {
				final long code = this.plugin.registerUserIdCode(user_id);
				// failed to create code
				if (code < 0L) {
					interaction.createImmediateResponder()
						.setFlags(MessageFlag.EPHEMERAL)
						.append(String.format("```diff\n- %s Failed to create a code!! ```", Character.toString(0x1F327)))
						.respond();
					this.log().info("Failed to create a discord link code for user: " + Long.toString(user_id));
				// created code
				} else {
					interaction.createImmediateResponder()
						.setFlags(MessageFlag.EPHEMERAL)
						.addEmbed(new EmbedBuilder()
							.setColor(Color.YELLOW)
							.setTitle(      String.format("%s Linking Your Account", Character.toString(0x1F587)))
							.setDescription(String.format("Type `/link %d` in game to complete the linking process.", Long.valueOf(code)))
						).respond();
					this.log().info("Created discord link code: " + Long.toString(user_id));
				}
			// /link <code>
			} else {
				final long code = arg.get().longValue();
				final Player player = this.plugin.getPlayerByCode(code);
				// invalid code
				if (player == null) {
					interaction.createImmediateResponder()
						.setFlags(MessageFlag.EPHEMERAL)
						.append(String.format("```diff\n- %s Invalid code! ```", Character.toString(0x26A0)))
						.respond();
					this.log().info("Invalid discord link code provided by user: " + Long.toString(user_id));
				// link accounts
				} else {
					this.plugin.registerLinkedDiscord(player, user.getId());
					interaction.createImmediateResponder()
						.setFlags(MessageFlag.EPHEMERAL)
						.addEmbed(new EmbedBuilder()
							.setColor(Color.GREEN)
							.setTitle(String.format("%s Account linked successfully!", Character.toString(0x1F91D)))
							.setDescription(String.format("Join the __*%s*__ voice channel for proximity chat.", this.plugin.getVoiceChannel()))
						).respond();
					player.sendMessage(String.format(
						"%sLinked to your discord account\n%sJoin the %s%s%s voice channel for proximity chat.",
						CHAT_PREFIX, CHAT_PREFIX,
						ChatColor.GOLD, this.plugin.getVoiceChannel(),
						ChatColor.RESET
					));
					this.log().info("Linked discord account for player: " + player.getName());
				}
			}
		}
	}



	public Logger log() {
		return this.plugin.log();
	}



}
