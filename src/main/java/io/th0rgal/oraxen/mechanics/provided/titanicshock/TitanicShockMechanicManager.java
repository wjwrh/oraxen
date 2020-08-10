package io.th0rgal.oraxen.mechanics.provided.titanicshock;

import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.items.OraxenItems;
import io.th0rgal.oraxen.mechanics.MechanicFactory;
import io.th0rgal.oraxen.settings.Message;
import io.th0rgal.oraxen.utils.timers.Timer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TitanicShockMechanicManager implements Listener{

    private final MechanicFactory factory;
    private Map<FallingBlock, Block> allFallingBlocks = new ConcurrentHashMap<>();


    public TitanicShockMechanicManager(MechanicFactory factory) {
        this.factory = factory;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Map<FallingBlock, Block> fallingBlocks = new ConcurrentHashMap<>();

        if (!(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) || event.getClickedBlock() == null)
            return;

        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (item == null)
            return;

        String itemID = OraxenItems.getIdByItem(item);

        if (factory.isNotImplementedIn(itemID))
            return;

        TitanicShockMechanic mechanic = (TitanicShockMechanic) factory.getMechanic(itemID);



        Player player = event.getPlayer();

        Timer playerTimer = mechanic.getTimer(player);

        if (!playerTimer.isFinished()) {
            Message.DELAY.send(player, Timer.DECIMAL_FORMAT.format(playerTimer.getRemainingTimeMillis() / 1000D));
            return;
        }

        playerTimer.reset();



        for (Block block : getCircleBlocks(event.getClickedBlock().getLocation(), mechanic.getRadius(), mechanic.getDepth())) {
            if (block.getBlockData().getMaterial() != Material.AIR && block.getBlockData().getMaterial() != Material.BEDROCK && block.getBlockData().getMaterial() != Material.CAVE_AIR && block.getBlockData().getMaterial() != Material.VOID_AIR) {
                FallingBlock fallingBlock = block.getLocation().getWorld().spawnFallingBlock(getCenter(block.getLocation()), block.getBlockData());
                fallingBlocks.put(fallingBlock, block);
                fallingBlock.setHurtEntities(true);
                fallingBlock.setGravity(false);
                fallingBlock.setDropItem(false);
                new BukkitRunnable(){

                    @Override
                    public void run() {
                        try {
                            Thread.sleep(new Random().nextInt(500));
                        } catch (InterruptedException ignored) {
                        }
                        fallingBlock.setVelocity(fallingBlock.getVelocity().setY(0.7));
                        fallingBlock.setGravity(true);
                    }
                }.runTaskAsynchronously(OraxenPlugin.get());
                block.setType(Material.AIR);
                block.getState().update();
            }
        }

        allFallingBlocks.putAll(fallingBlocks);

        new BukkitRunnable() {
            @Override
            public void run() {
                List<FallingBlock> reachedTop = new ArrayList<>();
                while (true) {
                    for(int i = 0;i < 20;i++) {
                            for (Map.Entry<FallingBlock, Block> block : fallingBlocks.entrySet()) {
                                if (block.getKey().getLocation().getY() - block.getValue().getLocation().getY() >= mechanic.getHeight()) {
                                    block.getKey().setVelocity(block.getKey().getVelocity().setY(-0.7));
                                    reachedTop.add(block.getKey());
                                } else if (!reachedTop.contains(block.getKey()))
                                    block.getKey().setVelocity(block.getKey().getVelocity().setY(0.7));
                                if (block.getKey().getLocation().getY() <= block.getValue().getLocation().getY() || block.getKey().isDead() || block.getKey().getTicksLived() > 400) {
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            block.getValue().setBlockData(block.getKey().getBlockData());
                                            block.getValue().getState().update();
                                            fallingBlocks.remove(block.getKey());
                                            allFallingBlocks.remove(block.getKey());
                                            block.getKey().remove();
                                        }
                                    }.runTask(OraxenPlugin.get());
                                }
                                if (fallingBlocks.isEmpty())
                                    this.cancel();
                            }

                    }

                }
            }
        }.runTaskAsynchronously(OraxenPlugin.get());

    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if(event.getEntity() instanceof FallingBlock && allFallingBlocks.containsKey(event.getEntity())){
            event.setCancelled(true);
            event.getEntity().remove();
        }
    }


    private Location getCenter(Location loc) {
        return new Location(loc.getWorld(),
                getRelativeCoord(loc.getBlockX()),
                loc.getBlockY(),
                getRelativeCoord(loc.getBlockZ()));
    }

    private double getRelativeCoord(int i) {
        double d = i;
        d = d < 0 ? d - .5 : d + .5;
        return d;
    }


    private List<Block> getCircleBlocks(Location loc, int radius, int depth) {
        List<Block> blocks = new ArrayList<>();

        for (int x = loc.getBlockX() - radius; x <= loc.getBlockX() + radius; x++) {
            for (int y = loc.getBlockY() - depth; y <= loc.getBlockY(); y++) {
                for (int z = loc.getBlockZ() - radius; z <= loc .getBlockZ() + radius; z++) {
                    double distance = ((loc.getBlockX() - x) * (loc.getBlockX() - x) + (loc.getBlockY() - y) * (loc.getBlockY() - y) + (loc.getBlockZ() - z) * (loc.getBlockZ() - z));
                    if (distance < radius * radius)
                        blocks.add(new Location(loc.getWorld(), x, y, z).getBlock());
                }

            }
        }
        return blocks;
    }


}
