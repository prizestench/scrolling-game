//A RareGet is a rare kind of Get that spawns more infrequently than the regular Get
//When consumed, RareGets restores the Player's HP in addition to awarding points
//Otherwise, behaves the same as a regular Get
public class RareGet extends Get{
    
    //Location of image file to be drawn for a RareGet
    public static final String RARE_GET_IMAGE_FILE = "boateng_game_assets/comet.gif";

    public RareGet(){
        this(0, 0);        
    }
    
    public RareGet(int x, int y){
        super(x, y, RARE_GET_IMAGE_FILE);  
    }
    
    //I'm missing something here...
    //What's special about RareGets?
    public int getDamage(){
        return 1;
    }   
}
