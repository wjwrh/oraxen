package io.th0rgal.oraxen.compatibilities.provided.mines;

import com.asylumdevs.mines.Mines;
import com.asylumdevs.mines.events.MinePreResetEvent;
import com.asylumdevs.mines.mine.Mine;
import com.asylumdevs.mines.utils.FileSystem;
import io.th0rgal.oraxen.compatibilities.CompatibilityProvider;
import io.th0rgal.oraxen.mechanics.provided.block.BlockMechanic;
import io.th0rgal.oraxen.mechanics.provided.block.BlockMechanicFactory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;

public class MinesCompatibility extends CompatibilityProvider<Mines> {


    @Override
    public void enable(String pluginName) {
        super.enable(pluginName);
        solveMines();
    }

    @EventHandler
    public void onMinePreReset(MinePreResetEvent event) {
        solveMines();
    }

    @SuppressWarnings("deprecation")
    private void solveMines() {
        for (Mine mine : Mines.getAPI().getMines()) {
            FileSystem file = mine.getFile();
            file.load();
            for (String line : file.getStringList("mine.oraxen-blocks")) {
                //MultipleFacing blockData = Bukkit.createBlockData(Material.MUSHROOM_STEM)
                String[] split = line.split(";");
                if (BlockMechanicFactory.get().isNotImplementedIn(split[0]))
                    continue;
                org.bukkit.material.MaterialData materialData = Bukkit.getUnsafe().toLegacy(Material.MUSHROOM_STEM).getNewData((byte) 0);
                //MaterialData materialData = new MaterialData(Material.MUSHROOM_STEM);
                int customVariation = ((BlockMechanic) BlockMechanicFactory.get().getMechanic(split[0])).getCustomVariation();
                materialData.setData((byte) customVariation);
                //Utils.setBlockFacing(blockData, customVariation);

                mine.getBlocks().put(materialData, Double.parseDouble(split[1]));
            }
        }
    }

}
