package ast;

/**
 * A mutation to the AST
 */
public interface Mutation {
   /**
    * Compares the type of this mutation to {@code m}
    * 
    * @param m
    *           The mutation to compare with
    * @return Whether this mutation is the same type as {@code m}
    */
   boolean equals(Mutation m);

   void mutate(Node n);

   void setRoot(Node n);

   Node getRandomNode(Node n);

   void addNode(Node target, Node qualified);

   boolean isContained(Node n, Class<?> cls);

   boolean isInvalid();

   boolean isContained(Node n, String value);

//   void replace(Node target, Node qualified);

   Node getQualifiedNode();
}
