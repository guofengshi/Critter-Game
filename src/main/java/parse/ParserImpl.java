package parse;

import java.io.Reader;
import java.util.*;

import ast.*;
import exceptions.SyntaxError;

class ParserImpl implements Parser {
   Program program;



   @Override
   public Program parse(Reader r) {
      Tokenizer t = new Tokenizer(r);
      try{
         program = parseProgram(t);
      }catch (SyntaxError e){
         e.printStackTrace();
      }
      return program;
   }

   /**
    * Parses a program from the stream of tokens provided by the Tokenizer,
    * consuming tokens representing the program. All following methods with a
    * name "parseX" have the same spec except that they parse syntactic form X.
    *
    * @return the created AST
    * @throws SyntaxError
    *            if there the input tokens have invalid syntax
    */
   public static ProgramImpl parseProgram(Tokenizer t) throws SyntaxError {
      List<Node> rules = new ArrayList<Node>();
      TokenType token = t.peek().getType();
      while(token!=TokenType.EOF){
         Rule rule = parseRule(t);
         if(rule==null){
            break;
         }
         rules.add(rule);
      }
      return new ProgramImpl("", rules);
   }

   /**
    * Parses a rule by initially parsing a condition (assuming condition --> command),
    * consuming the --> token and then parsing the command.
    * @param t The tokenizer
    * @return a Rule tree representing condition --> command, with --> as the value
    * @throws SyntaxError
    *             if the token stream has invalid syntax
    */
   public static Rule parseRule(Tokenizer t) throws SyntaxError {
      List<Node> children = new ArrayList<Node>();
      children.add(parseCondition(t));
      if(children.get(0) == null){return null;}
      assert t.peek().getType() == TokenType.ARR;
      consume(t, TokenType.ARR);
      children.add(parseCommand(t));
      return new Rule(TokenType.ARR.toString(), children);
   }

   /**
    * Parses a condition, which is made up of at least one conjunction with or's in between.
    * @param t the Tokenizer
    * @return a condition represented by an OR as the value and Conjunctions as its children
    * @throws SyntaxError
    *
    */
   public static Condition parseCondition(Tokenizer t) throws SyntaxError {
      List<Node> children = new ArrayList<Node>();
      List<Node> singleStack = new Stack<>();
      TokenType token = t.peek().getType();
      if(token==TokenType.EOF){return null;}
      while(token!=TokenType.RPAREN && token!=TokenType.ARR && token!=TokenType.RBRACKET
               && token!=TokenType.RBRACE){
         switch (token){
            case OR:
               if(singleStack.isEmpty()){throw new SyntaxError("Please provide a conjunction expression in from of and."
                       + " Line number: " + t.lineNumber());}
               children.add(((Stack<Node>) singleStack).pop());
               consume(t, TokenType.OR);
               children.add(parseConjunction(t));
               while(t.peek().getType() == TokenType.OR){
                  t.next();
                  children.add(parseConjunction(t));
               }
               singleStack.add(new Conjunction(token.toString(), children));
               children = new ArrayList<Node>();
               break;
            default:
               ((Stack<Node>) singleStack).push(parseConjunction(t));
               break;
         }
         //t.next();
         token = t.peek().getType();
      }
      return (Condition)((Stack<Node>) singleStack).pop();

   }

