package ast;

import parse.TokenType;

import java.util.ArrayList;
import java.util.Random;

/**
 * 5. Insert mutation:
 * A newly created node is inserted as the parent of the mutated node. The old
 * parent of the mutated node becomes the parent of the inserted node, and the
 * mutated node becomes a child of the inserted node. If the inserted node has
 * more than one child, the children that are not the original node are copies
 * of randomly chosen nodes of the right kind from the entire rule set.
 */
public class MutationInsert extends MutationGeneral {
   Random rand = new Random();

   @Override
   public void mutate(Node node) {
      if (applies(node)) {
         Node parent = node.getParent();
         if (parent == null) {
            invalid = true;
         }
         else {
            int index = parent.getChildren().indexOf(node);
            Node c = null;
            Node n = null;
            // lhs of an assignment?
            boolean assg = parent instanceof Update && node == (parent).getChildren().get(0);

            if (node instanceof Condition) {
               n = getSimilar(node, m -> m instanceof Condition);
               if (n == null) {
                  invalid = true;
               } else {
                  Condition c1 = (Condition)n.clone();
                  Condition c2 = (Condition)node;
                  TokenType op = rand.nextBoolean() ? TokenType.AND : TokenType.OR;
                  ArrayList<Node> children;
                  if (rand.nextBoolean()) children = new ArrayList<>() {{
                     add(c1);
                     add(c2);
                  }};
                  else children = new ArrayList<>() {{
                     add(c2);
                     add(c1);
                  }};
                  c = new Condition(op.toString(), children);
                  invalid = false;
                  c.setParent(parent);
                  if (index != -1) {
                     parent.getChildren().set(index, c);
                  }
                  else invalid =true;
               }
            } else if (node instanceof Expr && !assg && rand.nextBoolean()) {
               n = getSimilar(node, m -> m instanceof Expr);
               if (n == null) {
                  invalid = true;
               }
               else {
                  final TokenType[] ops = { TokenType.PLUS, TokenType.MINUS, TokenType.MUL, TokenType.DIV, TokenType.MOD };
                  TokenType op = ops[rand.nextInt(ops.length)];
                  Expr c1 = (Expr)n.clone();
                  Expr c2 = (Expr)node;
                  ArrayList<Node> children = rand.nextBoolean() ? new ArrayList<>() {{
                     add(c1);
                     add(c2);
                  }} : new ArrayList<>() {{
                     add(c2);
                     add(c1);
                  }};
                  c = rand.nextBoolean() ? new Expr(op.toString(), children) : new Expr(op.toString(), children);
                  invalid = false;
                  c.setParent(parent);
                  if (index != -1) {
                     parent.getChildren().set(index, c);
                  }
                  else invalid = true;
               }
            } else {
               if (node.getParent() != null && !(node.getParent() instanceof Conjunction)) {
                  final TokenType[] ops = { TokenType.MEM, TokenType.NEARBY, TokenType.AHEAD, TokenType.RANDOM, TokenType.MINUS };
                  TokenType op = assg ? TokenType.MEM : ops[rand.nextInt(ops.length)];
                  switch (op) {
                  case NEARBY:
                  case AHEAD:
                  case RANDOM:
                     c = new Sensor(op.toString(), new ArrayList<>(){{add(node);}});
                     invalid = false;
                     c.setParent(parent);
                     if (index != -1) {
                        parent.getChildren().set(index, c);
                        break;
                     }
                     else {
                        invalid = true;
                        break;
                     }
                  case MEM:
                     c = new Factor("MEM", new ArrayList<>(){{add(node);}});
                     invalid = false;
                     c.setParent(parent);
                     if (index != -1) {
                        parent.getChildren().set(index, c);
                        break;
                     }
                     else {
                        invalid = true;
                        break;
                     }
                  case MINUS:
                     c = new Factor(op.toString(), new ArrayList<>(){{add(node);}});
                     invalid = false;
                     c.setParent(parent);
                     if (index != -1) {
                        parent.getChildren().set(index, c);
                        break;
                     }
                     else {
                        invalid = true;
                        break;
                     }
                  default:
                     invalid = true;
                  }
               }

            }
         }
      }
      else {
         invalid = true;
      }
   }

   public boolean applies(Node node) {
      return node instanceof Expr || node instanceof Condition;
   }
}

