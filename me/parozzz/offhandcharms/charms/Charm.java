/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.offhandcharms.charms;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import me.parozzz.offhandcharms.charms.effects.EffectPotion;
import me.parozzz.offhandcharms.utilities.reflection.nbt.NBTCompound;
import me.parozzz.offhandcharms.utilities.reflection.nbt.item.ItemNBT;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author Paros
 */
public class Charm 
{
    private final List<String> charmLore;
    
    private final ItemNBT nbt;
    private final NBTCompound tag;
    public Charm(final ItemStack item)
    {
        charmLore = item.getItemMeta().hasLore() ? item.getItemMeta().getLore() : new ArrayList<>();
        
        nbt = new ItemNBT(item);
        tag = nbt.getTag();
    }
    
    private final static String POTION_COMPOUND = "OffHandPotion.PotionCompound";
    public Charm setValid()
    {
        tag.setTag(POTION_COMPOUND, new NBTCompound());
        return this;
    }
    
    public boolean isValid()
    {
        return tag.hasKey(POTION_COMPOUND);
    }
    
    public Optional<EffectPotion> getPotion(final PotionEffectType pet)
    {
        NBTCompound compound = tag.getCompound(POTION_COMPOUND);
        return compound.hasKey(pet.getName()) ? Optional.of(new EffectPotion(pet, compound.getCompound(pet.getName()))) : Optional.empty();
    }
    
    public void setPotion(final EffectPotion potion)
    {
        getPotion(potion.getType()).ifPresent(old -> old.removeLore(charmLore));
        potion.refreshLore(charmLore);
        tag.getCompound(POTION_COMPOUND).setTag(potion.getType().getName(), potion.getCompound());
    }
    
    public Set<PotionEffectType> getPotions()
    {
        return tag.getCompound(POTION_COMPOUND).keySet().stream().map(PotionEffectType::getByName).collect(Collectors.toSet());
    }
    
    public void removePotion(final PotionEffectType pet)
    {
        this.getPotion(pet).ifPresent(ep -> ep.removeLore(charmLore));
        tag.getCompound(POTION_COMPOUND).removeKey(pet.getName());
    }
    
    public OptionCompound getOptionCompound()
    {
        return new OptionCompound(tag, charmLore);
    }
    
    public ItemStack getItem()
    {
        return nbt.getBukkitItem(charmLore);
    }
    
    public ItemStack getItem(final int quality)
    {
        OptionCompound option = new OptionCompound(tag, charmLore);
        if(option.isUpgradable())
        {
            option.setQuality(quality);
        }
        return nbt.getBukkitItem(charmLore);
    }
    
    public ItemStack getClonedItem(final int quality)
    {
        ItemNBT nbt = new ItemNBT(this.nbt.getBukkitItem());
        NBTCompound tag = nbt.getTag();
        
        List<String> charmLore = new ArrayList<>(this.charmLore);
        
        OptionCompound option = new OptionCompound(tag, charmLore);
        if(option.isUpgradable())
        {
            option.setQuality(quality);
        }
        
        return nbt.getBukkitItem(charmLore);
    }
    
    @Override
    public String toString()
    {
        return tag.toString();
    }
}
