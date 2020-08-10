package ast;

import java.util.Random;

/**
 * A factory that produces the public static Mutation objects corresponding to
 * each mutation
 */
public class MutationFactory {
   public static Mutation getRemove() {
      return new MutationRemove();
   }

   public static Mutation getSwap() {
      return new MutationSwap();
   }

   public static Mutation getReplace() {
      return new MutationReplace();
   }

   public static Mutation getTransform() {
      return new MutationTransform();
   }

   public static Mutation getInsert() {
      return new MutationInsert();
   }

   public static Mutation getDuplicate() {
      return new MutationDuplicate();
   }

   public static Mutation getRandom() {
      int randNum = new Random().nextInt(6);
      if (randNum == 0) {
         System.out.println("Mutation: Swap Mutation");
         return getSwap();
      }
      else if (randNum == 1) {
         System.out.println("Mutation: Duplicate Mutation");
         return getDuplicate();
      }
      else if (randNum == 2) {
         System.out.println("Mutation: Transform Mutation");
         return getTransform();
      }
      else if (randNum == 3) {
         System.out.println("Mutation: Remove Mutation");
         return getRemove();
      }
      else if (randNum == 4) {
         System.out.println("Mutation: Insert Mutation");
         return getInsert();
      }
      else {
         System.out.println("Mutation: Replace Mutation");
         return getReplace();
      }
   }

   /**
    * @return a random mutation among mutations implemented by us or Transform Mutation.
    */
   public static Mutation getRandomCompatible() {
      int randNum = new Random().nextInt(4);
      if (randNum == 0) {
         System.out.println("Mutation: Swap Mutation");
         return getSwap();
      }
      else if (randNum == 1) {
         System.out.println("Mutation: Duplicate Mutation");
         return getDuplicate();
      }
      else if (randNum == 2) {
         System.out.println("Mutation: Transform Mutation");
         return getTransform();
      }
      else {
         System.out.println("Mutation: Replace Mutation");
         return getReplace();
      }
   }
}
