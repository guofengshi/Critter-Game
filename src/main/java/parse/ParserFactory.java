package parse;

/**
 * A factory that gives access to instances of parser.
 */
public class ParserFactory {

   /**
    * Return a {@code Parser} that can parse critter programs.
    * 
    * @return A critter program parser
    */
   public static Parser getParser() {
      return new ParserImpl();
   }
}
