package parse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An instance represents a Token with a category and a string representation.
 */
public enum TokenType {
   MEM(TokenCategory.OTHER, "mem"),
   WAIT(TokenCategory.ACTION, "wait"),
   FORWARD(TokenCategory.ACTION, "forward"),
   BACKWARD(TokenCategory.ACTION, "backward"),
   LEFT(TokenCategory.ACTION, "left"),
   RIGHT(TokenCategory.ACTION, "right"),
   EAT(TokenCategory.ACTION, "eat"),
   ATTACK(TokenCategory.ACTION, "attack"),
   GROW(TokenCategory.ACTION, "grow"),
   BUD(TokenCategory.ACTION, "bud"),
   MATE(TokenCategory.ACTION, "mate"),
   TAG(TokenCategory.ACTION, "tag"),
   SERVE(TokenCategory.ACTION, "serve"),
   OR(TokenCategory.OTHER, "or"),
   AND(TokenCategory.OTHER, "and"),
   LT(TokenCategory.RELOP, "<"),
   LE(TokenCategory.RELOP, "<="),
   EQ(TokenCategory.RELOP, "="),
   GE(TokenCategory.RELOP, ">="),
   GT(TokenCategory.RELOP, ">"),
   NE(TokenCategory.RELOP, "!="),
   PLUS(TokenCategory.ADDOP, "+"),
   MINUS(TokenCategory.ADDOP, "-"),
   MUL(TokenCategory.MULOP, "*"),
   DIV(TokenCategory.MULOP, "/"),
   MOD(TokenCategory.MULOP, "mod"),
   ASSIGN(TokenCategory.OTHER, ":="),
   NEARBY(TokenCategory.SENSOR, "nearby"),
   AHEAD(TokenCategory.SENSOR, "ahead"),
   RANDOM(TokenCategory.SENSOR, "random"),
   SMELL(TokenCategory.SENSOR, "smell"),
   LBRACKET(TokenCategory.OTHER, "["),
   RBRACKET(TokenCategory.OTHER, "]"),
   LPAREN(TokenCategory.OTHER, "("),
   RPAREN(TokenCategory.OTHER, ")"),
   LBRACE(TokenCategory.OTHER, "{"),
   RBRACE(TokenCategory.OTHER, "}"),
   ARR(TokenCategory.OTHER, "-->"),
   SEMICOLON(TokenCategory.OTHER, ";"),
   ABV_MEMSIZE(TokenCategory.MEMSUGAR, "MEMSIZE"),
   ABV_DEFENSE(TokenCategory.MEMSUGAR, "DEFENSE"),
   ABV_OFFENSE(TokenCategory.MEMSUGAR, "OFFENSE"),
   ABV_SIZE(TokenCategory.MEMSUGAR, "SIZE"),
   ABV_ENERGY(TokenCategory.MEMSUGAR, "ENERGY"),
   ABV_PASS(TokenCategory.MEMSUGAR, "PASS"),
   ABV_TAG(TokenCategory.MEMSUGAR, "TAG"),
   ABV_POSTURE(TokenCategory.MEMSUGAR, "POSTURE"),
   NUM(TokenCategory.OTHER, "<number>"),
   ERROR(TokenCategory.OTHER, "[error]"),
   EOF(TokenCategory.OTHER, "EOF");

   /** Maps the string representation of a token to its enum. */
   private static final Map<String, TokenType> stringToTypeMap;

   // static initializer to initialize the values of stringToTypeMap
   static {
      final Map<String, TokenType> temp = new HashMap<>();
      for (TokenType t : TokenType.values()) {
         temp.put(t.stringRep, t);
      }
      stringToTypeMap = Collections.unmodifiableMap(temp);
   }

   /** The category of this TokenType. */
   private final TokenCategory category;

   /** String representation of this TokenType. */
   private final String stringRep;

   /**
    * Constructs a new {@code TokenType} with category {@code cat} and string
    * representation {@code s}.
    * 
    * @param tcat
    *           token category, checks {@code tcat != null}
    * @param s
    *           string representation of this token, check {@code s != null}
    */
   private TokenType(TokenCategory tcat, String s) {
      assert tcat != null : "TokenType must have a category";
      assert s != null : "TokenType must have a string representation";
      category = tcat;
      stringRep = s;
   }

   /**
    * Returns this {@code TokenType}'s category.
    * 
    * @return this {@code TokenType}'s category
    */
   public TokenCategory category() {
      return category;
   }

   /**
    * Returns the {@code TokenType} that is represented by the string
    * {@code rep}.
    * 
    * @param rep
    *           the string representing the {@code TokenType}, checks
    *           {@code rep} indeed represents a valid {@code TokenType}
    * @return the {@code TokenType} represented by the string {@code rep}
    */
   public static TokenType getTypeFromString(String rep) {
      return stringToTypeMap.get(rep);
   }

   @Override
   public String toString() {
      return stringRep;
   }
}
