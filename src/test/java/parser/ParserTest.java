package parser;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.*;

import org.junit.jupiter.api.Test;

import ast.Program;
import parse.Parser;
import parse.ParserFactory;

/**
 * This class contains tests for the Critter parser.
 */
public class ParserTest {

    /** Checks that a valid critter program is not {@code null} when parsed. */
    @Test
    public void testProgramIsNotNull() {
        InputStream in = ClassLoader.getSystemResourceAsStream("files/example-rules.txt");
        Reader r = new BufferedReader(new InputStreamReader(in));
        Parser parser = ParserFactory.getParser();
        Program prog = parser.parse(r);
        assertNotNull(prog, "A valid critter program should not be null.");
        System.out.print(prog.prettyPrint(new StringBuilder()));

        StringBuilder a = prog.prettyPrint(new StringBuilder());
        Parser parser2 = ParserFactory.getParser();
        Program prog2 = parser2.parse(new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("files/test"))));
        System.out.print(prog2.prettyPrint(new StringBuilder()));
    }


}
