package main;

import ast.Program;
import parse.Parser;
import parse.ParserFactory;

import java.io.*;

public class ParseAndMutateApp {

   public static void main(String[] args) {
      int n = 0;
      String file = null;
//      System.out.println(args[0]);
      try {
         if (args.length == 1) {
            file = args[0];
         } else if (args.length == 3 && args[0].equals("--mutate")) {
            n = Integer.parseInt(args[1]);
            if (n < 0) throw new IllegalArgumentException();
            file = args[2];
         } else {
            throw new IllegalArgumentException();
         }
         InputStream in = null;
         try {
            in = new FileInputStream(file);
         } catch (FileNotFoundException e) {
            System.out.println("File not found.");
         }
         Reader r = new BufferedReader(new InputStreamReader(in));
         Parser parser = ParserFactory.getParser();
         Program prog = parser.parse(r);
         if (n == 0) {
            System.out.print(prog.prettyPrint(new StringBuilder()));
         }
         else {
            for (int i = 0; i < n; i ++) {
               prog.mutate();
               System.out.print(prog.prettyPrint(new StringBuilder()));
            }
         }

         throw new IllegalArgumentException();

      } catch (IllegalArgumentException e) {
         System.out.println("Usage:\n  <input_file>\n  --mutate <n> <input_file>");
      }
   }
   
}
