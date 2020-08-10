package clientserver;

import java.util.ArrayList;

public class HexInfoCritterPart extends HexInfo {

   public HexInfoCritterPart(String t, int i, String s, int r, int c, int d, ArrayList<Integer> m) {
      type = t;
      id = i;
      species_id = s;
      row = r;
      col = c;
      direction = d;
      mem = m;
   }
}