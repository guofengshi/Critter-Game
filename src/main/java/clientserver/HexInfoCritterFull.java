package clientserver;

import java.util.ArrayList;

public class HexInfoCritterFull extends HexInfo {

   public HexInfoCritterFull(String t, int i, String s, String p, int r, int c, int d, ArrayList<Integer> m, int rec) {
      type = t;
      id = i;
      species_id = s;
      program = p;
      row = r;
      col = c;
      direction = d;
      mem = m;
      recently_executed_rule = rec;
   }
}
