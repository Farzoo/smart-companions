package farzo.plugins.world.entities;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class Dwarf extends Player {

    public Dwarf(Level world, BlockPos blockposition, float f, GameProfile gameprofile, @Nullable ProfilePublicKey profilepublickey) {
        super(world, blockposition, f, gameprofile, profilepublickey);
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }
}
