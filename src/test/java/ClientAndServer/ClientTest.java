package ClientAndServer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ArrayList;

import ast.Program;
import clientserver.ClientRequestHandler;
import clientserver.PositionBundle;
import model.Critter;
import model.World;
import org.junit.jupiter.api.Test;
import parse.Parser;
import parse.ParserFactory;

import static org.junit.jupiter.api.Assertions.*;

public class ClientTest {

    private ClientRequestHandler testHandler = new ClientRequestHandler("http://35.199.36.178:8080/");

    @Test
    public void Login(){
        //using the provided server with correct level and password
        int session_id1 = testHandler.login("admin","gandalf");
        assertTrue(session_id1 != -1);
        int session_id2 = testHandler.login("read","bilbo");
        assertTrue(session_id2 != -1);
        int session_id3 = testHandler.login("write","frodo");
        assertTrue(session_id3 != -1);

        //using the provided server with correct level and password, and the respond is "Could not connect to the Server".
        int session_id4 = testHandler.login("admin","wrong");
        assertTrue(session_id4 == -1);
        int session_id5 = testHandler.login("read","wrong");
        assertTrue(session_id5 == -1);
        int session_id6 = testHandler.login("write","wrong");
        assertTrue(session_id6 == -1);

    }

    @Test
    public void AddNewWorld(){
        int session_id1 = testHandler.login("admin","gandalf");
        assertTrue(session_id1 != -1);

        String file1 = "/Users/szekwokfung/Documents/GitHub/team-cw-gs522-tzz3-xl289-critter_world/src/test/resources/examples/world.txt";
        try{
            String respond = testHandler.loadWorld(file1,session_id1);
            assertTrue(respond.equals("Ok\n"));

        }catch (IOException e){
            e.getMessage();
        }

        //tests for add food and rocks
        String respond2 = testHandler.addFood(0,0,session_id1,1000);
        assertTrue(respond2.equals("Ok\n"));

        String respond3 = testHandler.addRock(1,1,session_id1);
        assertTrue(respond3.equals("Ok\n"));

        Critter cri = addCritter("/Users/szekwokfung/Documents/GitHub/team-cw-gs522-tzz3-xl289-critter_world/src/test/resources/examples/example-critter.txt");
        assertTrue(cri.name.equals("example"));

        //randomly add critters
        String respond5 = testHandler.addCritter(cri, 5, session_id1);
        System.out.println(respond5);

        //more tests for adding rocks and food
        String respond6 = testHandler.addFood(2,1,session_id1,1000);
        assertTrue(respond6.equals("Ok\n"));

        String respond7 = testHandler.addRock(4,5,session_id1);
        assertTrue(respond7.equals("Ok\n"));

        //test for worldstep
        String respond8 = testHandler.worldStep(session_id1, 5);
        System.out.println("step: " + respond8);

        //TODO
        testHandler.removeCritter(cri.id,session_id1);

        System.out.println("2");
        testHandler.getWorldState(session_id1);
        System.out.println("3");
        testHandler.getWorldState(2, session_id1);
        System.out.println("4");
        testHandler.changeRate((float) 10.000, session_id1);

        //TODO this is problematic
        PositionBundle[] pos = new PositionBundle[1];
        pos[0] = new PositionBundle(5, 6, cri.getDir());

        String respond4 = testHandler.addCritter(cri, pos, session_id1);
        System.out.println("a " + respond4);

    }

    public Critter addCritter(String filename){
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
        Program p;
        try {
            p = parser.parse(r);
        }catch (Throwable error){
            return null;
        }

        return new Critter(name, p, m);
    }
}