   /**
    * Parses a conjunction, which is made up of at least one relation with and's in between
    * @param t the Tokenizer
    * @return a conjunction represented by an AND as the value and Relations as its children
    * @throws SyntaxError
    */
   public static Conjunction parseConjunction(Tokenizer t) throws SyntaxError{
      List<Node> children = new ArrayList<Node>();
      List<Node> singleStack = new Stack<>();
      TokenType token = t.peek().getType();
      while(token!=TokenType.OR && token!=TokenType.RPAREN && token!=TokenType.ARR
              && token!=TokenType.RBRACKET && token!=TokenType.RBRACE){
         switch (token){
            case AND:
               if(singleStack.isEmpty()){throw new SyntaxError("Please provide a relation expression in from of or."
                       + " Line number: " + t.lineNumber());}
               children.add(((Stack<Node>) singleStack).pop());
               consume(t, TokenType.AND);
               children.add(parseRelation(t));
               while(t.peek().getType() == TokenType.AND){
                  t.next();
                  children.add(parseRelation(t));
               }
               singleStack.add(new Conjunction(token.toString(), children));
               children = new ArrayList<Node>();
               break;
            default:
               ((Stack<Node>) singleStack).push(parseRelation(t));
               break;

         }
         //t.next();
         token = t.peek().getType();
      }
      return (Conjunction)((Stack<Node>) singleStack).pop();
   }


   /**
    * Parses an expression, which is made up of at least one term with addops in between
    * @param t the Tokenizer
    * @return an expression represented by an ADDOP as the value and Terms as its children
    * @throws SyntaxError
    */
   public static Expr parseExpression(Tokenizer t) throws SyntaxError {
      List<Node> children = new ArrayList<Node>();
      List<Node> nodeStack = new Stack<>();
      Queue<TokenType> addOpQueue = new LinkedList<>();
      TokenType token = t.peek().getType();
      while(token!=TokenType.RBRACE && token!=TokenType.RBRACKET && token!=TokenType.ARR && token!=TokenType.RPAREN
              && token.category()!=TokenCategory.RELOP && token!=TokenType.AND && token!=TokenType.OR && token!=TokenType.SEMICOLON){

         switch (token){
            case PLUS:
               if(nodeStack.isEmpty()){throw new SyntaxError(token + " is not supposed to be in an Expression. Please check your syntax."
                       + " Line number: " + t.lineNumber());}
               addOpQueue.add(token);
               t.next();
               break;
            case MINUS:
               if(nodeStack.isEmpty()){
                  ((Stack<Node>) nodeStack).push(parseFactor(t));
                  removeRightEnclosing(t);
               }else{
                  addOpQueue.add(token);
                  t.next();
               }
               break;
            case NUM:
            case NEARBY:
            case SMELL:
            case AHEAD:
            case RANDOM:
            case MEM:
            case ABV_POSTURE:
            case ABV_TAG:
            case ABV_SIZE:
            case ABV_PASS:
            case ABV_OFFENSE:
            case ABV_DEFENSE:
            case ABV_MEMSIZE:
            case ABV_ENERGY:
               ((Stack<Node>) nodeStack).push(parseTerm(t));
               break;
            case LPAREN:
               ((Stack<Node>) nodeStack).push(parseTerm(t)); // LParen is removed in factor
               break;
         default:
               throw new SyntaxError(token + " is not supposed to be in an Expression. Please check the syntax." + " Line number: " + t.lineNumber());
         }

         //t.next();
         token = t.peek().getType();
         if((token.category()==TokenCategory.ACTION || token == TokenType.MEM || token.category() == TokenCategory.MEMSUGAR)
                 && addOpQueue.size() == nodeStack.size()-1){break;} // May be a series of updates.
         removeRightEnclosing(t);
      }

      List<Node> temp = new ArrayList<Node>();
      while(!nodeStack.isEmpty()){
         temp.add(((Stack<Node>) nodeStack).pop());
      }
      while(!temp.isEmpty()){
         ((Stack<Node>) nodeStack).push(temp.remove(0));
      }

      while(!addOpQueue.isEmpty()){
         try {
            children.add(((Stack<Node>) nodeStack).pop());
            children.add(((Stack<Node>) nodeStack).pop());
         }catch (EmptyStackException e){
            throw new SyntaxError("Incorrect number of non-addop arguments. Please check the syntax."
                    + " Line number: " + t.lineNumber());
         }
         ((Stack<Node>) nodeStack).push(new Term(((LinkedList<TokenType>) addOpQueue).pop().toString(), children));
         children = new ArrayList<Node>();
      }
      if(nodeStack.size()!=1){throw new SyntaxError("Incorrect number of non-addop arguments. Please check the syntax."
              + " Line number: " + t.lineNumber()); }
      return (Expr)((Stack<Node>) nodeStack).pop();
   }


