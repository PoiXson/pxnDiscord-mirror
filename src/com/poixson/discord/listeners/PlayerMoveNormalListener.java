package com.poixson.discord.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.poixson.discord.DiscordPlugin;
import com.poixson.tools.events.PlayerMoveNormalEvent;
import com.poixson.tools.events.xListener;


public class PlayerMoveNormalListener implements xListener {

	protected final DiscordPlugin plugin;



	public PlayerMoveNormalListener(final DiscordPlugin plugin) {
		this.plugin = plugin;
	}



	public void register() {
		xListener.super.register(this.plugin);
	}



	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onPlayerMoveNormal(final PlayerMoveNormalEvent event) {
//TODO
	}



}
