package ast;

import java.util.List;

/**
 * A critter program expression that has an integer value.
 */
public class Expr extends AbstractNode {

   public Expr(String value, List<Node> children) {
      super(value, children);
   }

   public Expr(String value) {
      super(value);
   }

   @Override
   public Node clone() {
      Node root = new Expr(getValue());
      return cloneTree(root);
   }
}
