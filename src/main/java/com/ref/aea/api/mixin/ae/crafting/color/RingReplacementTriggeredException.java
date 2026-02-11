package com.ref.aea.api.mixin.ae.crafting.color;

public class RingReplacementTriggeredException extends RuntimeException {
  @Override
  public synchronized Throwable fillInStackTrace() {
    return this;
  }
}
