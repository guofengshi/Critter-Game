package ast;

import java.util.Random;
import parse.TokenType;

/**
 * 4. Transform mutation:
 * The node is replaced with a randomly chosen node of the same kind
 * (for example, replacing attack with eat, or + with *), but its
 * children remain the same. Literal integer constants are adjusted
 * up or down by the value of java.lang.Integer.MAX_VALUE/r.nextInt(),
 * where legal, and where r is a java.util.Random object.
 */
public class MutationTransform extends MutationGeneral {

   Random rand = new Random();

   @Override
   public void mutate(Node node) {

      if (node instanceof Sensor) {
         Sensor s = (Sensor)node;
         final TokenType[] sensorTypes = { TokenType.RANDOM, TokenType.AHEAD, TokenType.NEARBY };
         if (s.getValue().equals(TokenType.SMELL.toString())) {
            invalid = true;
         }
         else {
            int i = rand.nextInt(sensorTypes.length);
            while (s.getValue().equals(sensorTypes[i].toString())) i = rand.nextInt(sensorTypes.length);
            s.setValue(sensorTypes[i].toString());
            invalid = false;
         }
      }

      else if (!(node instanceof Factor) && (node instanceof Term || node instanceof Expr)) {
         Expr b = (Expr)node;
         final TokenType[] exprTypes = { TokenType.PLUS, TokenType.MINUS,
                  TokenType.MUL, TokenType.DIV, TokenType.MOD };
         int i = rand.nextInt(exprTypes.length);
         while (b.getValue().equals(exprTypes[i].toString())) i = rand.nextInt(exprTypes.length);
         b.setValue(exprTypes[i].toString());
         invalid = false;
      }

      else if (node instanceof Relation) {
         Relation r = (Relation)node;
         final TokenType[] relTypes = { TokenType.LE, TokenType.LT,
                  TokenType.GE, TokenType.GT, TokenType.EQ, TokenType.NE };
         int i = rand.nextInt(relTypes.length);
         while (r.getValue().equals(relTypes[i].toString())) i = rand.nextInt(relTypes.length);
         r.setValue(relTypes[i].toString());
         invalid = false;
      }

      else if (node instanceof Condition) {
         Condition b = (Condition)node;
         if (b.getValue().equals(TokenType.AND.toString())) b.setValue(TokenType.OR.toString());
         else b.setValue(TokenType.AND.toString());
         invalid = false;
      }

      else if (node instanceof Action) {
         Action a = (Action)node;
         if (a.getChildren() == null) {
            final TokenType[] actionTypes = { TokenType.WAIT, TokenType.FORWARD, TokenType.BACKWARD,
                     TokenType.LEFT, TokenType.RIGHT, TokenType.EAT, TokenType.ATTACK,
                     TokenType.GROW, TokenType.BUD, TokenType.MATE };
            int i = rand.nextInt(actionTypes.length);
            while (a.getValue().equals(actionTypes[i].toString())) i = rand.nextInt(actionTypes.length);
            a.setValue(actionTypes[i].toString());
         } else {
            if (a.getValue().equals(TokenType.TAG.toString())) a.setValue(TokenType.SERVE.toString());
            else a.setValue(TokenType.TAG.toString());
         }
         invalid = false;
      }

      else if (node instanceof Factor && node.getChildren() == null) {
         Factor n = (Factor)node;
         int adjust = java.lang.Integer.MAX_VALUE/(1 + rand.nextInt());
         int newValue = Integer.parseInt(n.getValue()) + (rand.nextBoolean() ? adjust : -adjust);
         if (newValue < 0) newValue = 0;
         n.setValue(Integer.toString(newValue));
         invalid = false;
      }

      else {
         invalid = true;
      }
   }
}
