package refinedstorage.api.autocrafting.registry;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import refinedstorage.api.autocrafting.ICraftingPattern;
import refinedstorage.api.autocrafting.task.ICraftingTask;
import refinedstorage.api.network.INetworkMaster;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A factory that creates a crafting task.
 * Register this factory in the {@link ICraftingTaskRegistry}.
 */
public interface ICraftingTaskFactory {
    /**
     * Returns a crafting task for a given NBT tag and pattern.
     *
     * @param world    the world
     * @param network  the network
     * @param pattern  the pattern
     * @param quantity the quantity
     * @param tag      the NBT tag, if this is null it isn't reading from disk but is used for making a task on demand
     * @return the crafting task
     */
    @Nonnull
    ICraftingTask create(World world, INetworkMaster network, ICraftingPattern pattern, int quantity, @Nullable NBTTagCompound tag);
}
