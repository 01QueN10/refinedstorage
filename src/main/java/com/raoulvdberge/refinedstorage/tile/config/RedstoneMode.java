package com.raoulvdberge.refinedstorage.tile.config;

import com.raoulvdberge.refinedstorage.tile.data.TileDataParameter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public enum RedstoneMode {
    IGNORE, HIGH, LOW;

    private static final String NBT = "RedstoneMode";

    public boolean isEnabled(World world, BlockPos pos) {
        switch (this) {
            case IGNORE:
                return true;
            case HIGH:
                return world.isBlockPowered(pos);
            case LOW:
                return !world.isBlockPowered(pos);
            default:
                return false;
        }
    }

    public void write(NBTTagCompound tag) {
        tag.setInteger(NBT, ordinal());
    }

    public static RedstoneMode read(NBTTagCompound tag) {
        if (tag.hasKey(RedstoneMode.NBT)) {
            return getById(tag.getInteger(NBT));
        }

        return IGNORE;
    }

    public static RedstoneMode getById(int id) {
        return id < 0 || id >= values().length ? IGNORE : values()[id];
    }

    public static <T extends TileEntity & IRedstoneConfigurable> TileDataParameter<Integer, T> createParameter() {
        return new TileDataParameter<>(DataSerializers.VARINT, IGNORE.ordinal(), t -> t.getRedstoneMode().ordinal(), (t, v) -> t.setRedstoneMode(RedstoneMode.getById(v)));
    }
}
