package com.ref.aea.util;

import appeng.api.stacks.AmountFormat;
import appeng.util.ReadableNumberConverter;
import java.text.NumberFormat;

public class FormatUtil {

  public static String formatAmountFromAE(long amount, AmountFormat format) {
    return switch (format) {
      case FULL -> NumberFormat.getNumberInstance().format(amount);
      case SLOT -> ReadableNumberConverter.format(amount, 4);
      case SLOT_LARGE_FONT -> ReadableNumberConverter.format(amount, 3);
    };
  }
}
