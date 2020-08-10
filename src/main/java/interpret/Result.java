package interpret;

import ast.Action;
import ast.Rule;

/**
 * The result of interpreting a critter program in a given state. This is just a
 * starting point and may be changed as much as you like.
 */
public class Result {
   Rule rule;
   Action action;


   public Result(Rule rule, Action a) {
      this.rule = rule;
      this.action = a;
   }

   public Action getAction() {
      return action;
   }

   public Rule getRule() {
      return rule;
   }
}
