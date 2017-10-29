/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.offhandcharms.charms;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import me.parozzz.offhandcharms.Configs;
import me.parozzz.offhandcharms.Configs.EffectOption;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author Paros
 */
public class CharmUtils 
{
    /*
    private static final String LORE = "Lore";
    private static final String AMOUNT = "Amount";
    
    public static Charm joinCharms(final ItemStack result, final PotionCompound first, final PotionCompound second)
    {
        Map<PotionEffectType, BasePotion> firstMap = first.getEffectTypes().stream().map(first::getPotion).collect(Collectors.toMap(BasePotion::getType, Function.identity()));
        
        if(firstMap.values().stream().allMatch(base -> Configs.getEffectOptions(base.getType()).getMaxLevel() == base.getLevel()))
        {
            return null;
        }
        
        Map<PotionEffectType, BasePotion> secondMap = second.getEffectTypes().stream().map(first::getPotion).collect(Collectors.toMap(BasePotion::getType, Function.identity()));
        
        if(secondMap.values().stream().allMatch(base -> Configs.getEffectOptions(base.getType()).getMaxLevel() == base.getLevel()))
        {
            return null;
        }
        
        Set<PotionEffectType> sameEffect = firstMap.keySet().stream().filter(secondMap::containsKey).collect(Collectors.toSet());
        
        if(firstMap.size() + secondMap.size() - sameEffect.size() > Configs.maxEffect)
        {
            return null;
        }
        
        Charm charm = new Charm(result);
        sameEffect.forEach(pet -> 
        {
            firstMap.compute(pet, (type, base) -> 
            {
                int amplifier = base.getLevel() + secondMap.remove(type).getLevel() + 1;
                
                EffectOption op = Configs.getEffectOptions(pet);
                return amplifier <= op.getMaxLevel() ? amplifier : op.getMaxLevel();
            });
        });
        
        firstMap.putAll(secondMap);
        return firstMap;
    }*/
}
