/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.offhandcharms;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.parozzz.offhandcharms.Configs.MessageEnum;
import me.parozzz.offhandcharms.utilities.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class CharmCommand implements CommandExecutor
{
    public enum CommandEnum
    {
        RELOAD("charm.command.reload", cs -> true),
        GETCHARM("charm.command.getcharm", Player.class::isInstance),
        GIVECHARM("charm.command.givecharm", cs -> true),
        LIST("charm.command.list", cs -> true);
        
        private final String perm;
        private final Predicate<CommandSender> pred;
        private CommandEnum(final String perm, final Predicate<CommandSender> pred)
        {
            this.perm = perm;
            this.pred = pred;
        }
        
        public void sendHelp(final CommandSender cs)
        {
            if(canUse(cs))
            {
                cs.sendMessage(getHelp());
            }
        }
        
        public boolean canUse(final CommandSender cs)
        {
            return cs.hasPermission(perm) && pred.test(cs);
        }
        
        public String getHelp()
        {
            return Configs.commandHelp.get(this);
        }
    }
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] val) 
    {
        if(val.length == 0)
        {
            Stream.of(CommandEnum.values()).forEach(ce -> ce.sendHelp(cs));
        }
        else
        {
            CommandEnum ce;
            try { ce = CommandEnum.valueOf(val[0].toUpperCase()); }
            catch(final IllegalArgumentException t) 
            {
                return true;
            }
            
            if(!ce.canUse(cs))
            {
                return true;
            }
            
            switch(ce)
            {
                case RELOAD:
                    JavaPlugin.getPlugin(OffHandCharms.class).reload();
                    MessageEnum.PLUGIN_RELOADED.send(cs);
                    break;
                case LIST:
                    cs.sendMessage(Configs.getCharmIds().stream().collect(Collectors.joining(", ")));
                    break;
                case GETCHARM:
                    if(val.length<2)
                    {
                        cs.sendMessage(ce.getHelp());
                        return true;
                    }
                    
                    (val.length == 4 && Utils.isNumber(val[3]) ? Configs.getCharm(val[1], Integer.valueOf(val[3])) : Configs.getCharm(val[1])).map(charm -> 
                    {
                        if(val.length==3 && Utils.isNumber(val[2]))
                        {
                            charm.setAmount(Integer.valueOf(val[2]));
                        }
                        
                        ((Player)cs).getInventory().addItem(charm);
                        cs.sendMessage(MessageEnum.CHARM_RECEIVED.toString().replace("{charm}", val[1]).replace("{amount}", Objects.toString(charm.getAmount())));
                        return charm;
                    }).orElseGet(() -> 
                    {
                        MessageEnum.WRONG_CHARM.send(cs);
                        return null;
                    });
                    break;
                case GIVECHARM:
                    if(val.length<3)
                    {
                        cs.sendMessage(ce.getHelp());
                        return true;
                    }
                    
                    Optional.ofNullable(Bukkit.getPlayer(val[1])).map(p -> 
                    {
                        (val.length == 5 && Utils.isNumber(val[4]) ? Configs.getCharm(val[2], Integer.valueOf(val[4])) : Configs.getCharm(val[2])).map(charm -> 
                        {
                            if(val.length==4 && Utils.isNumber(val[3]))
                            {
                                charm.setAmount(Integer.valueOf(val[3]));
                            }
                            
                            p.getInventory().addItem(charm);
                            p.sendMessage(MessageEnum.CHARM_RECEIVED.toString().replace("{charm}", val[2]).replace("{amount}", Objects.toString(charm.getAmount())));
                            
                            cs.sendMessage(MessageEnum.CHARM_SENT.toString().replace("{charm}", val[2]).replace("{amount}", Objects.toString(charm.getAmount())).replace("{player}", p.getName()));
                            return charm;
                        }).orElseGet(() -> 
                        {
                            MessageEnum.WRONG_CHARM.send(cs);
                            return null;
                        });
                        return p;
                    }).orElseGet(() -> 
                    {
                        MessageEnum.PLAYER_OFFLINE.send(cs);
                        return null;
                    });
            }
        }
        return true;
    }
    
}
