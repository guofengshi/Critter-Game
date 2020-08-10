package ast;

import java.util.ArrayList;
import java.util.List;

public class Action extends Command{
   public Action(String value, List<Node> children) {
      super(value, children);
   }

   public Action(String value) {
      super(value);
   }

   @Override
   public Node clone() {
      Node root = new Action(this.getValue());
      return cloneTree(root);
   }
}
