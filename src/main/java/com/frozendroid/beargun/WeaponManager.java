package com.frozendroid.beargun;

import com.frozendroid.beargun.models.Gun;
import com.frozendroid.beargun.models.Weapon;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WeaponManager {

    public static ArrayList<Gun> getGuns()
    {
        ArrayList<Gun> guns = new ArrayList<>();
        List<Weapon> weapons = MinigameManager.getWeapons().stream().filter(weapon -> weapon instanceof Gun).collect(Collectors.toList());
        for (Weapon weapon : weapons) {
            Gun gun = (Gun) weapon;
            guns.add(gun);
        }
        return guns;
    }

    public static Weapon findByName(String name)
    {
        return MinigameManager.getWeapons().stream().filter(weapon -> weapon.getName().equals(name)).findFirst().orElse(null);
    }

}