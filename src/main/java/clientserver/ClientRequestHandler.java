package clientserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import interpret.Food;
import javafx.scene.control.Alert;
import model.Critter;
import model.World;
import org.eclipse.jetty.io.ssl.ALPNProcessor;
import org.eclipse.jetty.util.log.Log;
import view.Controller;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;

public class ClientRequestHandler {
    private int lastestVersion;
    public String Url;
    Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    public ClientRequestHandler(String url) {Url = url;}


    private String getRespond(BufferedReader r) throws IOException{
        StringBuilder sb = new StringBuilder();
        String s = r.readLine();

        while(s != null){
            sb.append(s + '\n');
            s = r.readLine();
        }

        return sb.toString();
    }

    private Object get(String URIEnding, Class<?> Class){
        URL url;
        HttpURLConnection connection;
        try{
            url = new URL(Url + URIEnding);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            BufferedReader r = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String responseBodyText = getRespond(r);
//            System.out.println(responseBodyText);
            return gson.fromJson(responseBodyText, Class);
        }catch (IOException e){
            return -1;
        }
    }


    //GETs
    public CritterListBundle getCritterList(int session_id){
        String URLEnding = "critters?session_id=" + session_id;
        return (CritterListBundle) get(URLEnding, CritterListBundle.class);
    }

    public CritterBundle getCritter(int session_id, int species_id){
        String URLEnding = "critter/" + species_id + "?session_id=" + session_id;
        return (CritterBundle) get(URLEnding, CritterBundle.class);
    }

    public WorldInfoFull getWorldState(int session_id){
        String URLEnding = "world?session_id=" + session_id;
        return (WorldInfoFull) get(URLEnding, WorldInfoFull.class);
    }

    public WorldInfoFull getWorldState(int updateSince, int sessionId) {
        String URIEnding = "world?update_since=" + updateSince + "&session_id=" + sessionId;
        return (WorldInfoFull) get(URIEnding, WorldInfoFull.class);
    }

    public WorldInfoFull getWorldSubsectionState(int rowMin, int rowMax, int colMin, int colMax, int sessionId) {
        String URIEnding = "world?from_row=" + rowMin
                + "&to_row=" + rowMax
                + "&from_col=" + colMin
                + "&to_col=" + colMax
                + "&session_id=" + sessionId;
        return (WorldInfoFull) get(URIEnding, WorldInfoFull.class);
    }

    public WorldInfoFull getWorldSubsectionState(int updateSince, int rowMin, int rowMax, int colMin, int colMax, int sessionId) {
        String URIEnding = "world?update_since=" + updateSince
                + "&from_row=" + rowMin
                + "&to_row=" + rowMax
                + "&from_col=" + colMin
                + "&to_col=" + colMax
                + "&session_id=" + sessionId;
        return (WorldInfoFull) get(URIEnding, WorldInfoFull.class);
    }


    /**login into the server
     * @param level
     * @param password
     * @return the session id with correct level and password, -1 otherwise
     */
    //POSTs
    public int login(String level, String password) {
        URL url;
        HttpURLConnection connection;
        try {
            url = new URL(Url + "login");
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            PrintWriter w = new PrintWriter(connection.getOutputStream());
            LoginBundle login = new LoginBundle(level, password);
            String loginBody = gson.toJson(login, LoginBundle.class);

            w.print(loginBody);
            w.flush();

            BufferedReader r = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String responseBodyText = getRespond(r);
            SessionBundle responseBody = gson.fromJson(responseBodyText, SessionBundle.class);
            return responseBody.session_id;

        }catch (MalformedURLException e){
            System.out.println("The URL is incorrect");
            return -1;
        }catch (IOException e) {
            System.out.println("Could not connect to the Server");
            return -1;
        }
    }


