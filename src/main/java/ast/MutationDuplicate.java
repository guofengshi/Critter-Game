package ast;

import java.util.List;

/**
 * 6. Duplicate Mutation
 */
public class MutationDuplicate extends MutationGeneral{
   /**
    * This mutate method execute the duplicate mutation
    * for the qualified nodes
    * @param target
    */
   @Override
   public void mutate(Node target) {
      if (target instanceof ProgramImpl) {
         if (isContained(root, Rule.class)) {
            addNode(target, getQualifiedNode());
            invalid = false;
         } else {
            invalid = true;
         }
      }
      else if (!(target instanceof Relation) && target instanceof Conjunction) {
         boolean hasRelation = isContained(root, Relation.class);
         boolean hasCondition = isContained(root, Condition.class);
         if (hasRelation || hasCondition) {
            addNode(target, getQualifiedNode());
            invalid = false;
         }
         else {
            invalid = true;
         }
      }
      else if (!(target instanceof Relation) && target instanceof Condition) {
         boolean hasRelation = isContained(root, Relation.class);
         boolean hasConjunction = isContained(root, Conjunction.class);
         if (hasRelation || hasConjunction) {
            addNode(target, getQualifiedNode());
            invalid = false;
         }
         else {
            invalid = true;
         }
      }
      else if (target instanceof Command && !(target instanceof Action) && !(target instanceof Update)) {
         List<Node> children = target.getChildren();
         boolean temp = false;
         for (Node a : children) {
            if (a instanceof Action) {
               invalid = true;
               temp = true;
               break;
            }
         }
         if (!temp){
            boolean hasAction = isContained(root, Action.class);
            boolean hasUpdate = isContained(root, Update.class);
            if (hasAction || hasUpdate) {
               addNode(target, getQualifiedNode());
               invalid = false;
            }
            else {
               invalid = true;
            }
         }
     }
     else {
      invalid = true;
     }
   }
}
