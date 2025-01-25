package com.poixson.discord.commands;

import java.io.Closeable;

import com.poixson.discord.DiscordPlugin;


public class Commands implements Closeable {

	protected final Command_Link   cmd_link;   // /link
	protected final Command_Unlink cmd_unlink; // /unlink



	public Commands(final DiscordPlugin plugin) {
		this.cmd_link   = new Command_Link  (plugin);
		this.cmd_unlink = new Command_Unlink(plugin);
	}



	@Override
	public void close() {
		this.cmd_link.close();
	}



}
