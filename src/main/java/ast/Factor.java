package ast;

import java.util.List;

public class Factor extends Term {
   public Factor(String value, List<Node> children) {
      super(value, children);
   }

   public Factor(String value) {
      super(value);
   }

   @Override
   public Node clone() {
      Node root = new Factor(this.getValue());
      return cloneTree(root);
   }
}
