package com.ref.aea.mixin.ae.mirror;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.stacks.AEKey;
import appeng.helpers.patternprovider.PatternProviderLogic;
import com.ref.aea.api.mirror.IMirrorPatternService;
import com.ref.aea.api.mixin.ae.crafting.mirror.IMixinPatternProviderLogic;
import java.util.List;
import java.util.Set;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PatternProviderLogic.class, remap = false)
public abstract class PatternProviderLogicMixin implements IMixinPatternProviderLogic {

  @Shadow @Final private List<IPatternDetails> patterns;

  @Shadow @Final private Set<AEKey> patternInputs;

  @Shadow @Final private IManagedGridNode mainNode;

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
}
