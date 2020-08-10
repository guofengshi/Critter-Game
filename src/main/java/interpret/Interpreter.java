package interpret;

import ast.*;
import exceptions.SyntaxError;

import java.beans.Expression;
import java.util.List;
import java.util.Random;

/**
 * A class for interpreting a critter program. This is just a starting
 * point and may be changed as much as you like.
 */
public class Interpreter {

   /**
    * Execute program {@code p} starting from state {@code s} until either the
    * maximum number of rules per turn is reached or some rule whose command
    * contains an action is executed. Returns a result containing the action to
    * be performed; the action may be null if the maximum number of rules per
    * turn was exceeded.
    *
    * @param p
    *           the program to execute
    * @param s
    *           the state in which to execute it
    * @return a {@code Result} containing the action to be performed and the
    *         rule in which the action occurred; either of these may be null if
    *         the maximum number of rules per turn was exceeded.
    */
   Result interpret(Program p, State s) {
      s.setMem(5, 0);
      Result result = interpretRules(p.getChildren(), s);
      s.setMem(5, 0);
      return result;
   }

   public Result getInterpret(Program p, State s) {
      return interpret(p, s);
   }

   private Result interpretRules(List<Node> rules, State s){
      if(s.mem(5)==999){return null;}
      Result result = null;
      for(Node rule: rules){
         if(interpretCondition((Condition) rule.getChildren().get(0), s)){
            Action action = interpretCommand((Command)rule.getChildren().get(1), s);
            result = (action==null)?null:new Result((Rule)rule, action);
            break;
         }
      }
      if(result==null){
         s.setMem(5, s.mem(5)+1);
         result = interpretRules(rules, s);
      }
      return result;
   }

   private boolean interpretCondition(Condition condition, State s){
      if(condition instanceof Relation){
         return interpretRelation((Relation)condition, s);
      }else if(condition instanceof Conjunction){
         return interpretConjunction((Conjunction)condition, s);
      } else{
         for (Node conjunct : condition.getChildren()) {
            if (interpretConjunction((Conjunction) conjunct, s)) return true;
         }
      }
      return false;
   }

   private boolean interpretConjunction(Conjunction conjunct, State s){
      for(Node relation: conjunct.getChildren()){
         assert relation instanceof Relation;
         if(!interpretRelation((Condition)relation, s)) return false;
      }
      return true;
   }

   private boolean interpretRelation(Condition relation, State s){ // expr rel expr or {condition}
      switch (relation.getValue()){
         case "<":
            return interpretExpression((Expr)relation.getChildren().get(0), s) < interpretExpression((Expr)relation.getChildren().get(1), s);
         case "<=":
            return interpretExpression((Expr)relation.getChildren().get(0), s) <= interpretExpression((Expr)relation.getChildren().get(1), s);
         case "=":
            return interpretExpression((Expr)relation.getChildren().get(0), s) == interpretExpression((Expr)relation.getChildren().get(1), s);
         case ">":
            return interpretExpression((Expr)relation.getChildren().get(0), s) > interpretExpression((Expr)relation.getChildren().get(1), s);
         case ">=":
            return interpretExpression((Expr)relation.getChildren().get(0), s) >= interpretExpression((Expr)relation.getChildren().get(1), s);
         case "!=":
            return interpretExpression((Expr)relation.getChildren().get(0), s) != interpretExpression((Expr)relation.getChildren().get(1), s);
         default:
            return interpretCondition(relation, s);
      }
   }

   private Action interpretCommand(Command command, State s){
      for(Node n: command.getChildren()){
         if(n instanceof Update){
            interpretUpdate((Update)n, s);
         }else{
            // n should be an action, as well as the last node in the list
            return (Action) n;
         }
      }
      return null;
   }

   private void interpretUpdate(Update update, State s){
      int memExp = interpretExpression((Expr)update.getChildren().get(0).getChildren().get(0), s);
      if(memExp==5 || memExp >= 7)
      s.setMem(memExp, //Update's first child's value is mem, its child is an Expr
              interpretExpression((Expr)update.getChildren().get(1), s));
   }

   public int interpretExpression(Expr expr, State s){
      switch (expr.getValue()){
         case "+":
            return interpretExpression((Expr) expr.getChildren().get(0), s) + interpretExpression((Expr)expr.getChildren().get(1), s);
         case "-":
            return (expr.getChildren().size()==1)?interpretTerm(expr, s)
                    :interpretExpression((Expr) expr.getChildren().get(0), s) - interpretExpression((Expr)expr.getChildren().get(1), s);
         default:
            return interpretTerm(expr, s);
      }
   }

   private int interpretTerm(Expr term, State s) {
      switch (term.getValue()) {
         case "*":
            return interpretTerm((Expr) term.getChildren().get(0), s) * interpretTerm((Expr) term.getChildren().get(1), s);
         case "/":
            if (interpretTerm((Expr) term.getChildren().get(1), s) != 0) {
               return interpretTerm((Expr) term.getChildren().get(0), s) / interpretTerm((Expr) term.getChildren().get(1), s);
            }
            else return 0;
         case "mod":
            if (interpretTerm((Expr) term.getChildren().get(1), s) != 0) {
               return interpretTerm((Expr) term.getChildren().get(0), s) % interpretTerm((Expr) term.getChildren().get(1), s);
            }
            else return 0;
         default:
            return interpretFactor(term, s);

      }
   }

   private int interpretFactor(Expr factor, State s){
      if(factor.getValue().matches("^[0-9]*$")){
         return Integer.parseInt(factor.getValue());
      }
      switch (factor.getValue()){
         case "-":
            return -interpretFactor((Expr)factor.getChildren().get(0), s);
         case "mem":
            return s.mem(interpretExpression((Expr)factor.getChildren().get(0), s));
         case "nearby":
            return s.nearby(interpretExpression((Expr)factor.getChildren().get(0), s));
         case "ahead":
            return s.ahead(interpretExpression((Expr)factor.getChildren().get(0), s));
         case "random":
            int expr = interpretExpression((Expr)factor.getChildren().get(0), s);
            return (expr>=2)?new Random().nextInt(expr):0;
         case "smell":
            return s.smell();
         default:
            //System.out.println("If this makes it here then something might be wrong");
            return interpretExpression((Expr) factor.getChildren().get(0), s);
         //   return interpretExpression(, s);
      }
   }


}
