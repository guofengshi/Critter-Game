package clientserver;

import java.util.ArrayList;

/**
 * this bundle is used for giving the information of critters
 */
public class CritterInfo {
   protected Integer id;
   protected String species_id;
   protected String program;
   protected Integer row, col, direction;
   protected ArrayList<Integer> mem;
   protected Integer recently_executed_rule;
}
