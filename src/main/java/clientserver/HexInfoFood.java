package clientserver;

public class HexInfoFood extends HexInfo {

   public HexInfoFood(int r, int c, String t, int v) {
      row = r;
      col = c;
      type = t;
      value = (v + 1) * (-1);
   }
}
