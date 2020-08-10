package ast;

import java.util.List;

public class Term extends Expr {
   public Term(String value, List<Node> children) {
      super(value, children);
   }

   public Term(String value) {
      super(value);
   }

   @Override
   public Node clone() {
      Node root = new Term(this.getValue());
      return cloneTree(root);
   }
}
