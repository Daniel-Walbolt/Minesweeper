package com.codegym.games.minesweeper;

import com.codegym.engine.cell.Color;
import com.codegym.engine.cell.Game;

import java.util.ArrayList;
import java.util.List;

public class MinesweeperGame extends Game {
    private static final int SIDE = 20; // The size of the SQUARE board

    private GameObject[][] gameField = new GameObject[SIDE][SIDE]; // The Matrix that stores all the cell objects

    private int countMinesOnField; // The count of total mines on the field
    private int countFlags; //The count of total flags on the field (used for limiting the amount of flags available)
    private int countClosedTiles = SIDE * SIDE; //The total number of tiles that are closed (dimensions of the board) (used for determining winning conditions)
    private int score = 0;
    private int moveCount = 0; //The move the player is on (used for making sure first move is not a mine)
    private int reveals = 5; // The maximum amount of cells a player can reveal.

    private boolean isShowingMessage = false; // Boolean to store whether or not a message is being displayed to the user
    private boolean isGameStopped; // Boolean to store whether or not the game is stopped. If it is, left click functionality restarts the game instead of revealing a cell.

    private static final String MINE = "\uD83D\uDCA3"; //UTF-16 MINE icon
    private static final String FLAG = "\u2691"; //UTF-16 FLAG icon
    
    /*
    SET UP , INITIATE
     */
    @Override
    public void initialize() {
        setScreenSize(SIDE, SIDE);
        createGame();
    } //Starts the game by overriding a GameEngine method called in its start method.

    /*
    CREATE , START , MAKE NEW , START GAME
     */
    private void createGame()
    {
        /*
        * Creates all the objects on the game field and gives each a unique coordinate
        * Sets the colors of each cell to be the default ORANGE color and their information to be blank.
         */
        for (int y = 0; y < SIDE; y++) {
            for (int x = 0; x < SIDE; x++) {
                boolean isMine = getRandomNumber(10) < 2;
                if (isMine) {
                    countMinesOnField++;
                }
                gameField[y][x] = new GameObject(x, y, isMine);
                setCellColor(x, y, Color.ORANGE);
                setCellValue(x, y, "");
            }
        }

        /*
        * Initiate the values of total count of mines and flags.
        * The player should not be able to place more flags than there are mines
        * Store within each object the total amount of nearby mines (3x3 area) which is used in the openTile method
         */
        countFlags = countMinesOnField;
        countMineNeighbors();

    }

    /*
    GET NEIGHBORS , GET NEARBY , NEARBY , GET SURROUNDING CELLS
     */
    private List<GameObject> getNeighbors(GameObject gameObject)
    {

        /*
        * Returns a list of all the surrounding objects of a specific cell object. Widely used in the openTile method
         */

        List<GameObject> result = new ArrayList<>();
        for (int y = gameObject.y - 1; y <= gameObject.y + 1; y++) {
            for (int x = gameObject.x - 1; x <= gameObject.x + 1; x++) {
                if (y < 0 || y >= SIDE) {
                    continue;
                }
                if (x < 0 || x >= SIDE) {
                    continue;
                }
                if (gameField[y][x] == gameObject) {
                    continue;
                }
                result.add(gameField[y][x]);
            }
        }
        return result;
    } // Returns the surrounding cells of a GameObject (cell object)

    /*
    COUNT NEARBY MINES , GET MINES NEARBY
     */
    private void countMineNeighbors() //Counts the nearby cells of every cell object on the board and assigns a public variable of nearby mined cells
    {

        /*
        * Loops through every game object in the array filled in the createGame() method.
        * Loops through the neighbors of each cell and counts how many of them are mines
        * Stores the value of how many nearby mines within each object's data.
         */
        for(GameObject[] gameObjects : gameField)
        {

            for(GameObject gameObject : gameObjects)
            {

                    int temp = 0;

                    for(GameObject neighbors : getNeighbors(gameObject))
                    {

                        if(neighbors.isMine)
                            temp++;

                    }

                    gameObject.countMineNeighbors = temp;

            }

        }

    }

    /*
    REVEAL , UNCOVER, SHOW CELL , SHOW TILE
     */
    private void openTile(int x, int y) // Opens the tile at the x,y coordinates and handles behavior for the type revealed (mine, blank, or number)
    {

        // Check if the game is NOT stopped, and the tile is NOT flagged, and the tile is NOT already uncovered
        if(!isGameStopped && !gameField[y][x].isFlag && !gameField[y][x].isOpen)
        {

            // Make the cell REVEALED and reduce the amount of closed tiles on the field (part of the win condition)
            gameField[y][x].isOpen = true;
            countClosedTiles--;

            //Increase the move count by 1 (only used for the first move)
            moveCount++;

            //Check for if the cell at the given x,y coordinates is a mine and it is NOT the first move of the player
            if(gameField[y][x].isMine && moveCount > 1)
            {

                    //Set the cell to red and display a mine
                    setCellValueEx(x, y, Color.RED, MINE);

                    //End the game
                    gameOver();

            }
            else // If the cell is NOT a mine OR the player made their FIRST move.
            {

                //Check if it is the first move of the player and the cell is a mine
                if(moveCount == 1 && gameField[y][x].isMine)
                {

                    //Make the mined cell no longer a mine, and recount the mines on the field
                    gameField[y][x].isMine = false;
                    countMineNeighbors();
                    countMinesOnField--;

                }

                //Add 5 points every time a cell is revealed that isn't a mine and display it to the user.
                score += 5;
                setScore(score);

                //Check if the cell revealed has any mined neighbors
                if(gameField[y][x].countMineNeighbors > 0)
                {

                    //Set the number on the cell equal to the number of the surrounding mines and make it GREEN
                    setCellNumber(x, y, gameField[y][x].countMineNeighbors);
                    setCellColor(x, y, Color.GREEN);

                }
                else
                {

                    //Set the text box on the cell to nothing and make it GREEN
                    setCellValue(x, y, "");
                    setCellColor(x, y, Color.GREEN);

                    //Loop through the neighboring cells and recall this method if the cell is not already opened.
                    for(GameObject neighbor : getNeighbors(gameField[y][x]))
                    {

                        if(!neighbor.isOpen)
                            openTile(neighbor.x, neighbor.y);

                    }

                }

                //After every call of this method, check if the amount of tiles not opened are equal to the number of mines. If it is, the player wins.
                if(countClosedTiles == countMinesOnField)
                    win();

            }

        }

    }

