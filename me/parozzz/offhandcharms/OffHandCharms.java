/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.offhandcharms;

import me.parozzz.offhandcharms.listeners.AnvilListener;
import me.parozzz.offhandcharms.listeners.OffHandListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class OffHandCharms extends JavaPlugin
{

    @Override
    public void onEnable()
    {
        this.saveDefaultConfig();
        this.reloadConfig();
        
        if(this.getConfig().getBoolean("metric", true))
        {
            new MetricsLite(this);
        }
        
        Configs.init();
        if(Configs.particleTimer > 0)
        {
            ParticleRunnable.getInstance().runTaskTimer(this, Configs.particleTimer, Configs.particleTimer);
        }
        
        this.getCommand("charm").setExecutor(new CharmCommand());
        
        Bukkit.getPluginManager().registerEvents(new OffHandListener(), this);
        Bukkit.getPluginManager().registerEvents(new AnvilListener(), this);
    }
    
    public void reload()
    {
        this.reloadConfig();
        Configs.init();
    }
    
    @Override
    public void onDisable()
    {
        
    }
    
}
