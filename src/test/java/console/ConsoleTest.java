package console;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.ArrayList;
import java.util.Random;
import java.io.*;

import org.junit.jupiter.api.Test;
import model.World;
import model.Critter;
import parse.Parser;
import parse.ParserFactory;
import ast.Program;
import model.Constants;
import model.Location;


public class ConsoleTest {

    World w1;
    World w2;
    Critter c1;
    Critter c2;
    Critter c3;



    //load a full critter file

    @Test
    void test1() {
        String filename1 = "/Users/szekwokfung/Documents/GitHub/team-cw-gs522-tzz3-xl289-critter_world/src/test/resources/examples/example-critter.txt";
        c1 = addCritter(filename1);
    }

    //loading a full world file

    @Test
    void test2() {
        String worldfile = "/Users/szekwokfung/Documents/GitHub/team-cw-gs522-tzz3-xl289-critter_world/src/test/resources/examples/world.txt";
        BufferedReader reader;
        try{
            reader = new BufferedReader(new FileReader(worldfile));
            String line = reader.readLine();
            while(line != null){
                String[] tokens = line.split(" ");
                switch(tokens[0]){
                    case "name": {
                        for (int i = 1; i < tokens.length; i++){
                            w1.name += tokens[i] + " ";
                        }
                        line = reader.readLine();
                        break;
                    }
                    case "size": {
                        w1 = new World(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
                        line = reader.readLine();
                        break;
                    }
                    case "food": {
                        w1.H[Integer.parseInt(tokens[1])][Integer.parseInt(tokens[2])].setFood(-1 * (Integer.parseInt(tokens[3]) + 1));
                        line = reader.readLine();
                        break;
                    }
                    case "rock": {
                        w1.H[Integer.parseInt(tokens[1])][Integer.parseInt(tokens[2])].setRock();
                        line = reader.readLine();
                        break;
                    }
                    case "critter": {
                        Critter cri = addCritter(tokens[1]);
                        cri.setDir(Integer.parseInt(tokens[4]));
                        w1.H[Integer.parseInt(tokens[2])][Integer.parseInt(tokens[3])].setCritter(cri);
                        w1.addCritterLoc(new Location(Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3])));
                        line = reader.readLine();
                        break;
                    }
                    default:
                        line = reader.readLine();
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    //generating a new world

    @Test
    void test3() {
        w2 = new World(Constants.columns, Constants.rows);
        System.out.println(w2.H[0][0].getValue());
        Random rand = new Random();
        int r = rand.nextInt(Constants.columns * Constants.rows) + 1;
        int i = 0;
        while (i < r){
            int col_1 = rand.nextInt(Constants.columns);
            int row_1 = rand.nextInt(Constants.rows);
            if (w2.H[col_1][row_1].isEmpty() && w2.isValid(new Location(col_1, row_1))){
                w2.H[col_1][row_1].setRock();
                i++;
            }
        }
    }

    //stepping a single critter

    @Test
    void test4() {
        w2.execute();

    }

    //stepping multiple critters

    @Test
    void test5() {
        w1.execute();
    }

    //printing the ASCII-art world

    @Test
    void test6() {
        String[][] trans;
        if(w1.getNumCols() % 2 == 0){
            trans = new String[2*(w1.getNumRows() - w1.getNumCols()/2) + 1][w1.getNumCols()];
        }else{
            trans = new String[2*(w1.getNumRows() - w1.getNumCols()/2)][w1.getNumCols()];
        }
        for(int j = 0; j < trans.length; j++){
            for(int k = 0; k < trans[0].length; k++){

                if(w1.getNumCols() % 2 == 1 && ((j % 2 == 0 && k % 2 == 0) || (j % 2 == 1 && k % 2 == 1))){
                    trans[j][k] = " ";
                }else if(w1.getNumCols() % 2 == 0 && ((j % 2 == 0 && k % 2 == 1) || (j % 2 == 1 && k % 2 == 0))){
                    trans[j][k] = " ";
                }else{
                    if(k % 2 == 0){
                        int r = (trans.length - j)/2 + k/2;
                        int c = k;
                        if(c < w1.H.length && r < w1.H[0].length && c >= 0 && r >= 0){
                            if(w1.H[c][r].hasRock()){
                                trans[j][k] = "#" ;
                            }else if(w1.H[c][r].hasFood()){
                                trans[j][k] = "F";
                            }else if(w1.H[c][r].hasCritter()){
                                trans[j][k] = Integer.toString(w1.H[c][r].getCritter().getDir());
                            }else if(w1.H[c][r].getValue() == 0){
                                trans[j][k] = "-";
                            }
                        }else{
                            trans[j][k] = "-";
                        }
                    }else if(k % 2 == 1){
                        int r = (trans.length - j - 1)/2 + (k - 1)/2 + 1;
                        int c = k;
                        if(c < w1.H.length && r < w1.H[0].length && c >= 0 && r >= 0){
                            if(w1.H[c][r].hasRock()){
                                trans[j][k] = "#";
                            }else if(w1.H[c][r].hasFood()){
                                trans[j][k] = "F";
                            }else if(w1.H[c][r].hasCritter()){
                                trans[j][k] = Integer.toString(w1.H[c][r].getCritter().getDir());
                            }else if(w1.H[c][r].getValue() == 0){
                                trans[j][k] = "-";
                            }
                        }else{
                            trans[j][k] = "-";
                        }
                    }
                }
            }
        }
        for (int m = 0; m < trans.length; m++){
            for(int n = 0; n <trans[0].length; n++){
                System.out.print(trans[m][n] + " ");
            }
            System.out.println();
        }


    }

    //Spiral Critter

    @Test
    void test7() {

        String filename = "/Users/szekwokfung/Documents/GitHub/team-cw-gs522-tzz3-xl289-critter_world/src/test/resources/files/A5_Q2.txt";
        c3 = addCritter(filename);
    }

    //add functions
    public static Critter addCritter(String filename){
        BufferedReader reader;
        String name = "";
        ArrayList<Integer> m = new ArrayList<>();
        ArrayList<String> coll = new ArrayList<String>();
        try{
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            while(line != null){
                String[] tokens = line.split(" ");
                switch(tokens[0]){
                    case "species:": {
                        name = tokens[1];
                        line = reader.readLine();
                        break;
                    }
                    case "memsize:": {
                        m.add(Integer.parseInt(tokens[1]));
                        line = reader.readLine();
                        break;
                    }
                    case "defense:": {
                        m.add(Integer.parseInt(tokens[1]));
                        line = reader.readLine();
                        break;
                    }
                    case "offense:": {
                        m.add(Integer.parseInt(tokens[1]));
                        line = reader.readLine();
                        break;
                    }
                    case "size:": {
                        m.add(Integer.parseInt(tokens[1]));
                        line = reader.readLine();
                        break;
                    }
                    case "energy:": {
                        m.add(Integer.parseInt(tokens[1]));
                        m.add(0);
                        m.add(0);
                        line = reader.readLine();
                        break;
                    }
                    case "posture:": {
                        m.add(Integer.parseInt(tokens[1]));
                        line = reader.readLine();
                        break;
                    }
                    default:
                        coll.add(line);
                        line = reader.readLine();
                }

            }
        }catch (IOException e){
            e.printStackTrace();
        }

        StringBuilder buffer = new StringBuilder();
        for(String str : coll){
            buffer.append(str).append("\n");
        }

        BufferedReader br = new BufferedReader(new StringReader(buffer.toString()));
        Reader r = new BufferedReader(br);
        Parser parser = ParserFactory.getParser();
        Program p = parser.parse(r);

        return new Critter(name, p, m);
    }



}