    /**load a world from the file into the server
     * @param worldfile
     * @param sessionId
     * @return "Ok" from the server if successful, "oops" other wise
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public String loadWorld(String worldfile, int sessionId) throws IllegalArgumentException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(worldfile));
        String description = "";
        String currentLine = br.readLine();
        while (currentLine != null) {
            description += currentLine;
            description += "\r\n";
            currentLine = br.readLine();
        }
        WorldBundle loadWorldInfo = new WorldBundle(description);
        URL url = null;
        HttpURLConnection connection;
        try {
            url = new URL(Url + "world?session_id=" + sessionId);
            // url = new URL("http://localhost:" + 8080 + "/world?session_id=" + sessionId);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            PrintWriter w = new PrintWriter(connection.getOutputStream());
            w.println(gson.toJson(loadWorldInfo, WorldBundle.class));
            w.flush();

            BufferedReader r = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String responseBodyText = getRespond(r);

            return responseBodyText;

        } catch (MalformedURLException e) {
            System.out.println("The URL entered was not correct.");
            return "oops";
        } catch (IOException e) {
            System.out.println("Could not connect to the server");
            return "oops";
        } finally {
            br.close();
        }
    }

    /**load a new world into the server
     * @param sessionId
     * @return "Ok" from the server if successful, "oops" otherwise
     */
    public String loadWorld(int sessionId) {
        String description = "";
        WorldBundle loadWorldInfo = new WorldBundle(description);
        URL url = null;
        HttpURLConnection connection;
        try {
            url = new URL(Url + "world?session_id=" + sessionId);
            // url = new URL("http://localhost:" + 8080 + "/world?session_id=" + sessionId);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            PrintWriter w = new PrintWriter(connection.getOutputStream());
            w.println(gson.toJson(loadWorldInfo, WorldBundle.class));
            w.flush();

            BufferedReader r = new BufferedReader(new InputStreamReader(
                     connection.getInputStream()));
            String responseBodyText = getRespond(r);

            return responseBodyText;

        } catch (MalformedURLException e) {
            System.out.println("The URL entered was not correct.");
            return "oops";
        } catch (IOException e) {
            System.out.println("Could not connect to the server");
            return "oops";
        }
    }


    /**get the subsection of the world
     * @param to_column
     * @param from_column
     * @param to_row
     * @param from_row
     * @param sessionId
     * @return The gson of the world information
     * @throws SocketTimeoutException
     */
    public WorldInfoFull getWorldObjects(int to_column, int from_column, int to_row, int from_row, int sessionId) throws SocketTimeoutException {
        URL url = null;
        WorldInfoFull worldState= null;
        HttpURLConnection connection;
        try {
            url = new URL(Url + "world?session_id=" + sessionId + "&update_since=" + 0 + "&from_row=" + from_row + "&to_row=" + to_row + "&from_col=" + from_column + "&to_col=" + to_column);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            if (connection.getResponseCode() == 401) {
                return null;
            }
            if (connection.getResponseCode() == 403) {
                return null;
            }
            BufferedReader r = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            worldState = gson.fromJson(r, WorldInfoFull.class);
        } catch (MalformedURLException e) {
            System.out.println("The URL entered was not correct.");
        } catch (IOException e) {
            System.out.println("Could not connect to the server");
        }
        return worldState;
    }


    /**load a certain critter into a certain places
     * @param cri
     * @param pos
     * @param session_id
     * @return the information of the critter
     */
    public String addCritter(Critter cri, PositionBundle[] pos, int session_id){
        int[] m = cri.getMem().stream().mapToInt(Integer::intValue).toArray();
        CritterBundle critter = new CritterBundle(cri.name, cri.getProgram().prettyPrint(new StringBuilder()).toString(), m, pos, 0);
        URL url;
        HttpURLConnection connection;
        try {
            url = new URL(Url + "critters?session_id=" + session_id);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            PrintWriter w = new PrintWriter(connection.getOutputStream());
            w.println(gson.toJson(critter, CritterBundle.class));
            w.flush();

            BufferedReader r = new BufferedReader(new InputStreamReader(
                     connection.getInputStream()));
            String responseBodyText = getRespond(r);

            return responseBodyText;
        } catch (MalformedURLException e){
            System.out.println("The URL is incorrect");
        }catch (IOException e) {
            System.out.println("Could not connect to the Server");
        }
        return null;
    }

    /**load critters into random places
     * @param cri
     * @param n
     * @param session_id
     * @return the information of the critters
     */
    public String addCritter(Critter cri, int n, int session_id){
        int[] m = cri.getMem().stream().mapToInt(Integer::intValue).toArray();
        CritterBundle critter = new CritterBundle(cri.name, cri.getProgram().prettyPrint(new StringBuilder()).toString(), m, null, n);
//        System.out.println("bundle: " + cri.getProgram().prettyPrint(new StringBuilder()).toString());
        URL url;
        HttpURLConnection connection;
        try {
            url = new URL(Url + "critters?session_id=" + session_id);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            PrintWriter w = new PrintWriter(connection.getOutputStream());
            w.println(gson.toJson(critter, CritterBundle.class));
            w.flush();


            BufferedReader r = new BufferedReader(new InputStreamReader(
                     connection.getInputStream()));
            String responseBodyText = getRespond(r);

            return responseBodyText;
        } catch (MalformedURLException e){
            System.out.println("The URL is incorrect");
        }catch (IOException e) {
            System.out.println("Could not connect to the Server");
        }
        return null;
    }


