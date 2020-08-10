package ast;

import java.util.ArrayList;
import java.util.List;

public class Command extends AbstractNode {

   public Command(AbstractNode n) {
      super(n);
   }

   public Command(String value, List<Node> children) {
      super(value, children);
   }

   public Command(String value) {
      super(value);
   }

   @Override
   public Node clone() {
      Node root = new Command(this.getValue());
      return cloneTree(root);
   }
}
