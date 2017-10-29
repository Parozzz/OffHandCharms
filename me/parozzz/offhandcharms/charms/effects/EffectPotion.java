/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.offhandcharms.charms.effects;

import java.util.List;
import java.util.Objects;
import me.parozzz.offhandcharms.Configs;
import me.parozzz.offhandcharms.utilities.reflection.NBTTagManager;
import me.parozzz.offhandcharms.utilities.reflection.NBTTagManager.NBTType;
import me.parozzz.offhandcharms.utilities.reflection.nbt.NBTCompound;
import org.bukkit.ChatColor;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author Paros
 */
public final class EffectPotion 
{
    private final static String LEVEL = "Level";
    private final static String LORE = "Lore";
    private final static String UPGRADE = "Upgrade";
    private final static String DEFAULT = "Default";
    
    private final PotionEffectType pet;
    private final NBTCompound compound;
    public EffectPotion(final PotionEffectType pet, final NBTCompound compound)
    {
        this.pet = pet;
        
        this.compound = new NBTCompound();
        setLevel(compound.getKey(LEVEL, int.class))
                .setLore(compound.getKey(LORE, String.class))
                .setUpgrade(compound.getKey(UPGRADE, byte.class) == 1)
                .setDefault(compound.getKey(DEFAULT, byte.class) == 1);
    }
    
    public EffectPotion(final PotionEffectType pet)
    {
        this.pet = pet;
        compound = new NBTCompound();
    }

    public PotionEffectType getType()
    {
        return pet;
    }

    public EffectPotion setLevel(final int level)
    {
        compound.setValue(LEVEL, NBTType.INT, level);
        setLore(getDefaultLore(level));
        return this;
    }

    public EffectPotion setUpgrade(final boolean upgrade)
    {
        compound.setValue(UPGRADE, NBTType.BYTE, upgrade ? (byte)1 : (byte)0);
        return this;
    }

    public EffectPotion setDefault(final boolean isDefault)
    {
        compound.setValue(DEFAULT, NBTType.BYTE, isDefault ? (byte)1 : (byte)0);
        return this;
    }

    public void refreshLore(final List<String> lore)
    {
        setLore(lore, getLevel());
    }

    public void setLore(final List<String> lore, final int level)
    {
        String newLore = getDefaultLore(level);
        if(this.isUpgrade())
        {
            newLore += ChatColor.GREEN+ChatColor.BOLD.toString()+" +";
        }

        if(compound.hasKey(LORE))
        {
            int index = lore.indexOf(compound.getKey(LORE, String.class));
            if(index == -1)
            {
                lore.add(newLore);
            }
            else
            {
                lore.set(index, newLore);
            }
        }
        else
        {
            lore.add(newLore);
        }

        setLore(newLore);
    }
    
    
    
    private String getDefaultLore(final int level)
    {
        return Configs.getEffectOptions(pet).getName().replace("{level}", Objects.toString(level + 1));
    }
    
    private EffectPotion setLore(final String lore)
    {
        compound.setValue(LORE, NBTType.STRING, lore);
        return this;
    }
    
    
    
    public void removeLore(final List<String> lore)
    {
        lore.remove(compound.getKey(LORE, String.class));
    }

    public int getLevel()
    {
        return compound.hasKey(LEVEL) ? compound.getKey(LEVEL, int.class) : -1;
    }

    public boolean isDefault()
    {
        return compound.hasKey(DEFAULT) ? compound.getKey(DEFAULT, byte.class) == 1 : true;
    }

    public boolean isUpgrade()
    {
        return compound.hasKey(UPGRADE) ? compound.getKey(UPGRADE, byte.class) == 1 : false; 
    }

    public PotionEffect getPotionEffect()
    {
        return new PotionEffect(pet, Integer.MAX_VALUE, getLevel(), false, false);
    }

    public NBTCompound getCompound()
    {
        return this.compound;
    }
}
