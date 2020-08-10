package ast;

import java.util.List;

/**
 * A data structure representing a critter program.
 *
 */
public class ProgramImpl extends AbstractNode implements Program {

   public ProgramImpl(String value, List<Node> children) {
      super(value, children);
   }

   public ProgramImpl(String value) {
      super(value);
   }

   @Override
   public Program mutate() {
      Mutation mutation = MutationFactory.getRandom();
      int count = 0;
      while (mutation.isInvalid() && count < 100) {
         mutation.setRoot(this);
         mutation.mutate(mutation.getRandomNode(this));
         count ++;
      }
      if (!mutation.isInvalid()) {
         return this;
      }
      else {
         return null;
      }
      //      return (Program)(mutation.mutate(mutation.getRandomNode(this)));
   }

   public Program mutateCompatible() {
      Mutation mutation = MutationFactory.getRandomCompatible();
      int count = 0;
      while (mutation.isInvalid() && count < 100) {
         mutation.setRoot(this);
         mutation.mutate(mutation.getRandomNode(this));
         count ++;
      }
      if (!mutation.isInvalid()) {
         return this;
      }
      else {
         return null;
      }
   }

   @Override
   public Program mutate(int index, Mutation m) {
      try {
         m.setRoot(this);
         System.out.println("the class of this node is " + this.nodeAt(index).getClass());
         System.out.println("the value of this node is " + this.nodeAt(index).getValue());
         m.mutate(this.nodeAt(index));
      } catch (IndexOutOfBoundsException e1) {
         System.out.println("The index is out of bound");
         //      } catch (UnsupportedMutationException e2) {
         //         return null;
         //      }
      }
      if (!m.isInvalid()) {
         return this;
      }
      else {
         return null;
      }
   }

   @Override
   public Node clone() {
      Node root = new ProgramImpl(this.getValue());
      return cloneTree(root);
   }
}
