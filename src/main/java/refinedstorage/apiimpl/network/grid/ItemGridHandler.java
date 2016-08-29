package refinedstorage.apiimpl.network.grid;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import refinedstorage.RefinedStorage;
import refinedstorage.api.autocrafting.ICraftingPattern;
import refinedstorage.api.autocrafting.task.ICraftingTask;
import refinedstorage.api.network.INetworkMaster;
import refinedstorage.api.network.NetworkUtils;
import refinedstorage.api.network.grid.IItemGridHandler;
import refinedstorage.api.storage.CompareUtils;

public class ItemGridHandler implements IItemGridHandler {
    public static final int MAX_CRAFTING_PER_REQUEST = 500;

    private INetworkMaster network;

    public ItemGridHandler(INetworkMaster network) {
        this.network = network;
    }

    @Override
    public void onExtract(int hash, int flags, EntityPlayerMP player) {
        ItemStack item = network.getItemStorage().get(hash);

        if (item == null) {
            return;
        }

        int itemSize = item.stackSize;

        boolean single = (flags & EXTRACT_SINGLE) == EXTRACT_SINGLE;

        ItemStack held = player.inventory.getItemStack();

        if (single) {
            if (held != null && (!CompareUtils.compareStackNoQuantity(item, held) || held.stackSize + 1 > held.getMaxStackSize())) {
                return;
            }
        } else if (player.inventory.getItemStack() != null) {
            return;
        }

        int size = 64;

        if ((flags & EXTRACT_HALF) == EXTRACT_HALF && itemSize > 1) {
            size = itemSize / 2;

            if (size > 32) {
                size = 32;
            }
        } else if (single) {
            size = 1;
        } else if ((flags & EXTRACT_SHIFT) == EXTRACT_SHIFT) {
            // NO OP, the quantity already set (64) is needed for shift
        }

        size = Math.min(size, item.getItem().getItemStackLimit(item));

        ItemStack took = NetworkUtils.extractItem(network, item, size);

        if (took != null) {
            if ((flags & EXTRACT_SHIFT) == EXTRACT_SHIFT) {
                if (!player.inventory.addItemStackToInventory(took.copy())) {
                    InventoryHelper.spawnItemStack(player.worldObj, player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ(), took);
                }
            } else {
                if (single && held != null) {
                    held.stackSize++;
                } else {
                    player.inventory.setItemStack(took);
                }

                player.updateHeldItem();
            }

            network.getWirelessGridHandler().drainEnergy(player, RefinedStorage.INSTANCE.wirelessGridExtractUsage);
        }
    }

    @Override
    public ItemStack onInsert(EntityPlayerMP player, ItemStack stack) {
        ItemStack remainder = network.insertItem(stack, stack.stackSize, false);

        network.getWirelessGridHandler().drainEnergy(player, RefinedStorage.INSTANCE.wirelessGridInsertUsage);

        return remainder;
    }

    @Override
    public void onInsertHeldItem(EntityPlayerMP player, boolean single) {
        if (player.inventory.getItemStack() == null) {
            return;
        }

        ItemStack stack = player.inventory.getItemStack();
        int size = single ? 1 : stack.stackSize;

        if (single) {
            if (network.insertItem(stack, size, true) == null) {
                network.insertItem(stack, size, false);

                stack.stackSize -= size;

                if (stack.stackSize == 0) {
                    player.inventory.setItemStack(null);
                }
            }
        } else {
            player.inventory.setItemStack(network.insertItem(stack, size, false));
        }

        player.updateHeldItem();

        network.getWirelessGridHandler().drainEnergy(player, RefinedStorage.INSTANCE.wirelessGridInsertUsage);
    }

    @Override
    public void onCraftingRequested(int hash, int quantity) {
        if (quantity <= 0 || quantity > MAX_CRAFTING_PER_REQUEST) {
            return;
        }

        ItemStack stack = network.getItemStorage().get(hash);

        if (stack == null) {
            return;
        }

        ICraftingPattern pattern = NetworkUtils.getPattern(network, stack);

        if (pattern != null) {
            int quantityPerRequest = pattern.getQuantityPerRequest(stack);

            while (quantity > 0) {
                network.addCraftingTask(network.createCraftingTask(pattern));

                quantity -= quantityPerRequest;
            }
        }
    }

    @Override
    public void onCraftingCancelRequested(int id) {
        if (id >= 0 && id < network.getCraftingTasks().size()) {
            network.cancelCraftingTask(network.getCraftingTasks().get(id));
        } else if (id == -1) {
            for (ICraftingTask task : network.getCraftingTasks()) {
                network.cancelCraftingTask(task);
            }
        }
    }
}
