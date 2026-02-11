package com.ref.aea.core.modifier;

import appeng.api.stacks.GenericStack;
import com.ref.aea.AEA;
import com.ref.aea.api.modifier.PatternEncodingModifier;
import com.ref.aea.config.AEAClientConfig;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum GlobalModifier implements PatternEncodingModifier<ResourceLocation> {
  INSTANCE;

  @Override
  public Optional<ResourceLocation> getApplicableContext(
      ModificationResult modificationResult, ModificationContext modificationContext) {
    Object recipeBase = modificationContext.recipeBase();

    if (AEAClientConfig.debugLog) {
      AEA.LOGGER.debug("Recipe class: {}", recipeBase.getClass().getName());
    }

    if (recipeBase instanceof Recipe<?> recipe) {
      if (AEAClientConfig.debugLog) {
        AEA.LOGGER.debug("Recipe id: {}", recipe.getId());
      }
      return Optional.of(recipe.getId());
    }
    return Optional.empty();
  }

  @Override
  public ModificationResult apply(
      ModificationResult modificationResult,
      ModificationContext modificationContext,
      ResourceLocation customContext) {
    var inputs = modificationResult.inputs();
    var outputs = modificationResult.outputs();

    if (AEAClientConfig.ioLog) {
      logNestedStacks(inputs);
      logStacks(outputs);
    }

    var processedInputs =
        inputs.stream()
            .map(
                slot ->
                    slot.stream()
                        .filter(
                            stack ->
                                !AEAClientConfig.inputsBlackList.contains(stack.what().getId()))
                        .map(this::applyMultiplier)
                        .toList())
            .filter(slot -> !slot.isEmpty())
            .toList();
    var processedOutputs =
        outputs.stream()
            .filter(stack -> !AEAClientConfig.outputsBlackList.contains(stack.what().getId()))
            .map(this::applyMultiplier)
            .toList();

    return new ModificationResult(
        processedInputs, processedOutputs, modificationResult.mergeAdjacently());
  }

  public GenericStack applyMultiplier(GenericStack stack) {
    if (AEAClientConfig.ioMultiplier == 1) {
      return stack;
    }
    return new GenericStack(stack.what(), stack.amount() * AEAClientConfig.ioMultiplier);
  }

  private void logNestedStacks(List<List<GenericStack>> slots) {
    String joined =
        slots.stream()
            .map(
                slot ->
                    slot.stream()
                        .map(s -> s.what().getId().toString())
                        .collect(Collectors.joining(", ", "[", "]")))
            .collect(Collectors.joining(", ", "[", "]"));
    AEA.LOGGER.info("{}: {}", "Input", joined);
  }

  private void logStacks(List<GenericStack> stacks) {
    String joined =
        stacks.stream()
            .map(s -> s.what().getId().toString())
            .collect(Collectors.joining(", ", "[", "]"));
    AEA.LOGGER.info("{}: {}", "Output", joined);
  }
}
