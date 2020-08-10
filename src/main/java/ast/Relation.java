package ast;

import java.util.List;

public class Relation extends Conjunction {
   public Relation(String value, List<Node> children) {
      super(value, children);
   }

   public Relation(String value) {
      super(value);
   }

   @Override
   public Node clone() {
      Node root = new Relation(this.getValue());
      return cloneTree(root);
   }
}
