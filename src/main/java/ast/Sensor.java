package ast;

import java.util.List;

public class Sensor extends Factor {
   public Sensor(String value, List<Node> children) {
      super(value, children);
   }

   public Sensor(String value) {
      super(value);
   }

   @Override
   public Node clone() {
      Node root = new Sensor(this.getValue());
      return cloneTree(root);
   }
}
