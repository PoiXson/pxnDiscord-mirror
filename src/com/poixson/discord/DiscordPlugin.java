package com.poixson.discord;

import static com.poixson.utils.Utils.IsEmpty;
import static com.poixson.utils.Utils.SafeClose;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.javacord.api.DiscordApi;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.poixson.discord.bot.Bot;
import com.poixson.discord.commands.Commands;
import com.poixson.tools.xJavaPlugin;


public class DiscordPlugin extends xJavaPlugin {
	@Override public int getSpigotPluginID() { return 117334; }
	@Override public int getBStatsID() {       return 22211;  }
	public static final String CHAT_PREFIX = ChatColor.DARK_AQUA+"[Discord] "+ChatColor.WHITE;

	protected final AtomicReference<Bot> bot = new AtomicReference<Bot>(null);

	protected final ConcurrentHashMap<UUID, Long> discord_links = new ConcurrentHashMap<UUID, Long>();
	protected final AtomicReference<CodeManager> codes = new AtomicReference<CodeManager>(null);

	protected final AtomicReference<Commands> commands = new AtomicReference<Commands>(null);



	public DiscordPlugin() {
		super(DiscordPlugin.class);
	}



	@Override
	public void onEnable() {
		super.onEnable();
		// discord bot
		{
			final String token = this.getDiscordToken();
			if (IsEmpty(token)) {
				this.log().warning("Discord app token not set");
			} else {
				// discord bot
				final Bot bot = new Bot(this, token);
				final Bot previous_bot = this.bot.getAndSet(bot);
				if (previous_bot != null)
					previous_bot.close();
				// link codes
				final CodeManager codes = new CodeManager(this);
				final CodeManager previous_codes = this.codes.getAndSet(codes);
				if (previous_codes != null)
					previous_codes.stop();
				codes.start();
			}
		}
		// commands
		if (this.isBotReady()) {
			final Commands commands = new Commands(this);
			final Commands previous = this.commands.getAndSet(commands);
			if (previous != null)
				previous.close();
		}
	}

	@Override
	public void onDisable() {
		super.onDisable();
		// commands
		{
			final Commands commands = this.commands.getAndSet(null);
			if (commands != null)
				commands.close();
		}
		// discord bot
		{
			final Bot bot = this.bot.getAndSet(null);
			if (bot != null)
				bot.close();
		}
		// link codes
		{
			final CodeManager codes = this.codes.getAndSet(null);
			if (codes != null)
				codes.stop();
		}
	}



	public Bot getBot() {
		return this.bot.get();
	}
	public DiscordApi getDiscordAPI() {
		final Bot bot = this.getBot();
		return (bot == null ? null : bot.getDiscordAPI());
	}

	public boolean isBotReady() {
		final Bot bot = this.getBot();
		return (bot == null ? false : bot.isReady());
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void loadConfigs() {
		super.loadConfigs();
		// config.yml
		{
			final FileConfiguration cfg = this.getConfig();
			this.config.set(cfg);
			this.configDefaults(cfg);
			cfg.options().copyDefaults(true);
		}
		// discord-links.json
		{
			final File file = new File(this.getDataFolder(), "discord-links.json");
			if (file.isFile()) {
				try {
					final BufferedReader reader = Files.newBufferedReader(file.toPath());
					final Type token = new TypeToken<HashMap<String, Long>>() {}.getType();
					final HashMap<String, Long> map = (new Gson()).fromJson(reader, token);
					final Iterator<Entry<String, Long>> it = map.entrySet().iterator();
					while (it.hasNext()) {
						final Entry<String, Long> entry = it.next();
						final UUID uuid = UUID.fromString(entry.getKey());
						final long user_id = entry.getValue().longValue();
						this.discord_links.put(uuid, user_id);
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		this.saveConfigs();
	}
	@Override
	protected void saveConfigs() {
		super.saveConfigs();
		// discord-links.json
		{
			final File file = new File(this.getDataFolder(), "discord-links.json");
			if (file.isFile()
			|| !this.discord_links.isEmpty()) {
				try {
					final String json = (new Gson()).toJson(this.discord_links);
					final BufferedWriter writer = new BufferedWriter(new FileWriter(file));
					writer.write(json);
					SafeClose(writer);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	@Override
	protected void configDefaults(final FileConfiguration config) {
		super.configDefaults(config);
		config.addDefault("Discord Token", ""                        );
		config.addDefault("Voice Channel", "<have-an-admin-set-this>");
		config.addDefault("Code Digits",   Integer.valueOf(5)        );
	}



	public String getDiscordToken() {
		return this.getConfig().getString("Discord Token");
	}



	public String getVoiceChannel() {
		return this.getConfig().getString("Voice Channel");
	}



	public int getCodeDigits() {
		return this.getConfig().getInt("Code Digits");
	}



	// -------------------------------------------------------------------------------
	// discord linked users



	public boolean isLinked(final Player player) {
		return this.isLinked(player.getUniqueId());
	}
	public boolean isLinked(final UUID uuid) {
		return this.discord_links.containsKey(uuid);
	}
	public boolean isLinked(final long user_id) {
		return this.discord_links.containsValue(Long.valueOf(user_id));
	}



	public long getDiscordUserId(final Player player) {
		final Long user_id = this.discord_links.get(player.getUniqueId());
		return (user_id == null ? -1L : user_id.longValue());
	}
	public Player getPlayerByDiscordUserId(final long user_id) {
		for (final Entry<UUID, Long> entry : this.discord_links.entrySet()) {
			if (entry.getValue().longValue() == user_id)
				return Bukkit.getPlayer(entry.getKey());
		}
		return null;
	}



	public boolean registerLinkedDiscord(final Player player, final long user_id) {
		this.setConfigChanged();
		final Long existing = this.discord_links.putIfAbsent(player.getUniqueId(), Long.valueOf(user_id));
		return (existing != null);
	}
	public boolean unregisterLinkedDiscord(final Player player) {
		this.setConfigChanged();
		final Long user_id = this.discord_links.remove(player.getUniqueId());
		return (user_id != null);
	}



	// -------------------------------------------------------------------------------
	// link codes



	public long registerPlayerCode(final Player player) {
		return this.codes.get()
			.registerPlayerCode(player);
	}
	public long registerPlayerCode(final UUID uuid) {
		return this.codes.get()
			.registerPlayerCode(uuid);
	}
	public long registerUserIdCode(final long user_id) {
		return this.codes.get()
			.registerUserIdCode(user_id);
	}



	public Player getPlayerByCode(final long code) {
		return this.codes.get()
			.getPlayerByCode(code);
	}
	public long getUserIdByCode(final long code) {
		return this.codes.get()
			.getUserIdByCode(code);
	}



}
