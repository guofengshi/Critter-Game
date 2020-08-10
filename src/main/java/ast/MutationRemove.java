package ast;

import java.util.List;
import java.util.Random;

/**
 * 1. Remove mutation:
 * The node, along with its descendants, is removed. If the parent
 * of the node being removed needs a replacement child, one of the node’s
 * children of the correct kind is randomly selected. For example,
 * a rule node is simply removed, while a binary operation node would be
 * replaced with either its left or its right child.
 */
public class MutationRemove extends MutationGeneral {

   Random rand = new Random();

   @Override public void mutate(Node node) {
      Node parent = node.getParent();
      if (parent == null) {
         invalid = true;
      }
      else {
         List<Node> siblings = parent.getChildren();
         assert siblings.contains(node); // sanity check

         if (node instanceof Rule || node instanceof Update || node instanceof Action) {
            if (siblings.size() <= 1) {
               invalid = true; // do not delete last
            }
            else {
               int index = siblings.indexOf(node);
               siblings.remove(index);
               invalid = true;
            }
         }

         // check for left-hand side of an update
         else if (node instanceof Factor && node.getValue().equals("MEM") && parent instanceof Update
                  && node == (parent).getChildren().get(0)) invalid = true;

         else if (node instanceof Expr || node instanceof Condition) {
            List<Node> children = node.getChildren();
            if (children == null) {
               invalid = true;
            }
            else {
               Node n = children.get(rand.nextInt(children.size()));
               n.setParent(parent);
               int index = siblings.indexOf(node);
               if (index != -1) {
                  siblings.set(index, n);
                  invalid = false;
               }
               else invalid = true;
            }
         } else {
            invalid = true;
         }
      }
   }
}