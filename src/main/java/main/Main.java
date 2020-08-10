package main;

import clientserver.Server;
import javafx.application.Application;

public class Main {
   public static void main(String[] args) {
      try {
         if (args.length > 0) {
            String writePassword = null;
            String readPassword = null;
            String adminPassword = null;
            int port = Integer.parseInt(args[0]);
            readPassword = args[1];
            writePassword = args[2];
            adminPassword = args[3];
            Server server = new Server(readPassword, writePassword, adminPassword, port);
            server.run();
         } else {
            Application.launch(view.Main.class, args);
         }
      } catch (Exception e){
         System.out.println("Usage: java -jar critterworld.jar <[port] [read password] [write password] [admin password] | no args>");
      }
   }
}