    /**add food to a certain location
     * @param row
     * @param col
     * @param session_id
     * @param value
     * @return "Ok" from the server if successful, "oops" otherwise
     */
    public String addFood(int row, int col, int session_id, int value){
        URL url;
        HttpURLConnection connection;
        try {
            url = new URL(Url + "world/create_entity?session_id=" + session_id);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            PrintWriter w = new PrintWriter(connection.getOutputStream());

            ObjectBundle resBody = new ObjectBundle(row, col, "food", value);
            String resBody2 = gson.toJson(resBody, ObjectBundle.class);

            w.println(resBody2);
            w.flush();

            BufferedReader r = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String responseBodyText = getRespond(r);
            return responseBodyText;

        }catch (MalformedURLException e){
            System.out.println("The URL is incorrect");
            return "oops";
        }catch (IOException e) {
            System.out.println("Could not connect to the Server");
            return "oops";
        }
    }


    /**load a rock into a certain location
     * @param row
     * @param col
     * @param session_id
     * @return "Ok" from the server if successful, "oops" otherwise
     */
    public String addRock(int row, int col, int session_id){
        URL url;
        HttpURLConnection connection;
        try {
            url = new URL(Url + "world/create_entity?session_id=" + session_id);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            PrintWriter w = new PrintWriter(connection.getOutputStream());

            ObjectBundle resBody = new ObjectBundle(row, col, "rock", 0);
            String resBody2 = gson.toJson(resBody, ObjectBundle.class);

            w.println(resBody2);
            w.flush();

            BufferedReader r = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String responseBodyText = getRespond(r);
            return responseBodyText;

        }catch (MalformedURLException e){
            System.out.println("The URL is incorrect");
            return "oops";
        }catch (IOException e) {
            System.out.println("Could not connect to the Server");
            return "oops";
        }
    }


    /**advance the world for one step
     * @param sessionId
     * @param count
     * @return the world step bundle
     */
    public String worldStep(int sessionId, int count) {
        WorldStepBundle stepBundle = new WorldStepBundle(count);
        URL url;
        HttpURLConnection connection;
        try {
            url = new URL(Url + "step?session_id=" + sessionId);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true); // send a POST message
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            PrintWriter w = new PrintWriter(connection.getOutputStream());
            w.println(gson.toJson(stepBundle, WorldStepBundle.class));
            w.flush();

            BufferedReader r = new BufferedReader(new InputStreamReader(
                     connection.getInputStream()));
            String responseBodyText = getRespond(r);

            return responseBodyText;
        } catch (MalformedURLException e){
            System.out.println("The URL is incorrect");
        }catch (IOException e) {
            System.out.println("Could not connect to the Server");
        }
        return null;

    }


    /**change the rate of the world
     * @param rate
     * @param session_id
     * @return the rate bundle
     */
    public String changeRate(float rate, int session_id) {
        RunBundle runBundle = new RunBundle(rate);
        URL url;
        HttpURLConnection connection;
        try {
            url = new URL(Url + "run?session_id=" + session_id);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true); // send a POST message
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            PrintWriter w = new PrintWriter(connection.getOutputStream());
            w.println(gson.toJson(runBundle, RunBundle.class));
            w.flush();

            BufferedReader r = new BufferedReader(new InputStreamReader(
                     connection.getInputStream()));
            String responseBodyText = getRespond(r);

            return responseBodyText;
        } catch (MalformedURLException e){
            System.out.println("The URL is incorrect");
        }catch (IOException e) {
            System.out.println("Could not connect to the Server");
        }
        return null;
    }


    /**request the update since
     * @param sessionID
     * @param updateSince
     * @return the world bundle
     */
    public WorldBundle updateSince(int sessionID, int updateSince) {
        URL url;
        HttpURLConnection connection;
        try {
            url = new URL(Url + "world?session_id=" + sessionID + "&update_since=" + updateSince);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Length", "0");
            if (connection.getResponseCode() == 401) {
                return null;
            } else if (connection.getResponseCode() == 403) {
                return null;
            } else if (connection.getResponseCode() == 406) {
                return null;
            }
            BufferedReader r = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            WorldBundle state = gson.fromJson(r, WorldBundle.class);
            return state;

        } catch (MalformedURLException e) {
            System.out.println("The URL entered was not correct.");
            return null;
        } catch (IOException e) {
            System.out.println("Could not connect to the server");
            return null;
        }
    }

    //DELETE


    /**remove the critter from the server
     * @param critter_id
     * @param session_id
     * @return "Ok" from the server if successful, "oops" otherwise
     */
    public String removeCritter(int critter_id, int session_id){
        URL url;
        HttpURLConnection connection;
        try {
            url = new URL(Url + "critter/" + critter_id + "?session_id=" + session_id);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("DELETE");
            connection.setRequestProperty("Content-Type", "application/json");

            BufferedReader r = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String responseBodyText = getRespond(r);
            return responseBodyText;

        }catch (MalformedURLException e){
            System.out.println("The URL is incorrect");
            return "oops";
        }catch (IOException e) {
            System.out.println("Could not connect to the Server");
            return "oops";
        }
    }

}
