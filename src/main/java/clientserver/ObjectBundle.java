package clientserver;

/**
 * this bundle is used as the parent of the all the possible objects
 */
public class ObjectBundle {
    int row;
    int col;
    String type;
    int amount;

    ObjectBundle(int r, int c, String t, int a){
        row = r;
        col = c;
        type = t;
        amount = a;
    }
}
