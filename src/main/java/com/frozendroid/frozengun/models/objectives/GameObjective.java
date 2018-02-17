package com.frozendroid.frozengun.models.objectives;

import com.frozendroid.frozengun.events.PlayerShotEvent;
import com.frozendroid.frozengun.models.Kill;
import com.frozendroid.frozengun.models.Match;
import com.frozendroid.frozengun.models.MinigamePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Score;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class GameObjective implements Listener {

    public Match match;
    public HashMap<MinigamePlayer, ArrayList<Kill>> kills = new HashMap<>();
    public HashMap<MinigamePlayer, Integer> deaths = new HashMap<>();
    boolean achieved = false;
    private ConfigurationSection section;

    public abstract Match getMatch();

    public abstract void setMatch(Match match);

    public abstract String getEndText();

    public abstract String getTypeName();

    public abstract Object getGoal();

    public abstract void setGoal(Integer i);

    public abstract void removePlayer(MinigamePlayer player);

    public abstract void start();

    public abstract void stop();

    public abstract ArrayList<MinigamePlayer> getWinners();

    public void reset() {
        kills.clear();
    }

    public void onPlayerShot(PlayerShotEvent event) {
        if (event.getVictim().getHealth() - event.getGun().getDamage() > 0)
            return;

        Score score = match.getScoreboard().getObjective("killObjective").getScore(event.getShooter().getDisplayName());
        score.setScore(score.getScore() + 1);
        match.broadcast(event.getShooter().getName() + " killed " + event.getVictim().getName() + "!");

        Kill kill = new Kill();
        kill.setKilled(event.getVictim());
        kill.setKiller(event.getShooter());
        kill.setTime(System.currentTimeMillis());
        kills.putIfAbsent(event.getShooter(), new ArrayList<>());

        deaths.put(event.getVictim(), deaths.getOrDefault(event.getVictim(), 0) + 1);

        ArrayList<Kill> _kills = kills.get(event.getShooter());


        if (_kills.size() >= 1) {
            Kill lastKill = _kills.get(_kills.size() - 1);

            Long delta = kill.getTime() - lastKill.getTime();
            if (match.getArena().getKillingSpreeDelay() >= delta / 1000) {
                kill.setSpree(lastKill.getSpree() + 1);

                if (kill.getSpree() == 2)
                    match.broadcast(event.getShooter().getDisplayName() + " got a double kill!");
                if (kill.getSpree() == 3)
                    match.broadcast(event.getShooter().getDisplayName() + " got a triple kill!");
                if (kill.getSpree() == 4)
                    match.broadcast(event.getShooter().getDisplayName() + " got a quadra kill!");
                if (kill.getSpree() == 5)
                    match.broadcast(event.getShooter().getDisplayName() + " got a penta kill!");
                if (kill.getSpree() == 6)
                    match.broadcast(event.getShooter().getDisplayName() + " got a hexa kill!");
            }
        } else
            kill.setSpree(1);

        _kills.add(kill);
    }

    public ConfigurationSection getSection() {
        return section;
    }

    public void setSection(ConfigurationSection section) {
        this.section = section;
    }

}
