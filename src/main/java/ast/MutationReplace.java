package ast;

import java.util.List;

/**
 * 3. Replace Mutation
 */
public class MutationReplace extends MutationGeneral {
   /**
    * This mutate method execute mutation of replace on
    * the qualified target node
    * @param target
    */
   @Override
   public void mutate(Node target) {
      if (!(target instanceof Program)) {
         Node n = getSimilar(target, m -> m.getClass() == target.getClass());
         if (n == null) {
            invalid = true;
         }
         else {
            n = n.clone();
            Node parent = target.getParent();
            if (parent != null && n != null) {
               n.setParent(parent);
               List<Node> siblings = parent.getChildren();
               assert siblings.contains(target);
               int index = siblings.indexOf(target);
               if (index != -1) {
                  siblings.set(index, n);
                  invalid = false;
               }
               else invalid = true;
            }
            else invalid = true;
         }
      }
      else {
         invalid = true;
      }
   }
}
