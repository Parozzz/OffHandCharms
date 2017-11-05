/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.offhandcharms.api;

import java.util.Optional;
import me.parozzz.offhandcharms.Configs;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class CharmAPI 
{
    /**
     * Get the ItemStack from the charm id
     * @param id - The charm name from the config file
     * @return The charm ItemStack
     */
    public static Optional<ItemStack> getCharm(final String id)
    {
        return Configs.getCharm(id);
    }
}
