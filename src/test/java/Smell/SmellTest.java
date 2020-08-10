package Smell;

import model.Critter;
import model.Location;
import model.ShortestPath;
import model.World;
import org.junit.jupiter.api.Test;

import static model.World.addCritter;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

public class SmellTest {
   World w1;

   //Same scenario as the one set in A7 document
   @Test
   void test1() throws IOException {
      w1 = new World();
      w1.init("C:\\Users\\Tony\\Desktop\\School\\Freshman\\Semester_One\\CS_2112\\team-cw-gs522-tzz3-xl289-critter_world\\src\\test\\resources\\examples\\Smell-test.txt");
      Critter c = w1.w.H[4][8].getCritter();
      c.s = new ShortestPath(new Location(4,8), c.getDir(), w1.w);
      assertEquals(c.smell(), 4000);
   }

   //Same scenario as the one set in A7 document except food 2 is removed
   @Test
   void test2() throws IOException {
      w1 = new World();
      w1.init("C:\\Users\\Tony\\Desktop\\School\\Freshman\\Semester_One\\CS_2112\\team-cw-gs522-tzz3-xl289-critter_world\\src\\test\\resources\\examples\\Smell-test.txt");
      Critter c = w1.w.H[4][8].getCritter();
      w1.w.H[9][13].setValue(0);
      c.s = new ShortestPath(new Location(4,8), c.getDir(), w1.w);
      assertEquals(c.smell(), 6000);
   }

   //Same scenario as the one set in A7 document except food 2, 3, 4, 5 are removed
   @Test
   void test3() throws IOException {
      w1 = new World();
      w1.init("C:\\Users\\Tony\\Desktop\\School\\Freshman\\Semester_One\\CS_2112\\team-cw-gs522-tzz3-xl289-critter_world\\src\\test\\resources\\examples\\Smell-test.txt");
      Critter c = w1.w.H[4][8].getCritter();
      w1.w.H[9][13].setValue(0);
      w1.w.H[6][13].setValue(0);
      w1.w.H[6][12].setValue(0);
      w1.w.H[9][8].setValue(0);
      c.s = new ShortestPath(new Location(4,8), c.getDir(), w1.w);
      assertEquals(c.smell(), 1000000);
   }

   //Same scenario as the one set in A7 document except food 2, 4, 5 are removed
   @Test
   void test4() throws IOException {
      w1 = new World();
      w1.init("C:\\Users\\Tony\\Desktop\\School\\Freshman\\Semester_One\\CS_2112\\team-cw-gs522-tzz3-xl289-critter_world\\src\\test\\resources\\examples\\Smell-test.txt");
      Critter c = w1.w.H[4][8].getCritter();
      w1.w.H[9][13].setValue(0);
      w1.w.H[6][13].setValue(0);
      w1.w.H[6][12].setValue(0);
      c.s = new ShortestPath(new Location(4,8), c.getDir(), w1.w);
      assertEquals(c.smell(), 8000);
   }

   //Random world created with no food
   @Test
   void test5(){
      w1 = new World(15,15);
      w1.H[4][8].setValue(0); // make sure there are no rocks or anything
      Critter s = addCritter("C:\\Users\\Tony\\Desktop\\School\\Freshman\\Semester_One\\CS_2112\\team-cw-gs522-tzz3-xl289-critter_world\\src\\test\\resources\\examples\\example-critter.txt");
      w1.H[4][8].setCritter(s);
      w1.addCritterLoc(new Location(4, 8));
      Critter c = w1.H[4][8].getCritter();
      c.s = new ShortestPath(new Location(c.col, c.row), c.getDir(), w1);
      assertEquals(c.smell(), 1000000);

   }


}