    /*
    MOUSELEFT , MOUSE LEFT , LEFT CLICK , MOUSE EVENT
     */
    @Override
    public void onMouseLeftClick(int a, int b) // Even override from the Game Engine assigning left click functionality
    {

        //Check if the game is stopped
        if(isGameStopped)
        {
            restart();
        }
        //Check if the cell clicked on is not a flag and is not open
        else if(!gameField[b][a].isFlag && !gameField[b][a].isOpen)
            openTile(a, b); //Opening the tile that was clicked.
        else //Reveal feature works when a player left clicks a flag
            reveal(a, b);

    }

    /*
    MOUSERIGHT , MOUSE RIGHT , RIGHT CLICK , MOUSE EVENT
     */
    @Override
    public void onMouseRightClick(int a, int b) //Event override from the Game Engine assigning right click function
    {

        //When right clicking a cell, mark it.
        markTile(a, b);

    }

    /*
    FLAG , MARK , YELLOW
     */
    private void markTile(int x, int y) // Marks a cell with a UTF-16 Flag when called with x,y coordinates
    {

        //Check if the game is stopped, if it is, do nothing.
        if(!isGameStopped)
        {

            //If the cell is open, don't do anything
            if(gameField[y][x].isOpen)
                return;
            //Check if the cell is already a flag and the number of flags available is greater than 0
            else if(!gameField[y][x].isFlag && countFlags > 0)
            {

                //Set the object state to a flag and reduce the amount of available flags
                gameField[y][x].isFlag = true;
                countFlags--;

                //Set the cell to a UTF-16 Flag and make the color YELLOW
                setCellValue(x, y, FLAG);
                setCellColor(x, y, Color.YELLOW);

            }
            //If the tile is already a flag
            else if(gameField[y][x].isFlag)
            {

                //Set the state of the object to being an unrevealed cell and increase the amount of available flags
                gameField[y][x].isFlag = false;
                countFlags++;

                //Reset the color of the cell to default ORANGE
                setCellValue(x, y, "");
                setCellColor(x, y, Color.ORANGE);

            }

        }

    }

    /*
    END , LOSE , GAME OVER
     */
    private void gameOver() //Ends the game and shows "Game Over" message when called
    {

        isGameStopped = true;
        //Displays a red "Game Over" message to the player
        showMessageDialog(Color.LIGHTGRAY, "Game Over", Color.RED, 30);

    }

    /*
    WIN , VICTORY , COMPLETE
     */
    private void win()
    {

        isGameStopped = true;
        //Displays a blue "You Win!" message to the player
        showMessageDialog(Color.LIGHTGRAY, "You Win!", Color.BLUE, 30);

    }

    /*
    RESTART , RESET, START OVER
     */
    private void restart() // Restarts the game, setting default values to default.
    {

        //Reset all variables to their default values and create a new game using createGame()
        isGameStopped = false;

        countClosedTiles = SIDE*SIDE;
        countMinesOnField = 0;
        moveCount = 0;
        reveals = 5;

        score = 0;
        setScore(score);

        createGame();

    }

    /*
    SHOW HIDDEN , FLAG UNCOVER , REVEALS
     */
    private void reveal(int x, int y) //Reveals the type of cell hidden underneath a flag.
    {

        //If the game is displaying a message to the player, make the message false (behavior of the Game class) and do nothing
        if(isShowingMessage)
        {
            isShowingMessage = false;
            return;
        }
        // If the game is not displaying a message and the number of remaining reveals is greater than 0
        else if(reveals > 0)
        {

            //Reduce the amount of available reveals by one
            reveals--;

            /*
            * Display a message that depends on whether or not a marked cell is a mine
             */
            if(gameField[y][x].isMine)
                showMessageDialog(Color.WHITE, "MINE - Reveals : " + reveals, Color.RED, 15);
            else
                showMessageDialog(Color.WHITE, "CLEAR - Reveals : " + reveals, Color.BLUE, 15);

            // The game is now displaying a message
            isShowingMessage = true;

        }
        /*
        * Tell the player they have no more reveals available
         */
        else
        {
            showMessageDialog(Color.WHITE, "No More Reveals", Color.RED, 15);
            isShowingMessage = true;
        }

    }

}