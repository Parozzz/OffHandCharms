/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.offhandcharms.listeners;

import java.util.Optional;
import me.parozzz.offhandcharms.Configs;
import me.parozzz.offhandcharms.ParticleRunnable;
import me.parozzz.offhandcharms.charms.Charm;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Paros
 */
public class OffHandListener implements Listener
{
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onItemConsume(final PlayerItemConsumeEvent e)
    {
        Optional.ofNullable(e.getItem()).map(Charm::new).filter(Charm::isValid).ifPresent(charm -> e.setCancelled(true));
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onBlockPlace(final BlockPlaceEvent e)
    {
        Optional.ofNullable(e.getItemInHand()).map(Charm::new).filter(Charm::isValid).ifPresent(charm -> e.setCancelled(true));
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onSwap(final PlayerSwapHandItemsEvent e)
    {
        Optional.ofNullable(e.getMainHandItem())
                .filter(item -> item.getType()!=Material.AIR)
                .ifPresent(item -> removeCharm(e.getPlayer(), item));
        
        Optional.ofNullable(e.getOffHandItem())
                .filter(item -> item.getType()!=Material.AIR)
                .ifPresent(item -> addCharm(e.getPlayer(), item));
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onInventoryDrag(final InventoryDragEvent e)
    {
        Optional.ofNullable(e.getNewItems().get(45)).filter(item -> item.getType()==Material.AIR).ifPresent(item -> addCharm(e.getWhoClicked(), item));
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onInventoryClick(final InventoryClickEvent e)
    {
        switch(e.getInventory().getType())
        {
            case CRAFTING:
                if(e.getWhoClicked().getGameMode() != GameMode.CREATIVE)
                {
                    this.onSurvivalClick(e);
                }
                else
                {
                    this.onCreativeClick(e);
                }
                break;
        }
    }
    
    private void onSurvivalClick(final InventoryClickEvent e)
    {
        if(e.getInventory().getType() != InventoryType.CRAFTING)
        {
            return;
        }
        
        switch(e.getSlotType())
        {
            case QUICKBAR:
                if(e.getSlot() != 40)
                {
                    return;
                }
                
                switch(e.getAction())
                {
                    case HOTBAR_SWAP:
                        new BukkitRunnable()
                        {
                            @Override
                            public void run() 
                            {
                                Optional.ofNullable(e.getWhoClicked().getInventory().getItemInOffHand()).ifPresent(item -> addCharm(e.getWhoClicked(), item));
                            }
                        }.runTaskLater(JavaPlugin.getProvidingPlugin(OffHandListener.class), 1L);
                        removeCharm(e.getWhoClicked(), e.getCurrentItem());
                        break;
                    case SWAP_WITH_CURSOR:
                        this.removeCharm(e.getWhoClicked(), e.getCurrentItem());
                    case PLACE_ALL:
                    case PLACE_ONE:
                        this.addCharm(e.getWhoClicked(), e.getCursor());
                        break;
                    case PICKUP_ONE:
                    case PICKUP_HALF:
                    case DROP_ONE_SLOT:
                        if(e.getCurrentItem().getAmount() == 1)
                        {
                            this.removeCharm(e.getWhoClicked(), e.getCurrentItem());
                        }
                        break;
                    case PICKUP_ALL:
                    case MOVE_TO_OTHER_INVENTORY:
                    case DROP_ALL_SLOT:
                        removeCharm(e.getWhoClicked(), e.getCurrentItem());
                        break;
                }
                break;
            case CONTAINER:
                if(e.getClick() == ClickType.DOUBLE_CLICK && e.getCursor().isSimilar(e.getWhoClicked().getInventory().getItemInOffHand()))
                {
                    Charm charm = new Charm(e.getCursor());
                    if(!charm.isValid())
                    {
                        return;
                    }

                    new BukkitRunnable()
                    {
                        @Override
                        public void run() 
                        {
                            if(e.getWhoClicked().getInventory().getItemInOffHand().getType() == Material.AIR)
                            {
                                removeCharm(e.getWhoClicked(), charm);
                            }
                        }  
                    }.runTaskLater(JavaPlugin.getProvidingPlugin(OffHandListener.class), 1L);
                }
                break;
        } 
    }
    
    private void onCreativeClick(final InventoryClickEvent e)
    {
        //To - DO
    }
    
    private void addCharm(final HumanEntity p, final ItemStack item)
    {
        Optional.ofNullable(item).filter(temp -> temp.getType()!=Material.AIR)
                .map(Charm::new).filter(Charm::isValid)
                .ifPresent(charm -> 
                {
                    charm.getPotions().stream().map(charm::getPotion).map(Optional::get).forEach(potion -> 
                    {
                        p.addPotionEffect(potion.getPotionEffect(), true);
                        Configs.getEffectOptions(potion.getType()).getParticle().ifPresent(peEnum -> ParticleRunnable.getInstance().addPlayerParticle((Player)p, peEnum));
                    });
                });
    }
    
    private void removeCharm(final HumanEntity p, final ItemStack item)
    {
        Optional.ofNullable(item).filter(temp -> temp.getType()!=Material.AIR)
                .map(Charm::new)
                .filter(Charm::isValid)
                .ifPresent(charm -> removeCharm(p, charm));
    }

    private void removeCharm(final HumanEntity p, final Charm charm)
    {
        charm.getPotions().forEach(pet -> 
        {
            p.removePotionEffect(pet);
            Configs.getEffectOptions(pet).getParticle().ifPresent(peEnum -> ParticleRunnable.getInstance().removeParticle((Player)p, peEnum));
        });
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onPlayerQuit(final PlayerQuitEvent e)
    {
        ParticleRunnable.getInstance().purgePlayer(e.getPlayer());
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onPlayerJoin(final PlayerJoinEvent e)
    {
        Optional.ofNullable(e.getPlayer().getInventory().getItemInOffHand())
                .filter(item -> item.getType()!=Material.AIR)
                .ifPresent(item -> addCharm(e.getPlayer(), item));
    }
}
