package ast;

import java.util.List;

public class Conjunction extends Condition {

   public Conjunction(String value, List<Node> children) {
      super(value, children);
   }

   public Conjunction(String value) {
      super(value);
   }

   @Override
   public Node clone() {
      Node root = new Conjunction(getValue());
      return cloneTree(root);
   }
}
