package ast;

import java.util.List;
import java.util.Random;

/**
 * 2. Swap mutation:
 * The order of two children of the node is switched. For example,
 * this allows swapping the positions of two rules, or changing
 * a - b to b - a.
 */
public class MutationSwap extends MutationGeneral {

    public void mutate(Node node) {
        if (applies(node)) {
            List<Node> children = node.getChildren();
            boolean hasAction = children.size() >= 1
                     && children.get(children.size() - 1) instanceof Action;
            int range = hasAction ? children.size() - 1 : children.size();
            if (range <= 1) {
                invalid = true; // not enough to swap
            }
            else {
                Random rand = new Random();
                int i = rand.nextInt(range);
                int j = rand.nextInt(range);
                while (j == i) j = rand.nextInt(range);
                Node temp = children.get(i);
                children.set(i, children.get(j));
                children.set(j, temp);
                invalid = false;
            }
        }
        else {
            invalid = true;
        }
    }

    public boolean applies(Node node) {
        return !(node instanceof Rule)
                 && !(node instanceof Update)
                 && node.getChildren() != null && node.getChildren().size() >= 2;
    }

}