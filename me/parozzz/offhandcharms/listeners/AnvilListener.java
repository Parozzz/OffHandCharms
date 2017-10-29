/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.offhandcharms.listeners;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;
import me.parozzz.offhandcharms.Configs;
import me.parozzz.offhandcharms.Configs.EffectOption;
import me.parozzz.offhandcharms.charms.Charm;
import me.parozzz.offhandcharms.charms.effects.EffectPotion;
import me.parozzz.offhandcharms.charms.OptionCompound;
import me.parozzz.offhandcharms.utilities.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author Paros
 */
public class AnvilListener implements Listener
{
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onAnvilPrepare(final PrepareAnvilEvent e)
    {
        Optional.ofNullable(e.getInventory().getItem(0)).filter(item -> item.getAmount() == 1).map(Charm::new).filter(Charm::isValid).ifPresent(firstCharm -> 
        {
            OptionCompound firstOption = firstCharm.getOptionCompound();
            
            Optional.ofNullable(e.getInventory().getItem(1)).filter(item -> firstOption.isUpgradable()).map(Charm::new).filter(Charm::isValid).map(secondCharm -> 
            {
                OptionCompound secondOption = secondCharm.getOptionCompound();
                if(!secondOption.isUpgradable())
                {
                    e.setResult(new ItemStack(Material.AIR));
                    return secondCharm;
                }
                
                int totalChance = firstOption.getQuality() + secondOption.getQuality();
                
                Set<PotionEffectType> firstTypes = firstCharm.getPotions();
                if(firstTypes.stream().allMatch(pet -> firstCharm.getPotion(pet).map(EffectPotion::getLevel).get() >= Configs.getEffectOptions(pet).getMaxLevel()))
                {
                    e.setResult(new ItemStack(Material.AIR));
                    return firstCharm;
                }
                
                Set<PotionEffectType> secondTypes = secondCharm.getPotions();
                firstTypes.retainAll(secondTypes);
                secondTypes.removeAll(firstTypes);
                
                Charm result = new Charm(e.getInventory().getItem(0).clone());
                
                secondTypes.stream().map(secondCharm::getPotion).map(Optional::get).map(ep -> ep.setUpgrade(true).setDefault(false)).forEach(result::setPotion);
                
                firstTypes.stream().forEach(pet -> 
                {
                    EffectOption op = Configs.getEffectOptions(pet);
                    int level = firstCharm.getPotion(pet).get().getLevel();
                    if(level >= op.getMaxLevel())
                    {
                        return;
                    }
                    
                    level += secondCharm.getPotion(pet).get().getLevel() + 1;
                    result.setPotion(new EffectPotion(pet).setLevel(level).setUpgrade(true).setDefault(true));
                });
                
                if(result.getPotions().size() > Configs.maxEffect)
                {
                    e.setResult(new ItemStack(Material.AIR));
                    return firstCharm;
                }
                
                result.getOptionCompound().setSuccessChance(totalChance);
                e.setResult(result.getItem());
                
                return firstCharm;
            }).orElseGet(() -> 
            {
                e.getInventory().setRepairCost(Configs.renameExpCost);
                
                ItemStack renamed = e.getInventory().getItem(0).clone();
                ItemMeta meta = renamed.getItemMeta();
                meta.setDisplayName(Utils.color(e.getInventory().getRenameText()));
                renamed.setItemMeta(meta);
                
                e.setResult(renamed);
                return null;
            });
        });
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onInventoryClick(final InventoryClickEvent e)
    {
        if(e.getInventory().getType() != InventoryType.ANVIL)
        {
            return;
        }
        
        Optional.ofNullable(e.getInventory().getItem(2))
                .filter(item -> e.getInventory().equals(e.getClickedInventory()))
                .filter(item -> e.getSlot() == 2)
                .filter(item -> e.getWhoClicked().getItemOnCursor().getType()==Material.AIR)
                .map(Charm::new)
                .filter(Charm::isValid)
                .ifPresent(charm -> 
                {
                    e.setCancelled(true);
                    
                    OptionCompound option = charm.getOptionCompound();
                    if(!option.hasChance())
                    {
                        AnvilInventory anvil = (AnvilInventory)e.getInventory();
                        int levelCost = anvil.getRepairCost();

                        Player p = (Player)e.getWhoClicked();
                        if(p.getLevel() < levelCost)
                        {
                            return;
                        }

                        p.setLevel(p.getLevel() - levelCost);
                        
                        p.setItemOnCursor(charm.getItem());
                        e.getInventory().setItem(0, new ItemStack(Material.AIR));
                        return;
                    }
                    
                    int chance = option.getChance();
                    option.removeChance();
                    if(ThreadLocalRandom.current().nextInt(101) > chance)
                    {
                        switch(Configs.failType)
                        {
                            case DECREASE:
                                charm.getPotions().forEach(pet -> 
                                {
                                    EffectPotion potion = charm.getPotion(pet).get();
                                    int level = potion.getLevel() - 2;
                                    if(level <= -1)
                                    {
                                        if(!potion.isDefault())
                                        {
                                            charm.removePotion(pet);
                                            return;
                                        }
                                        level = 0;
                                    }
                                    
                                    charm.setPotion(potion.setLevel(level).setDefault(true).setUpgrade(false));
                                });
                                
                                e.getWhoClicked().setItemOnCursor(charm.getItem(chance / 2));
                                break;
                        }
                        
                        Configs.failSound.play(Optional.ofNullable(e.getInventory().getLocation()).orElse(e.getWhoClicked().getLocation()), (Player)e.getWhoClicked());
                    }
                    else
                    {
                        Configs.successSound.play(Optional.ofNullable(e.getInventory().getLocation()).orElse(e.getWhoClicked().getLocation()), (Player)e.getWhoClicked());
                        
                        charm.getPotions().stream()
                                .map(charm::getPotion)
                                .map(Optional::get)
                                .map(potion -> potion.setDefault(true).setUpgrade(false))
                                .forEach(charm::setPotion);
                        
                        e.getWhoClicked().setItemOnCursor(charm.getItem(chance / 2));
                    }
                    
                    e.getInventory().setItem(0, new ItemStack(Material.AIR));
                    Utils.decreaseItemStack(e.getInventory().getItem(1), e.getInventory());
                    e.getInventory().setItem(2, new ItemStack(Material.AIR));
                });
    }
}
