package com.poixson.discord;

import static com.poixson.utils.BukkitUtils.SafeCancel;
import static com.poixson.utils.NumberUtils.IsMinMax;

import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.poixson.tools.xRand;
import com.poixson.tools.abstractions.HashMapTimeout;
import com.poixson.tools.abstractions.xStartStop;


public class CodeManager extends BukkitRunnable implements xStartStop {
	public static final long INTERVAL_TICK    = 1200L; // 1 minute
	public static final long INTERVAL_CLEANUP =    5L; // 5 minutes

	protected final DiscordPlugin plugin;
	protected final xRand rnd = new xRand();

	protected final HashMapTimeout<UUID, Long> codes_players = new HashMapTimeout<UUID, Long>();
	protected final HashMapTimeout<Long, Long> codes_users   = new HashMapTimeout<Long, Long>();



	public CodeManager(final DiscordPlugin plugin) {
		this.plugin = plugin;
		this.codes_players.setTimeoutTicks(INTERVAL_CLEANUP);
		this.codes_users  .setTimeoutTicks(INTERVAL_CLEANUP);
	}



	@Override
	public void start() {
		this.runTaskTimerAsynchronously(this.plugin, INTERVAL_TICK, INTERVAL_TICK);
	}
	@Override
	public void stop() {
		SafeCancel(this);
	}



	// timeouts
	@Override
	public void run() {
		this.codes_players.run();
		this.codes_users  .run();
	}



	public long getNewCode() {
		final int code_digits = this.plugin.getCodeDigits();
		if (!IsMinMax(code_digits, 2, 10))
			throw new RuntimeException("Invalid code digits: "+Integer.toString(code_digits));
		final int code_max = ((int)Math.pow(10, code_digits)) - 1;
		final int code_min = code_max / 9;
		return this.rnd.nextLong(code_min, code_max);
	}



	public long registerPlayerCode(final Player player) {
		return this.registerPlayerCode(player.getUniqueId());
	}
	public long registerPlayerCode(final UUID uuid) {
		this.codes_players.flush(uuid);
		for (int i=0; i<100; i++) {
			final long code = this.getNewCode();
			final Long cod  = Long.valueOf(code);
			if (!this.codes_players.containsValue(cod)) {
				this.codes_players.put(uuid, cod);
				return code;
			}
		}
		return -1;
	}
	public long registerUserIdCode(final long user_id) {
		final Long id = Long.valueOf(user_id);
		this.codes_users.flush(id);
		for (int i=0; i<100; i++) {
			final long code = this.getNewCode();
			final Long cod  = Long.valueOf(code);
			if (!this.codes_users.containsValue(cod)) {
				this.codes_users.put(id, cod);
				return code;
			}
		}
		return -1;
	}



	public Player getPlayerByCode(final long code) {
		if (this.codes_players.containsValue(Long.valueOf(code))) {
			for (final Entry<UUID, Long> entry : this.codes_players.entrySet()) {
				if (code == entry.getValue().longValue()) {
					final UUID found = entry.getKey();
					this.codes_players.remove(found);
					this.codes_players.flush(found);
					return Bukkit.getPlayer(found);
				}
			}
		}
		return null;
	}
	public long getUserIdByCode(final long code) {
		if (this.codes_users.containsValue(Long.valueOf(code))) {
			for (final Entry<Long, Long> entry : this.codes_users.entrySet()) {
				if (code == entry.getValue().longValue()) {
					final long found = entry.getKey().longValue();
					final Long id = Long.valueOf(found);
					this.codes_users.remove(id);
					this.codes_users.flush(id);
					return found;
				}
			}
		}
		return -1L;
	}



}
