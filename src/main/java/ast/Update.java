package ast;

import java.util.ArrayList;
import java.util.List;

public class Update extends Command{

   public Update(String value, List<Node> children) {
      super(value, children);
   }

   public Update(String value) {
      super(value);
   }

   @Override
   public Node clone() {
      Node root = new Update(this.getValue());
      return cloneTree(root);
   }
}
