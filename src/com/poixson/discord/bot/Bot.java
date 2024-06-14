package com.poixson.discord.bot;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.util.logging.FallbackLoggerConfiguration;

import com.poixson.discord.DiscordPlugin;
import com.poixson.discord.bot.slashcommands.DiscordCommands;
import com.poixson.utils.FileUtils;


public class Bot implements Closeable {

	protected final DiscordPlugin plugin;

	protected final DiscordApi discord;
	protected final DiscordCommands slash_commands;

	protected final AtomicBoolean ready = new AtomicBoolean(false);



	public Bot(final DiscordPlugin plugin, final String token) {
		this.plugin = plugin;
		FallbackLoggerConfiguration.setDebug(true);
		// connect
		plugin.log().info("Connecting Discord Bot..");
		FallbackLoggerConfiguration.setDebug(true);
		FallbackLoggerConfiguration.setTrace(true);
		// discord api
		this.discord = new DiscordApiBuilder()
			.setToken(token)
			.setWaitForServersOnStartup(true)
			.addIntents(Intent.MESSAGE_CONTENT)
			.login()
			.join();
		// default avatar
		if (this.discord.getYourself().hasDefaultAvatar()) {
			try {
				final InputStream in = FileUtils.OpenResource(this.getClass(), "icon.png");
				BufferedImage icon = ImageIO.read(in);
				this.discord.updateAvatar(icon);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// discord command listener
		this.slash_commands = new DiscordCommands(this.plugin, this.discord);
		// bot is ready
		this.ready.set(true);
		if (this.isReady())
			plugin.log().info("Discord Bot Connected!");
	}



	public void close() {
		this.ready.set(false);
		this.slash_commands.close();
		this.discord.disconnect()
			.join();
	}



	public boolean isReady() {
		return this.ready.get();
	}



	public void update() {
//TODO
	}



	public DiscordApi getDiscordAPI() {
		return this.discord;
	}



}
