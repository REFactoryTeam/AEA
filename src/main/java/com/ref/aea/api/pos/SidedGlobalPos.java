package com.ref.aea.api.pos;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ref.aea.core.localization.AEAToolTips;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
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

  public static void listToNbt(CompoundTag tag, List<SidedGlobalPos> positions) {
    LIST_CODEC
        .encodeStart(NbtOps.INSTANCE, positions)
        .result()
        .ifPresent(nbt -> tag.put(NBT_SIDED_GLOBAL_POS_LIST, nbt));
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
      tag.remove(SidedGlobalPos.NBT_SIDED_GLOBAL_POS);
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
}
