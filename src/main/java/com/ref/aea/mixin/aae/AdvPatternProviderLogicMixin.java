package com.ref.aea.mixin.aae;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.stacks.AEKey;
import com.ref.aea.api.mirror.IMirrorPatternService;
import com.ref.aea.api.mixin.ae.crafting.mirror.IMixinAdvPatternProviderLogic;
import com.ref.aea.api.mixin.ae.crafting.mirror.IMixinPatternProviderLogic;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AdvPatternProviderLogic.class, remap = false)
public class AdvPatternProviderLogicMixin
    implements IMixinPatternProviderLogic, IMixinAdvPatternProviderLogic {

  @Shadow @Final private List<IPatternDetails> patterns;

  @Shadow @Final private Set<AEKey> patternInputs;

  @Shadow @Final private IManagedGridNode mainNode;

  @Shadow @Final private HashSet<AEKey> outputCache;

  @Override
  public IManagedGridNode AEA$getMainNode() {
    return this.mainNode;
  }

  @Override
  public void AEA$setPatterns(List<IPatternDetails> patterns) {
    this.patterns.clear();
    this.patterns.addAll(patterns);
  }

  @Override
  public List<IPatternDetails> AEA$getPatterns() {
    return this.patterns;
  }

  @Override
  public void AEA$setPatternInputs(Set<AEKey> patternInputs) {
    this.patternInputs.clear();
    this.patternInputs.addAll(patternInputs);
  }

  @Override
  public Set<AEKey> AEA$getPatternInputs() {
    return this.patternInputs;
  }

  @Inject(method = "updatePatterns", at = @At("RETURN"))
  private void AEA$onUpdatePatternsTail(CallbackInfo ci) {
    IGridNode node = this.mainNode.getNode();
    if (node != null) {
      node.getGrid().getService(IMirrorPatternService.class).refreshNode(node);
    }
  }

  @Override
  public void AAE$setOutputCache(HashSet<AEKey> set) {
    this.outputCache.clear();
    this.outputCache.addAll(set);
  }

  @Override
  public HashSet<AEKey> AAE$getOutputCache() {
    return this.outputCache;
  }
}
