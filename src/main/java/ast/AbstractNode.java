package ast;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractNode implements Node {
   private String value;
   private List<Node> children;
   private Node parent;

    /**
     * This constructor takes the value of the node and
     * its children
     * @param value
     * @param children
     */
   public AbstractNode(String value, List<Node> children){
      setValue(value);
      setChildren(children);
      connectParent();
   }

    /**
     * This constructor takes only one AbstractNode
     * and copies its value, children and parent
     * @param n
     */
   public AbstractNode(AbstractNode n){
      this.value = n.value;
      this.children = n.children;
      this.parent = n.parent;
   }

    /**
     * This constructor only takes the value of the node
     * @param value
     */
   public AbstractNode(String value) {
      this.value = value;
   }

    /**
     * This is an empty constructor
     */
   public AbstractNode(){}

    /**
     * The number of nodes in the AST rooted at this node, including this node
     *
     * @return The size of the AST rooted at this node
     */
    @Override
    public int size() {
       int size = 0;
       if (children == null) {
          return 1;
       }
       for (Node aChildren : children) {
          size += aChildren.size();
       }
       size += 1;
       return size;
    }

    /**
     * Put all the nodes in the AST into an array
     * @return An array contains all of the nodes of AST
     */
    public List<Node> allNodes() {
        List<Node> allNodes = new ArrayList<Node>();
        allNodes.add(this);
        if (getChildren() != null) {
            for (Node aChildren : getChildren()) {
                allNodes.addAll(aChildren.allNodes());
            }
        }
        return allNodes;
    }

    /**
     * Get the node at a certian index
     * @param index The index of the node to retrieve
     * @return The node at the index
     */
    @Override
    public Node nodeAt(int index) {
        return allNodes().get(index);
    }

    /**
     * Prints the AST according to the grammars
     * @param sb
     *           The {@code StringBuilder} to which the program will be appended
     * @return the program of AST in text
     */
    @Override
    public StringBuilder prettyPrint(StringBuilder sb) {
        //this prints the different type of sensors
        if(this instanceof Sensor){
            if(this.children == null){
                sb.append(this.value);
            }else{
                sb.append(this.value);
                sb.append(" [ ");
                sb.append(this.getChildren().get(0).prettyPrint(new StringBuilder()));
                sb.append(" ]");
            }
        }
        //this prints the different type of factors
        else if(this instanceof Factor){
            if(this.value != null){
                if(this.value == "mem"){
                    sb.append("mem[");
                    sb.append(this.children.get(0).prettyPrint(new StringBuilder()));
                    sb.append("]");
                }else if(this.value == "-"){
                    sb.append(this.value);
                    sb.append(this.children.get(0).prettyPrint(new StringBuilder()));
                }else{
                    sb.append(this.value);
                }
            }else if(this.children.get(0) instanceof Sensor){
                sb.append(this.children.get(0).prettyPrint(new StringBuilder()));
            }else if(this.children.get(0) instanceof Expr){
                sb.append(" ( ");
                sb.append(this.children.get(0).prettyPrint(new StringBuilder()));
                sb.append(" ) ");
            }
        }
        //this prints the terms
        else if(this instanceof Term){
            if(this.value != null){
                sb.append(this.children.get(0).prettyPrint(new StringBuilder()));
                sb.append(" " + this.value + " ");
                sb.append(this.children.get(1).prettyPrint(new StringBuilder()) + " ");
            }else{
                sb.append(this.children.get(0).prettyPrint(new StringBuilder()));
            }
        }
        //this prints Rule
        else if(this instanceof Rule) {
        	sb.append(this.getChildren().get(0).prettyPrint(new StringBuilder()));
        	sb.append(" --> ");
        	sb.append(this.getChildren().get(1).prettyPrint(new StringBuilder()));
        }
        //this prints Relation
        else if(this instanceof Relation) {
            if(this.value != null){
                sb.append(this.children.get(0).prettyPrint(new StringBuilder()));
                sb.append(" " + this.value + " ");
                sb.append(this.children.get(1).prettyPrint(new StringBuilder()));
            }else{
                sb.append(" { ");
                sb.append(this.children.get(0).prettyPrint(new StringBuilder()));
                sb.append(" } ");
            }
        }
        //this prints conjunction
        else if(this instanceof Conjunction){
            for(int i = 0; i < this.children.size(); i++) {
                sb.append(this.children.get(i).prettyPrint(new StringBuilder()));
                if(i < this.children.size() - 1){
                    sb.append(" " + this.value + " ");
                }
            }
        }
        //this prints condition
        else if(this instanceof Condition) {
            for(int i = 0; i < this.children.size(); i++) {
                sb.append(this.children.get(i).prettyPrint(new StringBuilder()));
                if(i < this.children.size() - 1){
                    sb.append(this.value);
                }
            }
        }
        //this prints update
        else if(this instanceof Update) {
            sb.append("mem[" + this.children.get(0).getChildren().get(0).prettyPrint(new StringBuilder()) + "]");
        	   sb.append(this.value + " ");
            sb.append(this.children.get(1).prettyPrint(new StringBuilder()));
        }
        //this prints the different type of actions
        else if(this instanceof Action) {
            if(this.children == null || this.children.size() == 0) {
                sb.append(" " + this.value);
            }else{
                sb.append(this.value);
                sb.append("[");
                sb.append(this.getChildren().get(0).prettyPrint(new StringBuilder()));
                sb.append("]");
            }
        }
        //this prints the expression
        else if(this instanceof Expr){
            if(this.value != null){
                sb.append(this.children.get(0).prettyPrint(new StringBuilder()));
                sb.append(this.value + " ");
                sb.append(this.children.get(1).prettyPrint(new StringBuilder()) + " ");
            }else{
                sb.append(this.children.get(0).prettyPrint(new StringBuilder()));
            }
        }
        //this prints the commands
        else if(this instanceof Command) {
            for(Node i : this.children) {
                sb.append(i.prettyPrint(new StringBuilder()));
            }
        }
        else if(this instanceof Program) {
            for(Node i : this.children) {
                sb.append(i.prettyPrint(new StringBuilder())  + ";" + '\n');
            }
        }
        return sb;
    }

    @Override
    public Node clone() {
       return null;
    }

    @Override
    public List<Node> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<Node> children) {
        this.children = children;
    }

    @Override
    public void addChild(Node child) {
       if (getChildren() == null) {
           setChildren(new ArrayList<Node>());
           List<Node> children = getChildren();
           children.add(child);
           setChildren(children);
       }
       else {
           List<Node> children = getChildren();
           children.add(child);
           setChildren(children);
       }
    }

    @Override
    public Node getParent() {
        return parent;
    }

    @Override
    public void setParent(Node p) {
        parent = p;
    }

    @Override
    public String getValue() {
       return value;
    }

    @Override
    public void setValue(String s) {
       value = s;
    }

    @Override
    public void setNode(Node n) {
       this.value = n.getValue();
       this.children = n.getChildren();
       this.parent = n.getParent();
    }

    public void connectParent() {
       if (children != null) {
          for (Node aChildren : children) {
             aChildren.setParent(this);
          }
       }
    }

    /**
     * This methods clone the node and its children
     * @param root
     * @return the cloned node
     */
   @Override
   public Node cloneTree(Node root) {
      List<Node> newChildren = new ArrayList<Node>();
      if (getChildren() == null) {
         return root;
      }
      else {
         for (int i = 0; i < getChildren().size(); i++) {
            newChildren.add(this.getChildren().get(i).cloneTree(getChildren().get(i)));
         }
         root.setChildren(newChildren);
         for (Node aChildren : root.getChildren()) {
            aChildren.setParent(root);
         }
         return root;
      }
   }
}
