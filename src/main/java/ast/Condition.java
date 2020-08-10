package ast;

import java.util.List;

/**
 * An abstract class representing a Boolean condition in a critter program.
 */
public class Condition extends AbstractNode {

   public Condition(String value, List<Node> children) {
      super(value, children);
   }

   public Condition(String value) {
      super(value);
   }

   public Condition(){}

   @Override
   public Node clone() {
      Node root = new Condition(getValue());
      return cloneTree(root);
   }
}
