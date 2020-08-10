package ast;

import java.util.ArrayList;
import java.util.List;

/**
 * A representation of a binary Boolean condition: 'and' or 'or'
 *
 */
public class BinaryCondition extends Condition{
   protected Operator operator;
   public String value;
   /**
    * Create an AST representation of l op r.
    * 
    * @param l
    * @param op
    * @param r
    */
   public BinaryCondition(Condition l, Operator op, Condition r) {
      List<Node> children = new ArrayList<Node>(){{add(l); add(r);}};
      this.setChildren(children);
      operator = op;
      value = op.toString();
   }


   /**
    * An enumeration of all possible binary condition operators.
    */
   public enum Operator {
      OR, AND;
   }

   @Override
   public Node clone() {
      List<Node> children = getChildren();
      List<Node> newChildren = new ArrayList<Node>(children.size());
      for (int i = 0; i < children.size(); i ++) {
         if (children.get(i) != null) {
            newChildren.set(i, children.get(i).clone());
         }
      }
      return new BinaryCondition((Condition)newChildren.get(0), operator, (Condition)newChildren.get(1));
   }
}
