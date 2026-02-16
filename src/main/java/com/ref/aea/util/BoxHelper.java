package com.ref.aea.util;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

public class BoxHelper {
  public static Map<Direction, AABB> createRotatedBoxes(AABB southBox) {
    Map<Direction, AABB> map = new EnumMap<>(Direction.class);
    for (Direction dir : Direction.values()) {
      map.put(dir, rotate(southBox, dir));
    }
    return map;
  }

  private static AABB rotate(AABB box, Direction dir) {
    return switch (dir) {
      case SOUTH -> box;
      case NORTH -> new AABB(box.minX, box.minY, 1 - box.maxZ, box.maxX, box.maxY, 1 - box.minZ);
      case WEST -> new AABB(1 - box.maxZ, box.minY, box.minX, 1 - box.minZ, box.maxY, box.maxX);
      case EAST -> new AABB(box.minZ, box.minY, 1 - box.maxX, box.maxZ, box.maxY, 1 - box.minX);
      case UP -> new AABB(box.minX, box.minZ, 1 - box.maxY, box.maxX, box.maxZ, 1 - box.minY);
      case DOWN -> new AABB(box.minX, 1 - box.maxZ, box.minY, box.maxX, 1 - box.minZ, box.maxY);
    };
  }
}
