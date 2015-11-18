package com.frozendroid.beargun.listeners;

import com.frozendroid.beargun.MinigameManager;
import com.frozendroid.beargun.models.Arena;
import com.frozendroid.beargun.models.Gun;
import com.frozendroid.beargun.models.MinigamePlayer;
import com.frozendroid.beargun.models.Queue;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

public class ActionListener implements Listener {
    final int ATTACK_REACH = 100;
    private Plugin plugin;

    public ActionListener(Plugin plugin)
    {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event)
    {
        Block block = event.getBlock();
        Sign sign = (Sign) event.getBlock().getState();
        String[] lines = event.getLines();
        Player player = event.getPlayer();

        if (!lines[0].equals("[railgun]"))
            return;

        if (!player.hasPermission("railgun.create.sign")) {
            player.sendMessage("You're not allowed to create a join sign.");
            block.breakNaturally();
            return;
        }

        if (lines[1].equals("")) {
            player.sendMessage("You need to fill in the arena name in the second row.");
            block.breakNaturally();
            return;
        }

        String arena_name = lines[1];

        Arena arena = Arena.getByName(arena_name);

        if (arena == null) {
            player.sendMessage("Arena not found.");
            block.breakNaturally();
            return;
        }

        event.setCancelled(true);

        sign.setLine(0, ChatColor.DARK_AQUA+"[Railgun]");
        sign.setLine(1, ChatColor.AQUA+"Join");
        sign.setLine(2, ChatColor.BLACK+arena.getName());
        sign.update();

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent evt)
    {
        if (evt.getAction() == Action.RIGHT_CLICK_BLOCK &&
                (evt.getClickedBlock().getType() == Material.WALL_SIGN
                || evt.getClickedBlock().getType() == Material.SIGN)
                ) {
            Sign sign = (Sign) evt.getClickedBlock().getState();
            String[] lines = sign.getLines();

            if (!lines[0].equals(ChatColor.DARK_AQUA+"[Railgun]"))
                return;

            if (lines[1].equals("")) {
                evt.getPlayer().sendMessage("The second row is empty.. so we'll never be able to find the arena to join.");
                return;
            }

            String arena_name = lines[2];
            Arena arena = Arena.getByName(arena_name);

            if (arena == null) {
                evt.getPlayer().sendMessage("Couldn't find the arena :(");
                return;
            }

            if (arena.hasQueue()) {
                arena.getQueue().addPlayer(new MinigamePlayer(evt.getPlayer()));
                return;
            }

            Queue queue = new Queue();
            queue.setArena(arena);
            queue.addPlayer(new MinigamePlayer(evt.getPlayer()));
            queue.startWaitingTimer();
        }

        MinigamePlayer player = MinigameManager.getPlayer(evt.getPlayer());


        if(player == null)
            return;

        if (!player.isInMatch())
            return;

        Gun gun = player.getGun();

        if(evt.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            if(evt.getPlayer().getItemInHand().getType() == gun.getMaterial()) {
                if (gun.canShoot())
                    gun.shoot();
            }
        }

    }

}