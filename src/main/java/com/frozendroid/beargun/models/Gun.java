package com.frozendroid.beargun.models;

import com.darkblade12.particleeffect.ParticleEffect;
import com.frozendroid.beargun.BearGun;
import com.frozendroid.beargun.MinigameManager;
import com.frozendroid.beargun.events.PlayerShotEvent;
import com.frozendroid.beargun.utils.Vector3D;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.Set;

public class Gun {

    private Material material;
    private Double damage = 0D;
    private String name;
    private Double cooldown = 0D;
    private Long lastShot = 0L;
    private MinigamePlayer player;

    public Gun()
    {

    }

    public Gun(Gun gun, MinigamePlayer player)
    {
        this.name       = gun.getName();
        this.material   = gun.getMaterial();
        this.damage     = gun.getDamage();
        this.cooldown   = gun.getCooldown();
        this.player     = player;
    }

    public double getCooldown() {
        return cooldown;
    }

    public void setCooldown(double cooldown) {
        this.cooldown = cooldown;
    }

    public Material getMaterial() {
        return material;
    }

    public void shoot()
    {
        int ATTACK_REACH = 100;

        lastShot = System.currentTimeMillis();

        Player shooter = player.getPlayer();

        Iterator iterator = shooter.getLineOfSight((Set) null, ATTACK_REACH).iterator();

        int blockdistance = 100;

        while (iterator.hasNext()) {
            Block block  = (Block) iterator.next();
            if (block.getType() != Material.AIR) {
                blockdistance = ((Double) block.getLocation().distance(shooter.getLocation())).intValue();
            }
        }

        Location start = shooter.getEyeLocation();
        Vector increase = start.getDirection();

        for (int counter = 0; counter < blockdistance-1; counter++) {
            Location point = start.add(increase);
            ParticleEffect.OrdinaryColor color = new ParticleEffect.OrdinaryColor(1, 0, 0);
            ParticleEffect.CRIT.display(0F, 0F, 0F, 0F, 1, point, 200D);
        }

        Location observerPos = shooter.getEyeLocation();
        Vector3D observerDir = new Vector3D(observerPos.getDirection());

        Vector3D observerStart = new Vector3D(observerPos);
        Vector3D observerEnd = observerStart.add(observerDir.multiply(ATTACK_REACH));

        Player hit = null;

        for (Player target : shooter.getWorld().getPlayers()) {
            Vector3D targetPos = new Vector3D(target.getLocation());
            Vector3D minimum = targetPos.add(-0.5, 0, -0.5);
            Vector3D maximum = targetPos.add(0.5, 1.80, 0.5);

            if (target != shooter && hasIntersection(observerStart, observerEnd, minimum, maximum)) {
                if (hit == null ||
                        hit.getLocation().distanceSquared(observerPos) >
                                target.getLocation().distanceSquared(observerPos)) {

                    hit = target;
                }
            }
        }

        if (hit != null) {
            if (
                    blockdistance > hit.getPlayer().getLocation().distance(shooter.getLocation())
                            && player.getMatch().findPlayer(hit.getUniqueId()) != null
                    )
            {
                PlayerShotEvent event = new PlayerShotEvent();
                event.setShooter(getPlayer());
                event.setVictim(MinigameManager.getPlayer(hit));
                BearGun.plugin.getServer().getPluginManager().callEvent(event);
                hit.damage(this.getDamage(), shooter);
            }

        }
    }

    public double getCooldownTime()
    {
        double time = cooldown*1000-(System.currentTimeMillis()-lastShot);
        return time >= 0? time: 0;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public boolean canShoot()
    {
        return getCooldownTime() <= 0;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static Gun findByName(String name) {
        return MinigameManager.getGuns().stream().filter(gun -> gun.getName().equalsIgnoreCase(name)).findFirst()
                .orElse(null);
    }

    public MinigamePlayer getPlayer() {
        return player;
    }

    public void setPlayer(MinigamePlayer player) {
        this.player = player;
    }

    private static boolean hasIntersection(Vector3D p1, Vector3D p2, Vector3D min, Vector3D max) {
        final double epsilon = 0.0001f;

        Vector3D d = p2.subtract(p1).multiply(0.5);
        Vector3D e = max.subtract(min).multiply(0.5);
        Vector3D c = p1.add(d).subtract(min.add(max).multiply(0.5));
        Vector3D ad = d.abs();

        if (Math.abs(c.x) > e.x + ad.x)
            return false;
        if (Math.abs(c.y) > e.y + ad.y)
            return false;
        if (Math.abs(c.z) > e.z + ad.z)
            return false;

        if (Math.abs(d.y * c.z - d.z * c.y) > e.y * ad.z + e.z * ad.y + epsilon)
            return false;
        if (Math.abs(d.z * c.x - d.x * c.z) > e.z * ad.x + e.x * ad.z + epsilon)
            return false;
        if (Math.abs(d.x * c.y - d.y * c.x) > e.x * ad.y + e.y * ad.x + epsilon)
            return false;

        return true;
    }

}