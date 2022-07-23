# Protocol

The program follows the client/server paradigma for the implementation of the game. The JSON protocl was used to communicate between Server and Client. Following the JSON messages exchanged between server and client will be listed and discussed in chronological order. 

## JSON Messages

Each of the JSON messages must be separated by a single newline character (\n). The messages need to be encoded in UTF-8. The general format of an individual messages is
{ "type" : "\<message-type\>", " \<further fields\>" }
Names that are surrounded by angle brackets represent placeholder fields. The messages are described below with the corresponding functionality.

Description of JSON messages in chronological order:

As soon as the user has decided to play on online multiplayer mode the connection with the server will be established and the user will be prompted with a login screen. When the user has chosen a nickname a JSON message is sent from client to server as a login request. This message looks like this:

 { "type" : "login”, “nick” : “\<nickname\>” }

If the nickname is available for the pertinent game the server will send a login success message, if not, a login failed is sent. The messages look like this:

{ "type" : "login success", "amount" : <amount of waiting players>, "nick" : "\<\[nicknames\]\>" }

The login success message includes the amount of players currently waiting in the lobby (as int) and their nicknames preceded by a "\[" and succeded by a "\]".

{ "type" : "login failed" }

As soon as a user has joined a JSON message informing all other players present in the current lobby that a user joined. The message looks like this:

{ "type" : "user joined", "nick" : "\<nickname\>" }

The player(s) will now wait up to 4 minutes for the lobby fo fill, if not it will start a minute after the last player (for a minimum of 2 players) has joined. For this the user will received following message:

{ "type" : "timer start" }

When the timer has ended the server will send following message:

{ "type" : "timer end" }

At the start of the game a message indicating the client to fill the factory plates looking like this will be sent:

{ "type" : "fill plates", "color" : "\<color\> \<more colors of tiles in same plate\>, \<color of tiles in other plate\> \<more colors of tiles in same plate\>, …”, “tiles” : “\<number of tiles of first color\> \<number of tiles of more colors in same plate\>, … “ }

This message includes all colors of tiles in a given plate separated by white spaces, colors referring to tiles in other plates are separated from the others by commas. It also includes all amounts of tiles of a given color in a given plate separated by white spaces, amounts referring to tiles and colors in other plates are separated by commas. An example for a message in a game with just 2 plates would look like this: 

{ "type" : "fill plates", "color" : "start_marker ,red yellow, black green blue”, “tiles” : “1, 3 1, 1 1 2“ }

This indicates that there are two plates; the first one has 4 tiles, 3 red and 1 yellow, the second one also has 4 tiles, 1 black, 1 green and 2 blue. As well as that the starting marker must be added to the factory floor. The first element in this data structure. 

At any point a cancelation of the game may be requested. For this to happen the server expects a message looking like this to be sent from the client:

{ "type" : "game cancel request", “nick” : “\<nickname\>”  }
Each time a cancelation is requested the game shall send this message to other players to allow them to vote on the cancelation of the game. The message looks like this:

{ "type" : "game cancel request", “nick” : “\<nickname\>” }

If enough cancelations are requested the server will approve the restart and send following message to all players. The minimum amount of requests to accept the cancelation is 1 less than the current players in the game. The number of maximum request per user is 1 per round and each round the amount of total requests is set to 0, so at least 3 players must agree in a given round to cancel: 

{ "type" : "game cancel" }

At any point a restart of the game may be requested. For this to happen the server expects a message looking like this to be sent from the client:

{ "type" : "game restart request", “nick” : “\<nickname\>” }
Each time a restart is requested the game shall send this message to other players to allow them to vote on the restart of the game. The message looks like this:

{ "type" : "game restart request", “nick” : “\<nickname\>” }

If enough restarts are requested the server will approve the restart and send following message to all players. The minimum amount of requests to accept the restart is 1 less than the current players in the game. The number of maximum request per user is 1 per round and each round the amount of total requests is set to 0, so at least 3 players must agree in a given round to restart: 

{ "type" : "game restart" }


At any point a user may disconnect. If this happens the server will send the following message to all remaining users: 

{ "type" : "user left", "nick" : "\<nickname\>” }

At which point for sake of simplicity and the essence of the game it will be canceld. So the "game cancel" message will be sent to all players still in game.

The server will also send all users which player shall start with the following message:

{ "type" : "next turn", "nick" : "\<nickname\>” }

When a player has clicked on a selectable tile the server will be waiting for a message from the client indicating what selection the user has made. The message should look like this: 

{ "type" : "tile selection", "plate" : "\<plate\>”, “color” : “\<color\>”  }

This message indicates the plate and the color of the tiles that have been selected.

After a successful selection the server will inform all players of this via following message:

{ "type" : "tile selection", "plate" : "\<plate\>”, “color” : “\<color\>”, "amount" : \<amount\>  }

As seen, the message is the same but for the extra field "amount", which contains the amount of tiles as int, each client should be aware of which player’s turn it is given that the “next turn” message was sent at the start of the turn.

If the selection is not successful, because it was tried to select not allowed tiles, following message will be sent:

{ "type" : "tiles not allowed" }

After a selection, the player shall be prompted to select (a) field(s) to place the tile(s). For this purpose the server will send a message to the user informing which fields are allowed. The message looks like this:

{ "type" : "allowed fields", "row" : "\<row number\> \<further rows\>” }

These rows allow the placement of tiles in them, each row number is separated by a whitespace. The top row containing only 1 field is the 0 and the row of the minus points is the 6.

The server will then wait for a message containing an allowed placement of the tiles. The message should look like this:

{ "type" : "tile placement", "row" : "\<row number\>” }

Since the server already knows how many tiles it only needs to know in which row to then calculate how many go into that row and eventually how many into the minus points row. 

If the placement is not successful, because it was tried to place the tiles in not allowed rows, following message will be sent:

{ "type" : "move not allowed" }

The server will then send the tile placement to all players with following message:

{ "type" : "tile placement", "nick" : "\<nickname\>", "color" : "\<color of tiles\>","amount" : \<amount of tiles\> ,"row" : "\<row number\>”}

At this point the server will also send a "floor line update message" in case the placement has caused for tiles to be placed in the 6th row, the minus points row. This message looks like this:

{ "type" : "floor line update", "nick" : "\<nickname\>", "amount" : \<amount of tiles\> , "floortile0" : "\<color\>”, "\<further floortilen fields\>"}

Here it is important to explain the floortile fields. The amount of the fields in this message is variable, starting at 4 and up to 10 depenging on how many tiles where added to this row. It is formated like so: 

Formats tiles like this: {.."floortile0": "BLUE", "floortile1": "RED"..}
Index is relative to the newly added tiles ~ floortile0 is not the first tile on the floor line, but the first tile to be added to it now. If the starting marker was added, it will be "floortile0"

After this the “next turn” message shall be sent again from the server.

This process is repeated until there are no more tiles that can be selected. At which point the server will send multiple messages again. If the game hasn’t ended a message indicating the plates to be filled will be sent to all users. Up to 4 “update board” messages may be sent to all players depending on the number of them, containing information regarding the placement of tiles in the pattern and the clearing of the rows that need to be cleared (because maybe a row was full and a tile has been placed in the pattern). As well as the score of the given player. The message looks like this:

{ "type" : "board update", "nick" : "\<nickname\>”, “row” : “\<\[row number, further rows\]\>”, “color” : “\<\[color (or 0)\> \<further colors(or 0)\>, \<color (or 0)\> \<further colors(or 0)\>, \<colors of tiles of further or 0\]\>”, “points” : “\<current score\>” }

The nickname of the board’s player is sent, as well as the rows that should be cleared in the field “row”, these rows are the same which now contain a tile in the pattern wall. It is important here to explain the colors. The state of the whole pattern is always sent in this message. For example, an empty wall would look like this: 

{..., "color" : "\[0 0 0 0 0 , 0 0 0 0 0, 0 0 0 0 0, 0 0 0 0 0, 0 0 0 0 0\]"}

 which means that in the 25 spaces for tiles noone is placed. Each row of the pattern is separated by a comma, each tile is separated by a white space. If there were some tiles in the pattern, the message would looke like this: 

{..., "color" : "\[blue 0 red green 0 , 0 0 0 red green, 0 0 0 0 0, 0 0 0 0 0, 0 0 0 white blue\]"}

If the game has ended then the server will inform each player through following message:

{ "type" : "game ended", "winners" : "\<nickname of winner\>, \<further nicknames\>", "points" : "\<score of player 1\>, \<score of player 2\>, \<further scores\>", "nick" : "\<nickname of player 1\>, \<nickname of player 2\>, \<further nicknames\>"  }

## Local Server

The client creates a local server to establish a connection and be able to play the game in hot seat mode. The communication with this server is not much different from the one stated above. But it should be noted that the there is no timer and the minimum amount of requests for restart/cancelation now is 1. 

Also, the local server must know beforhand how many players will be joining, so before the names and therefor the "log in" messages are sent, the client sends the server how many players will be in the game through following message: 

{ "type" : "players", "amount" : "\<amountof players\>" }

