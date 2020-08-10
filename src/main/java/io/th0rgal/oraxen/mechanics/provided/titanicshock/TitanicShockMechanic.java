package io.th0rgal.oraxen.mechanics.provided.titanicshock;

import io.th0rgal.oraxen.mechanics.Mechanic;
import io.th0rgal.oraxen.mechanics.MechanicFactory;
import io.th0rgal.oraxen.utils.timers.Timer;
import io.th0rgal.oraxen.utils.timers.TimersFactory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class TitanicShockMechanic extends Mechanic {

    private final int radius;
    private final int height;
    private final int depth;
    private final TimersFactory timersFactory;

    public TitanicShockMechanic(MechanicFactory mechanicFactory, ConfigurationSection section) {
        super(mechanicFactory, section);
        this.radius = section.getInt("radius");
        this.height = section.getInt("height");
        this.depth = section.getInt("depth");
        this.timersFactory = new TimersFactory(section.getLong("delay"));
    }

    public int getRadius() {
        return radius;
    }

    public int getHeight() {
        return height;
    }

    public int getDepth() {
        return depth;
    }

    public Timer getTimer(Player player) {
        return timersFactory.getTimer(player);
    }
}
