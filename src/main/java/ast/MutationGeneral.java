package ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class MutationGeneral implements Mutation{

   protected List<Node> possibleNodes = new ArrayList<Node>();

   protected Node root;

   protected boolean invalid = true;

   private Random rand = new Random();

   @Override
   public boolean equals(Mutation m) {
      return (this.getClass().isInstance(m.getClass()));
   }

   @Override
   public void setRoot(Node n) {
      root = n;
   }

   @Override
   public Node getRandomNode(Node target) {
      int random = new Random().nextInt(target.size());
      return target.nodeAt(random);
   }

   @Override
   public void addNode(Node target, Node qualified) {
      target.addChild(qualified.clone());
   }

   @Override
   public void mutate(Node n) {}

   /**
    * This methods examine whether the same class of node is contained
    * @param n
    * @param cls
    * @return the boolean value whether the node is contained
    */
   @Override
   public boolean isContained(Node n, Class<?> cls) {
      boolean temp = false;
      for (Node aNode : n.allNodes()) {
         if (cls == aNode.getClass()) {
            temp = true;
            possibleNodes.add(aNode);
         }
      }
      return temp;
   }

   @Override
   public boolean isInvalid() {
      return invalid;
   }

   /**
    * This method examine the node with the value if
    * it is contained
    * @param n
    * @param value
    * @return the boolean value whether the node is contained
    */
   @Override
   public boolean isContained(Node n, String value) {
      boolean temp = false;
      for (Node aNode : n.allNodes()) {
         if (value.equals(aNode.getValue())) {
            temp = true;
            possibleNodes.add(aNode);
         }
      }
      return temp;
   }

   /**
    * Randomly select a node which could be mutated
    * @return the qualified node which could be mutated
    */
   @Override
   public Node getQualifiedNode() {
      Node qualified = possibleNodes.get(new Random().nextInt(possibleNodes.size()));
      return qualified;
   }

   /**
    * Get a node similar to the given node, where similarity
    * is defined by the given {@code Predicate}.
    *
    * @param node A prototypical node
    * @param f The {@code Predicate} that determines similarity.
    * @return A node similar to but not == the given node.
    */
   protected Node getSimilar(Node node, Predicate<Node> f) {
      Node root = node;
      while (root.getParent() != null) root = root.getParent();
      List<Node> preorder = ((AbstractNode)root).allNodes();
      List<Node> filtered = filter(preorder, f);
      assert 0 < filtered.size();
      assert filtered.remove(node);
      assert filtered.size() < preorder.size();
      if (filtered.isEmpty()) return null;
      return filtered.get(rand.nextInt(filtered.size()));
   }

   /**
    * A generic method to filter out all elements of a {@code List}
    * that do not satisfy the property specified by the given
    * {@code Predicate}.
    *
    * @param list The initial {@code List} to filter
    * @param f The {@code Predicate} that tells whether to keep an element
    * @return The output list of all elements in the original list
    * satisfying the {@code Predicate}.
    *
    * Example: Given a list of integers, filter out the odd elements:
    *
    * filter(list, n -> n%2 == 0);
    */
   private <T> List<T> filter(List<T> list, Predicate<T> f) {
      List<T> filtered = new ArrayList<>();
      for (T x : list) {
         if (f.test(x)) filtered.add(x);
      }
      return filtered;
   }

}
