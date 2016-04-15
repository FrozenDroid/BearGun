package com.frozendroid.beargun.listeners;

import com.frozendroid.beargun.BearGun;
import com.frozendroid.beargun.Messenger;
import com.frozendroid.beargun.MinigameManager;
import com.frozendroid.beargun.events.PlayerKilledEvent;
import com.frozendroid.beargun.events.PlayerShotEvent;
import com.frozendroid.beargun.models.*;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.SplashPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;

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
    public void onItemDrop(PlayerDropItemEvent event)
    {
        Item item = event.getItemDrop();
        Player player = event.getPlayer();

        Bukkit.broadcastMessage("x: " + item.getVelocity().getX() + ", y: " + item.getVelocity().getY() + ", z: " + item.getVelocity().getZ());

        MinigamePlayer minigamePlayer = MinigameManager.getPlayer(player);

        if (minigamePlayer == null)
            return;

        Match match = minigamePlayer.getMatch();

        if (match == null)
            return;

        Weapon weapon = null;

        ArrayList<Weapon> weapons = minigamePlayer.getWeapons();
        for (Weapon _weapon : weapons) {
            if (_weapon.getMaterial() == event.getItemDrop().getItemStack().getType()) {
                BearGun.info("Grenade is not null!!");
                weapon = _weapon;
            }
        }

        if (weapon == null)
            return;



        if (weapon instanceof Grenade) {
            Grenade grenade = (Grenade) weapon;
            grenade.throwWeapon(item);
        }


    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event)
    {
        if (!(event.getEntity().getShooter() instanceof Player))
            return;


        Player player =(Player) event.getEntity().getShooter();

        MinigamePlayer minigamePlayer = MinigameManager.getPlayer(player);

        if (minigamePlayer == null)
            return;

        Match match = minigamePlayer.getMatch();

        if (match == null)
            return;

        Weapon weapon = null;

        if (event.getEntity().getType() == EntityType.SPLASH_POTION) {
            Bukkit.broadcastMessage("potion has been thrown");
            SplashPotion potion = (SplashPotion) event.getEntity();
            ItemStack item = potion.getItem();

            Item _item = potion.getLocation().getWorld().dropItemNaturally(player.getLocation(), item);
            Vector vector = new Vector();
            _item.teleport(player.getEyeLocation());
            _item.getLocation().setDirection(player.getEyeLocation().getDirection());
//            _item.getlo(player.getEyeLocation().getDirection());
            Bukkit.getPluginManager().callEvent(new PlayerDropItemEvent(player.getPlayer(), _item));

//            player.getInventory().

            event.setCancelled(true);
        }


//        Bukkit.broadcastMessage(event.getEntity().getType().getEntityClass().getName());

//        ArrayList<Weapon> weapons = minigamePlayer.getWeapons();
//        for (Weapon _weapon : weapons) {
//            if (_weapon.getMaterial() == ) {
//                BearGun.info("Grenade is not null!!");
//                weapon = _weapon;
//            }
//        }
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
                evt.getPlayer().sendMessage(Messenger.infoMsg("Couldn't find the arena :("));
                return;
            }

            MinigamePlayer player = MinigameManager.getPlayer(evt.getPlayer());

            if (arena.hasQueue()) {
                if (arena.getQueue().getPlayers().contains(player)) {
                    evt.getPlayer().sendMessage(Messenger.infoMsg("Already in queue!"));
                    return;
                }

                arena.getQueue().addPlayer(new MinigamePlayer(evt.getPlayer()));
                return;
            }
            if (arena.isOccupied()) {
                evt.getPlayer().sendMessage(Messenger.infoMsg("This arena is already in use!"));
                return;
            }

            Queue queue = new Queue();
            queue.setArena(arena);
            queue.addPlayer(new MinigamePlayer(evt.getPlayer()));
            queue.startWaitingTimer();
        }

        if (evt.getAction() == Action.RIGHT_CLICK_BLOCK || evt.getAction() == Action.RIGHT_CLICK_AIR) {
            MinigamePlayer player = MinigameManager.getPlayer(evt.getPlayer());

            if(player == null)
                return;

            if (!player.isInMatch())
                return;

            Weapon weapon = player.getWeaponInHand();

            if (weapon == null)
                return;

            if (weapon instanceof Gun) {
                Gun gun = (Gun) weapon;
                if (gun.canShoot())
                    gun.shoot();
            }

        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event)
    {
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)
            return;

        if (event.getEntityType() == EntityType.PLAYER)
        {
            Player player = (Player) event.getEntity();

            MinigamePlayer _player = MinigameManager.getPlayer(player);

            if (_player == null) {
                return;
            }

            if (!_player.isInMatch()) {
                return;
            }
            event.setDamage(9999);
        }
    }

    @EventHandler
    public void onPlayerShot(PlayerShotEvent event)
    {
        if (event.getVictim().getHealth() <= 0) {
            PlayerKilledEvent playerKilledEvent = new PlayerKilledEvent();
//            playerKilledEvent
        }
    }

}
