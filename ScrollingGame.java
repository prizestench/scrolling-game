import java.awt.*;
import java.awt.event.*;
import java.util.*;

//A Simple version of the scrolling game, featuring Avoids, Gets, and RareGets
//Players must reach a score threshold to win
//If player runs out of HP (via too many Avoid collisions) they lose
public class ScrollingGame extends GameEngine {

    // Starting Player coordinates
    protected static final int STARTING_PLAYER_X = 0;
    protected static final int STARTING_PLAYER_Y = 100;

    // Score needed to win the game
    protected static final int SCORE_TO_WIN = 500;

    // Maximum that the game speed can be increased to
    // (a percentage, ex: a value of 300 = 300% speed, or 3x regular speed)
    protected static final int MAX_GAME_SPEED = 300;
    // Interval that the speed changes when pressing speed up/down keys
    protected static final int SPEED_CHANGE_INTERVAL = 20;

    public static final String INTRO_SPLASH_FILE = "game_assets/splash.gif";
    // Key pressed to advance past the splash screen
    public static final int ADVANCE_SPLASH_KEY = KeyEvent.VK_ENTER;

    // Interval that Entities get spawned in the game window
    // ie: once every how many ticks does the game attempt to spawn new Entities
    protected static final int SPAWN_INTERVAL = 45;

    // A Random object for all your random number generation needs!
    public static final Random rand = new Random();

    // Player's current score
    protected int score;

    // Stores a reference to game's Player object for quick reference
    // (This Player will also be in the displayList)
    protected Player player;;

    public ScrollingGame() {
        super();
    }

    public ScrollingGame(int gameWidth, int gameHeight) {
        super(gameWidth, gameHeight);
    }

    // Performs all of the initialization operations that need to be done before the
    // game starts
    protected void pregame() {
        this.setBackgroundColor(Color.BLACK);
        // this.setBackgroundImage(INTRO_SPLASH_FILE);
        setSplashImage(INTRO_SPLASH_FILE);
        player = new Player(STARTING_PLAYER_X, STARTING_PLAYER_Y);
        displayList.add(player);
        score = 0;
    }

    // Called on each game tick
    protected void updateGame() {
        // scroll all scrollable Entities on the game board
        scrollEntities();
        // Spawn new entities only at a certain interval
        if (super.getTicksElapsed() % SPAWN_INTERVAL == 0) {
            spawnEntities();
        }
        // Update the title text on the top of the window
        setTitleText("HP: " + player.getHP() + ", Score: " + this.score);
    
    }

    // Scroll all scrollable entities per their respective scroll speeds
    private void scrollEntities() {

        for (int i = 0; i < displayList.size(); i++) {
            
            Entity entity = displayList.get(i); 
            if (entity instanceof Scrollable) {
                ((Scrollable) entity).scroll();
                if (player.isCollidingWith(entity)){
                   handlePlayerCollision((Consumable)entity);
                }

            }
        }

    }

    // Called whenever it has been determined that the Player collided with a
    // consumable
    private void handlePlayerCollision(Consumable collidedWith) {
         
        Entity entity = ((Entity) collidedWith);
        player.modifyHP(collidedWith.getDamage());
        this.score += collidedWith.getPoints();
        toBeGC.add(entity);  
    
    }

