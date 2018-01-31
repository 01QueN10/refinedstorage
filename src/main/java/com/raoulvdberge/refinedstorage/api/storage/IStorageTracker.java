package com.raoulvdberge.refinedstorage.api.storage;

import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nullable;

/**
 * Keeps track of when a stack is changed in the system.
 */
public interface IStorageTracker<T> {
    interface IStorageTrackerEntry {
        /**
         * @return the modification time
         */
        long getTime();

        /**
         * @return the name of the player who last modified the stack
         */
        String getName();
    }

    /**
     * Updates the storage tracker entry for a stack, changing it's player name and modification time.
     *
     * @param player player
     * @param stack  the stack
     */
    void changed(EntityPlayer player, T stack);

    /**
     * @param stack the stack
     * @return the entry, or null if the stack hasn't been modified yet
     */
    @Nullable
    IStorageTrackerEntry get(T stack);
}