   /**
    * Parses a term, which is made up of at least one factor with MODLUP's in between
    * @param t the Tokenizer
    * @return a Term represented by a MODLUP as the value and Factors as the children
    * @throws SyntaxError
    */
   public static Expr parseTerm(Tokenizer t) throws SyntaxError {
      List<Node> children = new ArrayList<Node>();
      TokenType token = t.peek().getType();
      List<Node> singleStack = new Stack<>();  //This should only ever contain 0 or 1 nodes
      while(token.category()!=TokenCategory.ADDOP && token!=TokenType.RBRACE && token!=TokenType.RBRACKET
              && token!=TokenType.ARR && token!=TokenType.RPAREN && token.category()!=TokenCategory.RELOP
              && token!=TokenType.AND && token!=TokenType.OR && token!=TokenType.SEMICOLON){
         switch (token){
            case MUL:
            case DIV:
            case MOD:
               if(singleStack.isEmpty()){throw new SyntaxError(token + "does not belong here. Please correctly format the MULOP expression."
                       + " Line number: " + t.lineNumber());}
               children.add(((Stack<Node>) singleStack).pop());
               consume(t, token);
               children.add(parseFactor(t));
               ((Stack<Node>) singleStack).push(new Term(token.toString(), children));
               children = new ArrayList<Node>();
               break;
            case MEM:
            case NUM:
            case NEARBY:
            case SMELL:
            case AHEAD:
            case RANDOM:
            case ABV_POSTURE:
            case ABV_TAG:
            case ABV_SIZE:
            case ABV_PASS:
            case ABV_OFFENSE:
            case ABV_DEFENSE:
            case ABV_MEMSIZE:
            case ABV_ENERGY:
               ((Stack<Node>) singleStack).push(parseFactor(t));
               break;
            case LPAREN:
               ((Stack<Node>) singleStack).push(parseFactor(t)); // LParen is consumed in factor
               break;
            default:
               throw new SyntaxError(token + " does not belong here. Please correctly format the MULOP expression."
                       + " Line number: " + t.lineNumber());
         }
         //t.next();
         token = t.peek().getType();
         if((token.category()==TokenCategory.ACTION || token == TokenType.MEM)
                 && !singleStack.isEmpty()){break;} // May be a series of updates.
      }
      if(singleStack.size()!=1){throw new SyntaxError("Your expression is missing a MULOP, please supply it where it is missing."
              + " Line number: " + t.lineNumber());}
      return (Term)((Stack<Node>) singleStack).pop();
   }

