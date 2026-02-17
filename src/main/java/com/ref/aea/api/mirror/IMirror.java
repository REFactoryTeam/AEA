package com.ref.aea.api.mirror;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeService;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ref.aea.core.localization.AEAToolTips;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;

public interface IMirror<T> extends IGridNodeService {

  String NBT_SOURCE_POS = "sourcePos";

  void setSourcePos(@Nullable SourcePos sourcePos);

  @Nullable
  SourcePos getSourcePos();

  Optional<T> getSource();

  void updateSource(IGridNode node);

  static @NotNull MutableComponent getToolTip(IMirror.SourcePos sourcePos) {
    return Component.translatable(
        AEAToolTips.MirrorInfo.getTranslationKey(),
        Component.literal(String.valueOf(sourcePos.globalPos().pos().getX()))
            .withStyle(Style.EMPTY.withColor(15702682)),
        Component.literal(String.valueOf(sourcePos.globalPos().pos().getY()))
            .withStyle(Style.EMPTY.withColor(10868391)),
        Component.literal(String.valueOf(sourcePos.globalPos().pos().getZ()))
            .withStyle(Style.EMPTY.withColor(9489145)),
        sourcePos.direction(),
        sourcePos.globalPos().dimension().location());
  }

  static void writeSourceToNBT(@NotNull CompoundTag tag, @NotNull SourcePos sourcePos) {
    SourcePos.CODEC
        .encodeStart(NbtOps.INSTANCE, sourcePos)
        .result()
        .ifPresent(nbt -> tag.put(NBT_SOURCE_POS, nbt));
  }

  static Optional<SourcePos> readSourceFromNBT(@NotNull CompoundTag tag) {
    return SourcePos.CODEC.parse(NbtOps.INSTANCE, tag.get(NBT_SOURCE_POS)).result();
  }

  record SourcePos(GlobalPos globalPos, Direction direction) {
    public static final Codec<SourcePos> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        GlobalPos.CODEC.fieldOf("global_pos").forGetter(SourcePos::globalPos),
                        Direction.CODEC.fieldOf("direction").forGetter(SourcePos::direction))
                    .apply(instance, SourcePos::new));
  }
}
