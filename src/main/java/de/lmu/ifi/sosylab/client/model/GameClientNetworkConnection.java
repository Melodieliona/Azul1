package de.lmu.ifi.sosylab.client.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import static java.util.Objects.requireNonNull;

public class GameClientNetworkConnection {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    // TODO: insert code here

    private Socket connection;

    private GameModel model;

    private String nickname = null;

    /**
     * Constructor of the class.
     *
     * @param model the model
     */
    public GameClientNetworkConnection(GameModel model) {
        // TODO: insert code here
        this.model = requireNonNull(model);
    }


    /**
     * Start the network connection.
     */
    public void start() {
        // TODO: insert code here
        try {
            connection = new Socket(HOST, PORT);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        Thread connectorThread =
                new Thread(this::receivingThread);
        connectorThread.setDaemon(true);
        connectorThread.start();
    }


    /**
     * Stop the network-connection.
     */
    public void stop() {
        // TODO: insert code here
        JSONObject disconnect = new JSONObject();
        try {
            disconnect.put("type", "disconnect");
            send(disconnect);
            connection.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * Send a login-request to the server.
     *
     * @param nickname The name of the user that requests to log in.
     */
    public void sendLogin(String nickname) {
        // TODO: insert code here
        this.nickname = nickname;
        JSONObject jsonNickname = new JSONObject();
        JSONObject jsonLoginStatus;
        String loginStatus = null;
        try {
            jsonNickname.put("type", "login");
            jsonNickname.put("nick", nickname);
        } catch (JSONException e) {
            System.out.println(e.getMessage());
        }
        send(jsonNickname);
        jsonLoginStatus = receive();
        try {
            loginStatus = jsonLoginStatus.getString("type");
        } catch (JSONException e) {
            System.out.println(e.getMessage());
        }
        if (loginStatus != null && loginStatus.equals("login success")) {
            model.loggedIn();
        } else {
            model.loginFailed();
        }
    }

    /**
     * Send a chat message to the server.
     *
     * @param chatMessage The {@link UserTextMessage} containing the message of the user.
     */
    public void sendMessage(UserTextMessage chatMessage) {
        // TODO: insert code here
        JSONObject jsonMessage = new JSONObject();
        try {
            jsonMessage.put("type", "post message");
            jsonMessage.put("content", chatMessage.getContent());
        } catch (JSONException e) {
            System.out.println(e.getMessage());
        }
        send(jsonMessage);
    }

    /**
     * Sends an arbitrary JSONObject.
     *
     * @param messageToSend JSONObject to be sent
     */
    private void send(JSONObject messageToSend) {
        try {
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8));
            writer.write(messageToSend + System.lineSeparator());
            writer.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Receives an arbitrary JSONObject.
     *
     * @return the JSONObject
     */
    private JSONObject receive() {
        JSONObject jsonMessage = null;
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            jsonMessage = new JSONObject(reader.readLine());

        } catch (IOException | JSONException e) {
            System.out.println(e.getMessage());
        }

        return jsonMessage;
    }

    /**
     * Thread that looks at the input stream for oncoming messages.
     */
    private void receivingThread() {
        while (true) {
            Thread thread = new Thread(() -> {
                try {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                    JSONObject jsonMessage;
                    String readMessage;
                    while (reader.ready()) {
                        readMessage = reader.readLine();
                        jsonMessage = new JSONObject(readMessage);
                        String type = jsonMessage.getString("type");
                        String nick = jsonMessage.getString("nick");
                        switch (type) {
                            case ("user joined") -> {
                                model.userJoined(nick);
                                break;
                            }
                            case ("message") -> {
                                Date date = convertStringToDate(jsonMessage.getString("time"));
                                model.addTextMessage(nick, date, jsonMessage.getString("content"));
                                break;
                            }
                            case ("user left") -> {
                                System.out.println(jsonMessage);
                                model.userLeft(nick);
                                if (nick.equals(this.nickname)) {
                                    return;
                                }
                                break;
                            }
                            default -> {
                                //
                            }
                        }
                    }
                } catch (JSONException | IOException | ParseException e) {
                    System.out.println(e.getMessage());
                }
            });
            thread.start();
        }
    }

    /**
     * Converts string to date.
     *
     * @param date string to be converted
     * @return date
     * @throws ParseException if string cannot be parsed
     */
    private static Date convertStringToDate(String date) throws ParseException {
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.GERMANY)
                .parse(date);
    }

}
