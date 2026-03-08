package com.ref.aea.api.pos;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ref.aea.core.localization.AEAToolTips;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public record SidedGlobalPos(GlobalPos globalPos, Optional<Direction> direction) {

  public static final String NBT_SIDED_GLOBAL_POS = "sided_global_pos";

  public static final String NBT_SIDED_GLOBAL_POS_LIST = "sided_global_pos_list";

  public static final Codec<SidedGlobalPos> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      GlobalPos.CODEC.fieldOf("global_pos").forGetter(SidedGlobalPos::globalPos),
                      Direction.CODEC
                          .optionalFieldOf("direction")
                          .forGetter(SidedGlobalPos::direction))
                  .apply(instance, SidedGlobalPos::new));

  public static final Codec<List<SidedGlobalPos>> LIST_CODEC = CODEC.listOf();

  public static SidedGlobalPos of(GlobalPos pos, @Nullable Direction side) {
    return new SidedGlobalPos(pos, Optional.ofNullable(side));
  }

  public CompoundTag toNbt(@NotNull CompoundTag tag) {
    CODEC
        .encodeStart(NbtOps.INSTANCE, this)
        .result()
        .ifPresent(nbt -> tag.put(NBT_SIDED_GLOBAL_POS, nbt));
    return tag;
  }

  public CompoundTag toNbt() {
    return this.toNbt(new CompoundTag());
  }

  public static Optional<SidedGlobalPos> fromNbt(CompoundTag tag) {
    if (tag != null && tag.contains(NBT_SIDED_GLOBAL_POS, Tag.TAG_COMPOUND)) {
      return CODEC.parse(NbtOps.INSTANCE, tag.getCompound(NBT_SIDED_GLOBAL_POS)).result();
    }
    return Optional.empty();
  }

  public static boolean removeNbt(CompoundTag tag) {
    if (tag != null && tag.contains(NBT_SIDED_GLOBAL_POS, Tag.TAG_COMPOUND)) {
      tag.remove(SidedGlobalPos.NBT_SIDED_GLOBAL_POS);
      return true;
    }
    return false;
  }

  public static CompoundTag listToNbt(
      @NotNull CompoundTag tag, Collection<SidedGlobalPos> positions) {
    LIST_CODEC
        .encodeStart(NbtOps.INSTANCE, List.copyOf(positions))
        .result()
        .ifPresent(nbt -> tag.put(NBT_SIDED_GLOBAL_POS_LIST, nbt));
    return tag;
  }

  public static CompoundTag listToNbt(Collection<SidedGlobalPos> positions) {
    return listToNbt(new CompoundTag(), positions);
  }

  public static List<SidedGlobalPos> listFromNbt(CompoundTag tag) {
    if (tag != null && tag.contains(NBT_SIDED_GLOBAL_POS_LIST, Tag.TAG_LIST)) {
      return LIST_CODEC
          .parse(NbtOps.INSTANCE, tag.get(NBT_SIDED_GLOBAL_POS_LIST))
          .result()
          .orElse(List.of());
    }
    return List.of();
  }

  public static boolean removeListNbt(CompoundTag tag) {
    if (tag != null && tag.contains(NBT_SIDED_GLOBAL_POS_LIST, Tag.TAG_LIST)) {
      tag.remove(SidedGlobalPos.NBT_SIDED_GLOBAL_POS_LIST);
      return true;
    }
    return false;
  }

  public @NotNull MutableComponent getToolTip() {
    return Component.translatable(
        AEAToolTips.MirrorInfo.getTranslationKey(),
        Component.literal(String.valueOf(this.globalPos().pos().getX()))
            .withStyle(Style.EMPTY.withColor(15702682)),
        Component.literal(String.valueOf(this.globalPos().pos().getY()))
            .withStyle(Style.EMPTY.withColor(10868391)),
        Component.literal(String.valueOf(this.globalPos().pos().getZ()))
            .withStyle(Style.EMPTY.withColor(9489145)),
        this.direction().orElse(null),
        this.globalPos().dimension().location());
  }

  /**
   * Checks if this SidedGlobalPos matches another.
   *
   * <p>Matching logic: 1. {@link GlobalPos} (dimension and block position) must be identical. 2.
   * Direction matching: - If this object's {@link #direction()} is empty ({@link
   * Optional#empty()}), it acts as a wildcard, matching any direction (or lack thereof) of the
   * other object. - If this object has a specific direction, the other object's direction must be
   * exactly the same.
   *
   * @param other The other {@link SidedGlobalPos} to compare against.
   * @return {@code true} if the positions match according to the logic, {@code false} otherwise.
   */
  public boolean is(@NotNull SidedGlobalPos other) {
    if (!this.globalPos.equals(other.globalPos())) {
      return false;
    }
    if (this.direction.isEmpty()) {
      return true;
    }
    return this.direction.equals(other.direction());
  }

  public Vec3 getVec() {
    Vec3 center = Vec3.atCenterOf(this.globalPos().pos());
    return this.direction().map(dir -> center.add(new Vec3(dir.step()).scale(0.5))).orElse(center);
  }

  @OnlyIn(Dist.CLIENT)
  public boolean isCurrentLevel() {
    ClientLevel level = Minecraft.getInstance().level;
    if (level != null) {
      return level.dimension().location().equals(this.globalPos().dimension().location());
    } else {
      return false;
    }
  }
}