    // Spawn new Entities on the right edge of the game board
    private void spawnEntities() {
        
        //random number of entities to be added
        int numberOfEntitiesToAdd = rand.nextInt(5);

        for (int i = 0; i < numberOfEntitiesToAdd; i++) {

            int avoidY = rand.nextInt(getWindowHeight() - Avoid.AVOID_HEIGHT);
            int getY = rand.nextInt(getWindowHeight() - Get.GET_HEIGHT);
            int raregetY = rand.nextInt(getWindowHeight() - Get.GET_HEIGHT);

            //conditionals to prevent overlap
             
            while (Math.abs(getY - avoidY) < Avoid.AVOID_HEIGHT) {
                getY = rand.nextInt(getWindowHeight() - Get.GET_HEIGHT);
            }
            while (Math.abs(raregetY - avoidY) < Avoid.AVOID_HEIGHT ||
                    Math.abs(raregetY - getY) < Get.GET_HEIGHT) {
                raregetY = rand.nextInt(getWindowHeight() - Get.GET_HEIGHT);
            }


            // Generate a random number to determine entity type
            int chance = rand.nextInt(100);
            Entity entityToAdd;
            // 80% chance to add Avoid or Get
            if (chance <= 80) {
                if (chance % 2 == 0) {
                    entityToAdd = new Avoid(getWindowWidth(), avoidY);
                } else {
                    entityToAdd = new Get(getWindowWidth(), getY);
                }
            }
            // 20% chance to add RareGet
            else {
                entityToAdd = new RareGet(getWindowWidth(), raregetY);
            }
            //check if entityToAdd overlaps with previously added scrollable
            if (findCollisions(entityToAdd).size() == 0)
                displayList.add(entityToAdd);
        }

        //Garbage collecting entities that have left the window
        for (Entity e : displayList) {
            if (e instanceof Scrollable){
                if (e.getX() + e.getWidth() < 0)
                    toBeGC.add(e);
                }
        }
    }

    // Called once the game is over, performs any end-of-game operations
    protected void postgame() {

        if (player.getHP() <= 0)
            super.setTitleText("GAME OVER - You Lose!");
        else
            super.setTitleText("GAME OVER - You Won!");
    }

    
    // Determines if the game is over or not
    // Game can be over due to either a win or lose state
    protected boolean isGameOver() {

        if (player.getHP() <= 0 || this.score >= SCORE_TO_WIN) {
            return true;
        } else
            return false;
    }

    // Reacts to a single key press on the keyboard
    protected void reactToKey(int key) {
        // if a splash screen is up, only react to the advance splash key
        if (!isPaused) {
            if (getSplashImage() != null) {
                if (key == ADVANCE_SPLASH_KEY)
                    super.setSplashImage(null);
                return;
            } 
            else {
                if (key == UP_KEY && player.getY() - Player.DEFAULT_MOVEMENT_SPEED >= 0) {
                    player.setY(player.getY() - Player.DEFAULT_MOVEMENT_SPEED);
                } 
                else if (key == DOWN_KEY
                        && player.getY() + Player.DEFAULT_MOVEMENT_SPEED + player.getHeight() <= getWindowHeight()) {
                    player.setY(player.getY() + Player.DEFAULT_MOVEMENT_SPEED);
                } 
                else if (key == LEFT_KEY && player.getX() - Player.DEFAULT_MOVEMENT_SPEED >= 0) {
                    player.setX(player.getX() - Player.DEFAULT_MOVEMENT_SPEED);
                } 
                else if (key == RIGHT_KEY
                        && player.getX() + Player.DEFAULT_MOVEMENT_SPEED + player.getWidth() <= getWindowWidth()) {
                    player.setX(player.getX() + Player.DEFAULT_MOVEMENT_SPEED);
                }

                pauseAndsetSpeed(key);
            }
        } 
        else if (key == KEY_PAUSE_GAME)
            isPaused = !isPaused;
    }

    private void pauseAndsetSpeed(int key) {

        if (key == KEY_PAUSE_GAME) {
            isPaused = !isPaused;
        }
        if (key == SPEED_UP_KEY && getGameSpeed() < MAX_GAME_SPEED)
            setGameSpeed(getGameSpeed() + SPEED_CHANGE_INTERVAL);
        else if (key == SPEED_DOWN_KEY && getGameSpeed() > SPEED_CHANGE_INTERVAL)
            setGameSpeed(getGameSpeed() - SPEED_CHANGE_INTERVAL);

    }

    // Handles reacting to a single mouse click in the game window
    protected MouseEvent reactToMouseClick(MouseEvent click) {
        // Mouse functionality is not used at all in the Simple game...
        // you may want to override this function for a CreativeGame feature though!        
        return click;// returns the mouse event for any child classes overriding this method
    
    }

}
