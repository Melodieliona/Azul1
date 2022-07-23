## Dependencies

We recommend Java 17 for this project.
It was tested on Windows 10 and macOS with openjdk-17.0.3,
so no other guarantees can be made.

## How to compile

Create 2 separate Jar files for the client and the remote server.
IntelliJ -> Project Structure -> Artifacts -> Add -> Jar -> "from modules with dependencies" 
-> Select Modules: "main"
-> Select Main Class: "GameClient" and "GameServer" respectively
-> "OK" -> Tick "Include in project build"

Then build the project. Compiled Jar files are found under team09 > out > artifacts .

## How to start

Open (double click) the Azul_Client.jar without any more arguments to start the client.
For multiplayer mode, the server needs to be started previously by opening (double click) the Azul_Server.jar .
