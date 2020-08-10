package ast;

import java.util.List;

public class Rel extends AbstractNode{
	
	public Rel(String value, List<Node> children) {
	      super(value, children);
	   }

	public Rel(String value) {
		super(value);
	}

	@Override
	public Node clone() {
		Node root = new Rel(this.getValue());
		return cloneTree(root);
	}
}
