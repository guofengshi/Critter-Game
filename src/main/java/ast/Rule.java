package ast;

import java.util.ArrayList;
import java.util.List;

/**
 * A representation of a critter rule.
 */
public class Rule extends AbstractNode {

   public Rule(String value, List<Node> children) {
     super(value, children);
   }

   public Rule(String value) {
      super(value);
   }

   @Override
   public Node clone() {
      Node root = new Rule(this.getValue());
      return cloneTree(root);
   }
}
