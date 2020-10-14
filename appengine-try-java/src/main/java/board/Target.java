package org.hirschhorn.ricochet.board;

import java.util.ArrayList;
import java.util.List;

public class Target {

  private static final List<Target> targets = buildAllTargets();
  
  private final Color color;
  private final Shape shape;
  
  private Target(Color color, Shape shape) {
    this.color = color;
    this.shape = shape;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((color == null) ? 0 : color.hashCode());
    result = prime * result + ((shape == null) ? 0 : shape.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Target other = (Target) obj;
    if (color != other.color)
      return false;
    if (shape != other.shape)
      return false;
    return true;
  }

  public Color getColor() {
    return color;
  }

  public Shape getShape() {
    return shape;
  }

  public static List<Target> buildAllTargets() {
    List<Target> targets = new ArrayList<>();
    for (Color color : Color.values()) {
      for (Shape shape : Shape.values()) {
        targets.add(new Target(color, shape));
      }
    }
    return targets;
  }
  
  public static List<Target> getTargets() {
    return targets;
  }
  
  public static Target getTarget(Color color, Shape shape){
    for (Target target : targets) {
      if (target.getColor().equals(color) && target.getShape().equals(shape)){
        return target;
      }
    }
    throw new AssertionError("Incorrect shape or color for target: " + shape + color);
  }

  @Override
  public String toString() {
    return "Target [color=" + color + ", shape=" + shape + "]";
  }
  
}
