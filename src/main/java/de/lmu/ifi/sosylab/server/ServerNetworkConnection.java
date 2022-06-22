package de.lmu.ifi.sosylab.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * */
public class ServerNetworkConnection {
  private static final int port = 8080;

  List<User> users;

  private int newestGameNumber = 1;

  /**
   * Initializes the User list, which stores all clients that are currently connected.
   */
  public ServerNetworkConnection() {
    users = new ArrayList<User>();
  }

  /**
   * Start the network-connection such that clients can establish a connection to this server.
   * Starts a Thread, which listens for new connection requests.
   */
  public void start() {

    ServerSocket serverSocket;
    try {
      serverSocket = new ServerSocket(port);
    } catch (IOException e) {
      System.out.println("Cannot create socket with port " + port + ".");
      return;
    }

    Thread acceptThread = new Thread(new Runnable() {
      @Override
      public void run() {
        Socket socket;
        try {
          try {
            while (true) {
              socket = serverSocket.accept();
              // Start thread for every new client
              startHandler(socket);
            }
          } finally {
            serverSocket.close();
          }
        } catch (IOException e) {
          System.out.println(e.getMessage());
        }
      }
    });

    acceptThread.start();
  }

  /**
   * Starts a new thread to handle one client.
   * Listens to incoming messages from the client.
   *
   * @param socket Provides the connection to a new client
   * */
  private void startHandler(Socket socket) {

    Thread newConnectionThread = new Thread() {
      String clientNick = "Not initialized.";

      //
      private boolean keepReading = true;

      @Override
      public void run() {
        try {
          BufferedReader reader = new BufferedReader(
            new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
          OutputStreamWriter writer =
            new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);


          while (keepReading) {

            // Wait for a single message from the client.
            // If readLine is null, means that socket is closed / client disconnected.
            JSONObject jsonObject;
            try {
              String readLine = reader.readLine();
              jsonObject = new JSONObject(readLine);
            } catch (Exception e) {
              keepReading = false;
              break;
            }

            // Get Info out of the message
            String messageType = (String) jsonObject.get("type");

            switch (messageType) {
              case "login":
                clientNick = (String) jsonObject.get("nick");

                boolean nickAlreadyUsed = false;
                for (User user : users) {
                  if (clientNick.equals(user.getName())) {
                    nickAlreadyUsed = true;
                    break;
                  }
                }

                if (nickAlreadyUsed) {
                  JSONObject loginFailedJson = new JSONObject();
                  loginFailedJson.put("type", "login failed");

                  writer.write(loginFailedJson + System.lineSeparator());
                  writer.flush();
                } else {
                  // Inform other users
                  for (User user : users) {
                    if (user.getGameNumber() == newestGameNumber) {
                      JSONObject userJoinedJson = new JSONObject();
                      userJoinedJson.put("type", "user joined");
                      userJoinedJson.put("nick", clientNick);

                      user.getWriter().write(userJoinedJson + System.lineSeparator());
                      user.getWriter().flush();
                    }
                  }

                  // Inform newly logged in user
                  JSONObject loginSuccessJson = new JSONObject();
                  loginSuccessJson.put("type", "login success");
                  loginSuccessJson.put("gameNumber", newestGameNumber);

                  writer.write(loginSuccessJson + System.lineSeparator());
                  writer.flush();

                  // Add user to User list
                  users.add(new User(clientNick, writer, newestGameNumber));

                  //
                  int numberOfUsersInNewestGame = 0;
                  for (User user : users) {
                    if (user.getGameNumber() == newestGameNumber) {
                      numberOfUsersInNewestGame++;
                    }
                  }

                  // Spielzahl erhöhen, wenn ein Spiel voll ist
                  if (numberOfUsersInNewestGame > 3) {
                    newestGameNumber++;
                  }
                }
                break;
              case "move":
                String scheibeOderMitte = (String) jsonObject.get("quelle");

                for (User user : users) {
                  if (user.getName().equals(clientNick)) {
                    continue;
                  }
                  JSONObject postMessageJson = new JSONObject();
                  postMessageJson.put("type", "move");
                  postMessageJson.put("nick", clientNick);
                  postMessageJson.put("content", scheibeOderMitte);

                  user.getWriter().write(postMessageJson + System.lineSeparator());
                  user.getWriter().flush();
                }
                break;
              default: break;
            }
          }
        } catch (IOException | JSONException e) {
          e.printStackTrace();
        } finally {
          // Remove user
          User removeUser = null;
          for (User user : users) {
            if (clientNick.equals(user.getName())) {
              removeUser = user;
              break;
            }
          }
          users.remove(removeUser);

          // Close the socket
          try {
            socket.close();
          } catch (IOException e) {
            e.printStackTrace();
          }

          // Inform other users
          if (!(removeUser == null)) {
            sendUserLeft(removeUser.getName());
          }
        }
      }
    };

    newConnectionThread.start();
  }


  /**
   * Broadcasts that a user has disconnected from the server to all still connected clients.
   *
   * @param clientNick Name of the disconnected user.
   */
  private void sendUserLeft(String clientNick) {
    try {
      for (User user : users) {
        JSONObject postMessageJson = new JSONObject();
        postMessageJson.put("type", "user left");
        postMessageJson.put("nick", clientNick);

        user.getWriter().write(postMessageJson + System.lineSeparator());
        user.getWriter().flush();
      }
    } catch (IOException | JSONException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Stop the network-connection.
   * Unused in this implementation.
   */
  public void stop() {
    // stop connection
  }
}
