package clientserver;

import java.util.ArrayList;

/**
 * this is the content of the possible hexs for food, rock and critters or null
 */
public class HexInfo {
   public String type;
   public Integer id = null;
   public Integer value = null;
   public String species_id;
   public String program;
   public int row, col;
   public Integer direction = null;
   public ArrayList<Integer> mem;
   public Integer recently_executed_rule = null;
}
