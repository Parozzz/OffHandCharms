/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.offhandcharms.charms;

import java.util.List;
import java.util.Objects;
import me.parozzz.offhandcharms.Configs;
import me.parozzz.offhandcharms.utilities.reflection.NBTTagManager.NBTType;
import me.parozzz.offhandcharms.utilities.reflection.nbt.NBTCompound;

/**
 *
 * @author Paros
 */
public class OptionCompound 
{
    private final static String COMPOUND = "OffHandCharm.OptionCompound";
    
    private final List<String> lore;
    private final NBTCompound compound;
    public OptionCompound(final NBTCompound tag, final List<String> lore)
    {
        this.lore = lore;
        if(!tag.hasKey(COMPOUND))
        {
            tag.setTag(COMPOUND, (compound = new NBTCompound()));
        }
        else
        {
            compound = tag.getCompound(COMPOUND);
        }
    }
    
    private final static String NOT_UPGRADABLE="upgradable";
    public OptionCompound setUpgradable(final boolean upgradable)
    {
        compound.setValue(NOT_UPGRADABLE, NBTType.BYTE, upgradable ? (byte)1 : (byte)0);
        return this;
    }
    
    public boolean isUpgradable()
    {
        return compound.getKey(NOT_UPGRADABLE, byte.class) != 0;
    }
    
    private void manageLore(final String newLore)
    {
        if(compound.hasKey("Lore"))
        {
            lore.set(lore.indexOf(compound.getKey("Lore", String.class)), newLore);
        }
        else
        {
            lore.add(newLore);
        }
        
        compound.setValue("Lore", NBTType.STRING, newLore);
    }
    
    private final static String CHANCE_VALUE= "upgradeChance";
    public OptionCompound setSuccessChance(final int success)
    {
        if(this.isUpgradable())
        {
            manageLore(Configs.chanceLore.replace("{chance}", Objects.toString(success)));
            compound.setValue(CHANCE_VALUE, NBTType.INT, success); 
        }
        return this;
    }
    
    public boolean hasChance()
    {
        return compound.hasKeyOfType(CHANCE_VALUE, NBTType.INT);
    }
    
    public int getChance()
    {
        return compound.getKey(CHANCE_VALUE, int.class);
    }
    
    public void removeChance()
    {
        compound.removeKey(CHANCE_VALUE);
    }
    
    private final static String QUALITY= "Quality";
    public OptionCompound setQuality(final int quality)
    {
        if(isUpgradable())
        {
            manageLore(Configs.qualityLore.replace("{quality}", Objects.toString(quality)));
            compound.setValue(QUALITY, NBTType.INT, quality);
        }
        return this;
    }
    
    public int getQuality()
    {
        return compound.getKey(QUALITY, int.class);
    }
}
