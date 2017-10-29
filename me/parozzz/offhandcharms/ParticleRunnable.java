/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.offhandcharms;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import me.parozzz.offhandcharms.utilities.reflection.API;
import me.parozzz.offhandcharms.utilities.reflection.ParticleManager.ParticleEffect;
import me.parozzz.offhandcharms.utilities.reflection.ParticleManager.ParticleEnum;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Paros
 */
public class ParticleRunnable extends BukkitRunnable
{
    private static ParticleRunnable instance;
    public static ParticleRunnable getInstance()
    {
        return Optional.ofNullable(instance).orElseGet(() -> instance = new ParticleRunnable());
    }
    
    private final Map<Player, Particles> map;
    private ParticleRunnable()
    {
        map = new HashMap<>();
    }
    
    public void addPlayerParticle(final Player p, final ParticleEnum particle)
    {
        Optional.ofNullable(map.get(p)).orElseGet(() -> 
        {
            Particles part = new Particles();
            map.put(p, part);
            return part;
        }).addEffect(particle);
    }
    
    public void removeParticle(final Player p, final ParticleEnum particle)
    {
        Optional.ofNullable(map.get(p)).ifPresent(part -> 
        {
            part.removeEffect(particle);
            if(part.isEmpty())
            {
                map.remove(p, part);
            }
        });
    }
    
    public void purgePlayer(final Player p)
    {
        map.remove(p);
    }
    
    @Override
    public void run() 
    {
        map.forEach((p, particles) -> particles.spawnAll(p));
    }
    
    
    private class Particles
    {
        private final Set<ParticleEnum> set;
        private Particles()
        {
            set = EnumSet.noneOf(ParticleEnum.class);
        }
        
        public void addEffect(final ParticleEnum particle)
        {
            set.add(particle);
        }
        
        public void removeEffect(final ParticleEnum particle)
        {
            set.remove(particle);
        }
        
        public boolean isEmpty()
        {
            return set.isEmpty();
        }
        
        public void spawnAll(final Player p)
        {
            set.forEach(particle -> API.getParticleManager().spawn(p.getWorld().getPlayers(), ParticleEffect.CLOUD, particle, p.getLocation(), 1));
        }
    }
}
