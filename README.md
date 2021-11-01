# CSCI201Project_Connect4
Team: Dylan Drain, Maia Cho, Matthew Rosenthal, Anh Nguyen, Yoon Jung (YJ) Lee, Saleem Bekkali

Please commit only **final code** to **master** branch

## Connect4 main working code (src):
- Servermain: creates GameBoard, threads for each Player
  - saving login data (user, pass): SQL database
  - Boolean findPlayer(string name)
    - available if registered == true
- Clientmain: each Player, implements log in, customization selections, lobby search (by Player name)
  - Prompt login, pulls data or makes new one
  - Invites other players to game
- GameBoard: time limit condition for Player's turns, checks for winner, void UpdateBoard()
  - Integer isValid() returns lowest valid row
- Player: default values if not registered, reassigned in constructor if registered, Boolean registered (for findPlayer()), preferred token (default for unregistered)
  - Boolean insert(int col) {false if GameBoard.isValid() returns out of bounds value}
    - gets lock
    - set value of board[isValid()][col] = player.token;
    - board.update()
    - release lock
  - Boolean isPlaying() for request to play
  - void run()
    - while(true) -> if(condition notified)
      - prompt insert where: insert(scanner.getLine())    //scanner for now, front end later
      - if(winner) notify other player (or notifyall), print winner, break;
- Token: appearance

## Front end (no folder yet):
- ?

## Customizations (sprites):
- Tokens
- Board?
