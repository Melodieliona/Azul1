# JSON Messages

Each of the JSON messages must be separated by a single newline character (\n). The messages need to be encoded in UTF-8. The general format of an individual messages is
{ "type" : "\<message-type\>", " \<further fields\>" }
Names that are surrounded by angle brackets represent placeholder fields. The messages are described below with the corresponding functionality.

Description of JSON messages in chronological order:

As soon as the user has decided to play on online multiplayer mode the connection with the server will be established and the user will be prompted with a login screen. When the user has chosen a nickname a JSON message is sent from client to server as a login request. This message looks like this:

 { "type" : "login”, “nick” : “\<nickname\>” }

If the nickname is available for the pertinent game the server will send a login success message, if not, a login failed is sent. The messages look like this:

{ "type" : "login success" }
{ "type" : "login failed" }

As soon as a user has joined a JSON message informing all other players present in the current lobby that a user joined. The message looks like this:

{ "type" : "user joined", "nick" : "\<nickname\>" }

At the start of the game various JSON messages shall be sent to each player, these messages contain all relevant information for the game. 

A message indicating the client to fill the factory plates looking like this:

{ "type" : "fill plates", "color" : "\<color\> \<more colors of tiles in same plate\>, \<color of tiles in other plate\> \<more colors of tiles in same plate\>, …”, “tiles” : “\<number of tiles of first color\> \<number of tiles of more colors in same plate\>, … “ }

This message includes all colors of tiles in a given plate separated by white spaces, colors referring to tiles in other plates are separated from the others by commas. It also includes all amounts of tiles of a given color in a given plate separated by white spaces, amounts referring to tiles and colors in other plates are separated by commas. An example for a message in a game with just 2 plates would look like this: 

{ "type" : "fill plates", "color" : "red yellow, black green blue”, “tiles” : “ 3 1, 1 1 2“ }

This indicates that there are two plates; the first one has 4 tiles, 3 red and 1 yellow, the second one also has 4 tiles, 1 black, 1 green and 2 blue. 

The server shall also send the client the score of all players so the client can possibly initialize these values. The message looks like this:

{ "type" : "points", "nick" : "\<nickname 1\> <\nickname 2\> \<further nicknames\>”, “scores” : “\<user 1 score\> \<user 2 score\> \<further scores\>“ }

Different nicknames are separated by white spaces and the order is important given that the first score in the scores field will be assigned to the first user in the nick field and so on. 

At any point a user may disconnect. If this happens the server will send the following message to all remaining users: 

{ "type" : "user left", "nick" : "\<nickname\>” }

At any point a restart of the game may be requested. For this to happen the server expects a message looking like this to be sent from the client:

{ "type" : "game restart request" }
Each time a restart is requested the game shall send this message to other players to allow them to vote on the restart of the game. The message looks like this:

{ "type" : "game restart request", “nick” : “\<nickname\>” }

If enough restarts are requested the server will approve the restart and send following message to all players: 

{ "type" : "game restart" }

The server will also send all users which player shall start with the following message:

{ "type" : "next turn", "nick" : "\<nickname\>” }

During a player’s turn the server will send the player which tiles he is allowed to select with the following message: 

{ "type" : "allowed tiles", "plates" : "\<plate number\> \<further plates\>” }

The message indicates that the tiles in said plates are a valid selection and should be available to the player. The number of each tile is separated by a whitespace.  Plate 0 are tiles that “fell” from the factory plates.

When a player has clicked on a selectable tile the server will be waiting for a message from the client indicating what selection the user has made. The message should look like this: 

{ "type" : "tile selection", "plate" : "\<plate\>”, “color” : “\<color\>”  }

This message indicates the plate and the color of the tiles that have been selected.

After a successful selection the server will inform all players of this via following message:

{ "type" : "tile selection", "plate" : "\<plate\>”, “color” : “\<color\>”  }

As seen, the message is the same, each client should be aware of which player’s turn it is given that the “next turn” message was sent at the start of the turn.

After a selection, the player shall be prompted to select (a) field(s) to place the tile(s). For this purpose the server will send a message to the user informing which fields are allowed. The message looks like this:

{ "type" : "allowed fields", "row" : "\<row number\> \<further rows\>” }

These rows allow the placement of tiles in them, each row number is separated by a whitespace. The top row containing only 1 field is the 0 and the row of the minus points is the 6.

The server will then wait for a message containing an allowed placement of the tiles. The message should look like this:

{ "type" : "tile placement", "row" : "\<row number\>” }

Since the server already knows how many tiles it only needs to know in which row to then calculate how many go into that row and eventually how many into the minus points row. 

The server will then send the tile placement to all players with following message:

{ "type" : "tile placement", "row" : "\<row number\> \<further rows\>”, “column” : “\<column number\> \<further columns\>” }

Each client can now know where each of the selected tiles must go. An example of a message like this would be:

{ "type" : "tile placement", "row" : "0 6 6”, “column” : “0 0 1” }

This would mean that 3 tiles were selected and the player has elected to place one of them in the first row, which only has one column and the other two tiles will be placed in the minus points rows in the first and second columns.

After this the “next round” message shall be sent again from the server.

This process is repeated until there are no more tiles that can be selected. At which point the server will send multiple messages again. The server shall send a “points” message again to every player. This time with all accumulated points up until the end of the current round. If the game hasn’t ended a message indicating the plates to be filled will be sent to all users. 
Up to 4 “update board” messages may be sent to all players depending on the number of them, containing information regarding the placement of tiles in the pattern and the clearing of the rows that need to be cleared (because maybe a row was full and a tile has been placed in the pattern). The message looks like this:

{ "type" : "board update", "nick" : "\<nickname\>”, “row” : “\<row number\> \<further rows\>”, “pattern rows” : “\<pattern row number\> \<further rows\>”, “pattern columns” : “\<pattern column number\> \<further columns\>” }

The nickname of the board’s player is sent, as well as the rows that should be cleared in the field “row”, and the rows and columns of the pattern where a tile is to be placed. Naturally, the same rows that are cleared will be the rows that receive a tile in the pattern. The same reasoning regarding the order of the coordinates applies: the first number of rows corresponds to the first number of columns and so on. 

If the game has ended then the server will inform each player through following message:

{ "type" : "game ended" }

At any point a move that is not allowed may be sent to the server. For example, the server could get a valid JSON message with a format specified on this document, but if the timing is not right (for example it is the turn of another player) the server will send a “move not allowed” message to said player; the same goes for any JSON format not adhering to the formats specified here. The message looks like this.

{ “type” : “move not allowed”, “content”: “\<reason for the message\>” }     


## Dependencies

We recommend Java 17 for this project.
It was tested on Windows 10 and macOS with openjdk-17.0.3,
so no other guarantees can be made.
