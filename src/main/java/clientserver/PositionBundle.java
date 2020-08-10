package clientserver;

/**
 * this bundle is used for the addcritter method in order to locate
 */
public class PositionBundle {
    public int row;
    public int col;
    public int direction;

    public PositionBundle(int r, int c, int d){
        row = r;
        col = c;
        direction = d;
    }
}
