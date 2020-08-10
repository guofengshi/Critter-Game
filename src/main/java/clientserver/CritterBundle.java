package clientserver;


import java.util.ArrayList;

/**
 * this bundle is used for adding critters
 */
public class CritterBundle{
    String species_id;
    String program;
    int[] mem;
    PositionBundle[] positions;
    int num;

    public CritterBundle(String s, String pro, int[] m, PositionBundle[] pos, int num) {
        species_id = s;
        program = pro;
        mem = m;
        positions = pos;
        this.num = num;
    }

}
