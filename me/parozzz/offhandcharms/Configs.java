/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.offhandcharms;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import me.parozzz.offhandcharms.CharmCommand.CommandEnum;
import me.parozzz.offhandcharms.charms.Charm;
import me.parozzz.offhandcharms.charms.effects.EffectPotion;
import me.parozzz.offhandcharms.utilities.Debug;
import me.parozzz.offhandcharms.utilities.Utils;
import me.parozzz.offhandcharms.utilities.classes.SoundManager;
import me.parozzz.offhandcharms.utilities.classes.SimpleMapList;
import me.parozzz.offhandcharms.utilities.reflection.ParticleManager.ParticleEnum;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author Paros
 */
public class Configs 
{
    public enum FailType
    {
        DESTROY, DEBUFF, DECREASE;
    }
    
    public enum MessageEnum
    {
        PLAYER_OFFLINE, WRONG_CHARM, CHARM_SENT, CHARM_RECEIVED, PLUGIN_RELOADED, EFFECT_MAX_REACHED;
        
        public void send(final CommandSender cs)
        {
            cs.sendMessage(this.toString());
        }
        
        @Override
        public String toString()
        {
            return messages.get(this);
        }
    }
    
    protected final static Map<CommandEnum, String> commandHelp = new EnumMap(CommandEnum.class);
    private final static Map<MessageEnum, String> messages = new EnumMap(MessageEnum.class);
    
    private static Map<PotionEffectType, EffectOption> options;
    private static Map<String, Charm> charms;
    
    public static FailType failType;
    public static List<PotionEffectType> debuffs;
    
    public static String qualityLore;
    public static String chanceLore;
    public static int maxEffect;
    public static int renameExpCost;
    
    public static SoundManager failSound;
    public static SoundManager successSound;
    
    public static int particleAmount;
    public static int particleTimer;
    
    protected static void init()
    {
        charms = new HashMap<>();
        
        FileConfiguration c = JavaPlugin.getProvidingPlugin(Configs.class).getConfig();
        particleTimer = c.getInt("particleTimer");
        particleAmount = c.getInt("particleAmount");
        
        options = new HashMap<>();
        ConfigurationSection eoPath = c.getConfigurationSection("effectOption");
        eoPath.getKeys(false).stream().map(eoPath::getConfigurationSection).forEach(path -> 
        {
            PotionEffectType pet = PotionEffectType.getByName(path.getName().toUpperCase());
            
            String name = Utils.color(path.getString("name", pet.getName()));
            int maxLevel = path.getInt("maxLevel", 1) - 1;
            ParticleEnum particle = Optional.ofNullable(path.getString("particle")).map(s -> Debug.validateEnum(s, ParticleEnum.class)).orElse(null);
            
            options.put(pet, new EffectOption(name, maxLevel, particle));
        });
        
        ConfigurationSection qoPath = c.getConfigurationSection("upgradeOption");
        
        qualityLore = Utils.color(qoPath.getString("qualityLore"));
        chanceLore = Utils.color(qoPath.getString("chanceLore"));
        maxEffect = qoPath.getInt("maxEffect");
        renameExpCost = qoPath.getInt("renameExpCost");
        failSound = new SoundManager(qoPath.getString("failSound"), ",");
        successSound = new SoundManager(qoPath.getString("successSound"), ",");
        
        failType = Debug.validateEnum(qoPath.getString("onFail"), FailType.class);
        if(failType == FailType.DEBUFF)
        {
            debuffs = qoPath.getStringList("debuffs").stream().map(String::toUpperCase).map(PotionEffectType::getByName).collect(Collectors.toList());
        }
        
        ConfigurationSection cPath = c.getConfigurationSection("Charms");
        cPath.getKeys(false).stream().map(cPath::getConfigurationSection).forEach(path -> 
        {
            String id = path.getName();
            
            Charm charm = new Charm(Utils.getItemByPath(path.getConfigurationSection("Item"))).setValid();
            
            charm.getOptionCompound().setUpgradable(path.getBoolean("upgradable", true)).setQuality(0);
            
            new SimpleMapList(path.getMapList("effect"))
                    .getConvertedValues(s -> PotionEffectType.getByName(s.toUpperCase()), Integer::valueOf)
                    .forEach((pet, level) -> charm.setPotion(new EffectPotion(pet).setLevel(level).setUpgrade(false).setDefault(true)));
            
            charms.put(id.toLowerCase(), charm);
        });
        
        c.getConfigurationSection("Messages.CommandHelp").getValues(false)
                .forEach((cmd, o) -> commandHelp.put(Debug.validateEnum(cmd, CommandEnum.class), Utils.color(o.toString())));
         
        c.getConfigurationSection("Messages.General").getValues(false)
                .forEach((msg, o) -> messages.put(Debug.validateEnum(msg, MessageEnum.class), Utils.color(o.toString())));
    }
    
    public static EffectOption getEffectOptions(final PotionEffectType pet)
    {
        return options.get(pet);
    }
    
    public static Set<String> getCharmIds()
    {
        return charms.keySet();
    }
    
    public static Optional<ItemStack> getCharm(final String id)
    {
        return getCharm(id, ThreadLocalRandom.current().nextInt(50));
    }
    
    public static Optional<ItemStack> getCharm(final String id, final int quality)
    {
        return Optional.ofNullable(charms.get(id.toLowerCase())).map(charm -> charm.getClonedItem(quality));
    }
    
    public static class EffectOption
    {
        private final String name;
        private final int maxLevel;
        private final ParticleEnum particle;
        private EffectOption(final String name, final int maxLevel, final ParticleEnum particle)
        {
            this.name=name.replace("{maxLevel}", Objects.toString(maxLevel + 1));
            this.maxLevel=maxLevel;
            this.particle = particle;
        }
        
        
        public String getName()
        {
            return name;
        }
        
        public int getMaxLevel()
        {
            return maxLevel;
        }
        
        public Optional<ParticleEnum> getParticle()
        {
            return Optional.ofNullable(particle);
        }
    }
}
