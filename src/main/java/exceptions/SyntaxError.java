package exceptions;

/**
 * An exception indicating a syntax error.
 */
public class SyntaxError extends Exception {

   public SyntaxError(String message) {
      super(message);
   }
}
