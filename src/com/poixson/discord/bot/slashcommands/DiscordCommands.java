package com.poixson.discord.bot.slashcommands;

import java.io.Closeable;
import java.util.HashSet;

import org.javacord.api.DiscordApi;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.server.ServerBecomesAvailableEvent;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import org.javacord.api.listener.server.ServerBecomesAvailableListener;

import com.poixson.discord.DiscordPlugin;


public class DiscordCommands implements Closeable,
SlashCommandCreateListener, ServerBecomesAvailableListener {

	protected final DiscordPlugin plugin;
	protected final DiscordApi discord;

	protected final DiscordCommand_Link   cmd_link;   // /link
	protected final DiscordCommand_Unlink cmd_unlink; // /unlink



	public DiscordCommands(final DiscordPlugin plugin, final DiscordApi discord) {
		this.plugin  = plugin;
		this.discord = discord;
		final HashSet<SlashCommandBuilder> commands = new HashSet<SlashCommandBuilder>();
		this.cmd_link   = new DiscordCommand_Link  (plugin, discord);
		this.cmd_unlink = new DiscordCommand_Unlink(plugin, discord);
		commands.add(this.cmd_link  .getSlashCommandBuilder());
		commands.add(this.cmd_unlink.getSlashCommandBuilder());
//		for (final SlashCommandBuilder cmd : commands)
//			cmd.createGlobal(discord).join();
		discord.bulkOverwriteGlobalApplicationCommands(commands).join();
		discord.addSlashCommandCreateListener(this);
System.out.println(discord.getGlobalSlashCommands().join().size());
	}



	@Override
	public void close() {
		this.discord.removeListener(this);
	}



	@Override
	public void onSlashCommandCreate(final SlashCommandCreateEvent event) {
		final String cmd = event.getSlashCommandInteraction().getCommandName();
		switch (cmd) {
		case "link":   this.cmd_link  .onSlashCommandCreate(event); break;
		case "unlink": this.cmd_unlink.onSlashCommandCreate(event); break;
		default: break;
		}
	}



	@Override
	public void onServerBecomesAvailable(final ServerBecomesAvailableEvent event) {
		this.plugin.log().info("Connected to discord");
	}



}
