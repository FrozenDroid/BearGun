package com.frozendroid.frozengun.models;

import com.frozendroid.frozengun.FrozenGun;
import com.frozendroid.frozengun.Messenger;
import com.frozendroid.frozengun.MinigameManager;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Lobby {

    private Arena arena;
    private Location location;
    private List<MinigamePlayer> players = new ArrayList<>();
    private long countdownTime = 20;
    private long timeTillStart = 20;
    private long startingSince;

    private BukkitTask starting_timer;
    private BukkitTask checker_timer;
    private BukkitTask waiting_timer;

    public Arena getArena() {
        return arena;
    }

    public void setArena(Arena arena) {
        this.arena = arena;
    }

    public void setTimeTillStart(Integer timeTillStart)
    {
        this.timeTillStart = timeTillStart;
    }

    public long getTimeTillStart()
    {
        return timeTillStart;
    }

    public boolean isWaiting() {
        return waiting_timer != null;
    }

    public void startWaitingTimerIfNotStarted() {
        if (!this.isWaiting()) {
            this.timeTillStart = this.countdownTime;
            this.startWaitingTimer();
        }
    }

    public void startWaitingTimer()
    {
        this.timeTillStart = this.countdownTime;
        waiting_timer = FrozenGun.plugin.getServer().getScheduler().runTaskTimer(FrozenGun.plugin, () -> {
            if (checker_timer == null) {
                startLobbyChecker();
            }
            int delta = arena.getMinPlayers()-players.size();
            if (delta != 0)
                players.forEach((player) -> player.getPlayer().sendMessage(Messenger.infoMsg("Waiting for "+delta+" more players to join...")));
        }, 0L, 20L*5L);
    }

    public void stop()
    {
        if (waiting_timer != null) {
            waiting_timer.cancel();
            this.waiting_timer = null;
        }

        if (checker_timer != null) {
            checker_timer.cancel();
            this.checker_timer = null;
        }

        if (starting_timer != null) {
            starting_timer.cancel();
            this.starting_timer = null;
        }
    }

    public void reset() {
        this.stop();
        this.players = new ArrayList<>();
    }

    public void startLobbyChecker()
    {
        checker_timer = FrozenGun.plugin.getServer().getScheduler().runTaskTimer(FrozenGun.plugin, () -> {
            if (arena.getMinPlayers() <= players.size()) {
                startingSince = System.currentTimeMillis() / 1000L;
                startStartingTimer();
                if (this.waiting_timer != null)
                    this.waiting_timer.cancel();
                this.waiting_timer = null;
                if (this.checker_timer != null)
                    this.checker_timer.cancel();
                this.checker_timer = null;
            }
        }, 0L, 10L);
    }

    public void startStartingTimer()
    {
        starting_timer = FrozenGun.plugin.getServer().getScheduler().runTaskTimer(FrozenGun.plugin, () -> {
            players.forEach((player) -> {
                if (timeTillStart % 5 == 0 && timeTillStart > 0) {
                    player.getPlayer().sendMessage(Messenger.infoMsg("Starting in " + timeTillStart));
                } else if (timeTillStart <= 5 && timeTillStart > 0) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1);
                    player.getPlayer().sendMessage(Messenger.infoMsg("Starting in " + timeTillStart));
                }
            });
            if (timeTillStart <= 0) {
                Match match = new Match();
                MinigameManager.addMatch(match);
                match.setArena(arena);
                players.forEach(match::addPlayer);
                match.start();
                players.forEach((player) -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 2F, 2F));
                if (this.starting_timer != null) {
                   this.starting_timer.cancel();
                }
            }
            timeTillStart--;
        }, 0L, 20L);
    }

    public void addPlayer(MinigamePlayer player)
    {
        player.sendMessage(Messenger.infoMsg("Joined the queue for " + arena.getName()) + ".");
        MinigameManager.addPlayer(player);
        players.add(player);
    }

    public void removePlayer(MinigamePlayer player)
    {
        MinigameManager.removePlayer(player);
        players.remove(player);
    }

    public List<MinigamePlayer> getPlayers() {
        return players;
    }

    public Optional<Location> getLocation() {
        return Optional.ofNullable(location);
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public long getCountdownTime() {
        return countdownTime;
    }

    public void setCountdownTime(long countdownTime) {
        this.countdownTime = countdownTime;
    }
}