   /**
    * Parses a factor, which is made up of one of the possible factor tokens stated in the gramamr
    * @param t the Tokenizer
    * @return a Factor representing the token that the tokenizer has.
    * @throws SyntaxError
    *             if the token is not a factor.
    */
   public static Expr parseFactor(Tokenizer t) throws SyntaxError {
      List<Node> children = new ArrayList<Node>();
      TokenType token = t.peek().getType();
      List<Node> memSugarChildren = new ArrayList<Node>();
      if(token!=TokenType.RBRACE && token!=TokenType.RBRACKET
              && token!=TokenType.ARR && token!=TokenType.RPAREN && token.category()!=TokenCategory.RELOP
              && token.category()!=TokenCategory.MULOP && token!=TokenType.PLUS
              && token!=TokenType.AND && token!=TokenType.OR){
         switch (token){
            case NUM:
               Factor factor = new Factor(t.peek().toString(), null);
               consume(t, TokenType.NUM);
               //removeRightEnclosing(t);
               return factor;
            case MEM:
               consume(t, TokenType.MEM);
               consume(t,TokenType.LBRACKET);
               children.add(parseExpression(t));
               if(children.isEmpty()){throw new SyntaxError(token + " does not have an expr. Please format with a [expr]."
                       + " Line number: " + t.lineNumber()); }
               //removeRightEnclosing(t);
               return new Factor(token.toString(), children);
            case LPAREN:
               consume(t, TokenType.LPAREN);
               Expr expr = parseExpression(t);
               //removeRightEnclosing(t);
               return  expr;
            case MINUS:
               consume(t, TokenType.MINUS);
               children.add(parseFactor(t));
               //removeRightEnclosing(t);
               return new Factor(token.toString(), children);
            case RANDOM:
            case NEARBY:
            case SMELL:
            case AHEAD:
               return parseSensor(t);
            case ABV_POSTURE:
               consume(t, TokenType.ABV_POSTURE);
               memSugarChildren.add(new Factor("7", null));
               //removeRightEnclosing(t);
               return new Factor("mem", memSugarChildren);
            case ABV_TAG:
               consume(t, TokenType.ABV_TAG);
               memSugarChildren.add(new Factor("6", null));
               //removeRightEnclosing(t);
               return new Factor("mem", memSugarChildren);
            case ABV_SIZE:
               consume(t, TokenType.ABV_SIZE);
               memSugarChildren.add(new Factor("3", null));
               //removeRightEnclosing(t);
               return new Factor("mem", memSugarChildren);
            case ABV_PASS:
               consume(t, TokenType.ABV_PASS);
               memSugarChildren.add(new Factor("5", null));
               //removeRightEnclosing(t);
               return new Factor("mem", memSugarChildren);
            case ABV_OFFENSE:
               consume(t, TokenType.ABV_OFFENSE);
               memSugarChildren.add(new Factor("2", null));
               //removeRightEnclosing(t);
               return new Factor("mem", memSugarChildren);
            case ABV_DEFENSE:
               consume(t, TokenType.ABV_DEFENSE);
               memSugarChildren.add(new Factor("1", null));
               //removeRightEnclosing(t);
               return new Factor("mem", memSugarChildren);
            case ABV_MEMSIZE:
               consume(t, TokenType.ABV_MEMSIZE);
               memSugarChildren.add(new Factor("0", null));
               //removeRightEnclosing(t);
               return new Factor("mem", memSugarChildren);
            case ABV_ENERGY:
               consume(t, TokenType.ABV_ENERGY);
               memSugarChildren.add(new Factor("4", null));
               //removeRightEnclosing(t);
               return new Factor("mem", memSugarChildren);
            default:
               break;
         }
      }
      throw new SyntaxError(token + " does not belong in factor." + " Line number: " + t.lineNumber());
   }

   private static void removeRightEnclosing(Tokenizer t){
      if(/*t.peek().getType()==TokenType.RBRACE ||*/ t.peek().getType() == TokenType.RBRACKET
               || t.peek().getType() == TokenType.RPAREN){t.next();}
   }

