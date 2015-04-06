/*
 * Use GUI tools to display map, level, and repetion
 */
package smpp.game.display;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapText;
import com.jme3.font.BitmapFont;
import com.jme3.ui.Picture;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;

import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.math.ColorRGBA;

/**
 * @author Hannah
 */
public class Status extends AbstractAppState {
    //Access game components
    private SimpleApplication app;
    private Node guiNode;
    private AssetManager assetManager;
    private AppSettings settings;
    
    //constructor
    public Status(AssetManager assetManager, AppSettings settings){
        this.assetManager = assetManager;
        this.settings = settings;
    }
    
    public int level=1;
    private BitmapText levelText;
    public float rep=1;
    private BitmapText repText;
    public int score=0;
    private BitmapText scoreText;
    
    @Override
    public void update(float tpf){
        levelText.setText("Level: "+level);
        repText.setText("Repetition: "+(int)rep);
        scoreText.setText("Score: "+score);
    }
    
    @Override
    public void cleanup() {}
    
    @Override
    public void initialize(AppStateManager stateManager, Application app){
        super.initialize(stateManager, app);
        this.app = (SimpleApplication) app;
        this.guiNode = this.app.getGuiNode();
        
        //Remove stat view
        this.app.setDisplayStatView(false);
        this.app.setDisplayFps(false);
        
        //Display level
        BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        levelText = new BitmapText(guiFont);
        levelText.setSize(guiFont.getCharSet().getRenderedSize());
        levelText.move(20,levelText.getLineHeight()*13,0);

        //Display repetition
        repText = new BitmapText(guiFont);
        repText.setSize(guiFont.getCharSet().getRenderedSize());
        repText.move(20,repText.getLineHeight()*12,0);
        
        //Display Score
        scoreText = new BitmapText(guiFont);
        scoreText.setSize(guiFont.getCharSet().getRenderedSize());
        scoreText.move(20,scoreText.getLineHeight()*11,0);
        
        //Attach nodes
        guiNode.attachChild(levelText);
        guiNode.attachChild(repText);
        guiNode.attachChild(scoreText);
        
        //Display map
        Picture map = new Picture("User interface map");
        map.setImage(assetManager, "Interface/FlightMap.png", true);
        map.move(20,10,0);
        map.setHeight(200);
        map.setWidth(150);
        guiNode.attachChild(map);
    }
}