   /**
    * Parses a command, which is represented by a series of {@code Update} and an optional final {@code Action}
    * @param t The tokenizer
    * @return a Command tree
    * @throws SyntaxError
    *                Will throw an error if there are updates after an action or if the syntax is incorrect.
    */
   public static Command parseCommand(Tokenizer t) throws SyntaxError {
      List<Node> children = new ArrayList<Node>();
      TokenType token = t.peek().getType();
      while(token!=TokenType.SEMICOLON){
         if(token.category()==TokenCategory.ACTION){ // Checks if it's an action
            if(token == TokenType.TAG || token == TokenType.SERVE){ // Checks if this action needs a child node
               t.next();
               consume(t, TokenType.LBRACKET);
               List<Node> temp = new ArrayList<Node>();
               temp.add(parseExpression(t));
               children.add(new Action(token.toString(), temp));
            }else{
               children.add(new Action(token.toString(), null));
               t.next();
            }
            //t.next();
            if(t.peek().getType()!=TokenType.SEMICOLON){throw new SyntaxError("Incorrect Syntax: Action can only be placed at the end of a command."
                    + " Line number: " + t.lineNumber());}
         }else if(token==TokenType.MEM || token.category()==TokenCategory.MEMSUGAR){ // Checks if it's an update
            List<Node> assignsChildren = new ArrayList<Node>();
            assignsChildren.add(parseFactor(t));
            consume(t, TokenType.ASSIGN);
            assignsChildren.add(parseExpression(t));
            children.add(new Update(TokenType.ASSIGN.toString(), assignsChildren));
         }else{
            throw new SyntaxError(token + " does not belong in a command expression. Please fix the syntax on line " + t.lineNumber());
         }
         //t.next();
         token = t.peek().getType();
      }
      consume(t, TokenType.SEMICOLON);
      return new Command("", children);
   }

   /**
    * Parses a sensor
    * @param t The tokenizer
    * @return a Sensor tree representing sensor[expr] or sensor
    * @throws SyntaxError
    *             If a non-sensor token or there is no expr in sensor[expr]
    */
   public static Sensor parseSensor(Tokenizer t)throws SyntaxError{
      List<Node> children = new ArrayList<Node>();
      TokenType token = t.peek().getType();
      if(token!=TokenType.RBRACE && token!=TokenType.RBRACKET
              && token!=TokenType.ARR && token!=TokenType.RPAREN && token.category()!=TokenCategory.RELOP
              && token!=TokenType.AND && token!=TokenType.OR){
         switch (token){
            case SMELL:
               consume(t, TokenType.SMELL); // moves tokenizer one token forward.
               //removeRightEnclosing(t);
               return new Sensor(token.toString(), null);
            case NEARBY:
            case RANDOM:
            case AHEAD:
               t.next(); // moves tokenizer one token forward.
               consume(t, TokenType.LBRACKET);
               children.add(parseExpression(t));
               if(children.isEmpty()){throw new SyntaxError(token + " does not have an expr. Please format with a [expr]." +
                       " Line number: " + t.lineNumber()); }
               //removeRightEnclosing(t);
               return new Sensor(token.toString(), children);
            default:
               break;
         }
      }
      throw new SyntaxError(token + "does not belong here. Please correctly format the Sensor expression." + " Line number: " + t.lineNumber());

   }

   /**
    * Parses a relation in the form
    * expr rel expr | {condition}
    * @param t The tokenizer
    * @return a Relation tree
    * @throws SyntaxError
    *             Does not throw it explicitly in this function, passes it down.
    */
   public static Condition parseRelation(Tokenizer t) throws SyntaxError{
      List<Node> children = new ArrayList<Node>();
      TokenType token = t.peek().getType();

      if(token==TokenType.LBRACE){ // checks if relation is {condition}
         t.next();
         Condition con = parseCondition(t);
         consume(t, TokenType.RBRACE);
         return con;
      }

      children.add(parseExpression(t)); // Parse first expression
      token = t.peek().getType(); // update token
      if(token.category()!=TokenCategory.RELOP){throw new SyntaxError(token + " is not proper syntax in Relation.");}
      Rel rel = new Rel(token.toString(), null);
      t.next();
      children.add(parseExpression(t)); // Parse second expression
      return new Relation(rel.getValue(), children); // This is sort of redundant since all I do is get Rel's value
   }


   /**
    * Consumes a token of the expected type.
    *
    * @throws SyntaxError
    *            if the wrong kind of token is encountered.
    */
   public static void consume(Tokenizer t, TokenType tt) throws SyntaxError {
      TokenType token = t.next().getType();
      if(token != tt){
         throw new SyntaxError("Expected token type: " + tt + ". Received token type: " + token + ". Line number: " + t.lineNumber());
      }
   }
}
