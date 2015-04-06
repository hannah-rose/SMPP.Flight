/*
 * Use GUI tools to display map, level, and repetion
 */
package smpp.game.appstates.levels;

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

import smpp.game.display.*;

/**
 * @author Hannah
 */
public class Level1 extends AbstractAppState {
    //Access game components
    private SimpleApplication app;
    private Node guiNode;
    private AssetManager assetManager;
    private AppSettings settings;
    
    //constructor
    public Level1(AssetManager assetManager, AppSettings settings){
        this.assetManager = assetManager;
        this.settings = settings;
    }
    
    @Override
    public void update(float tpf){
       
    }
    
    @Override
    public void cleanup() {}
    
    @Override
    public void initialize(AppStateManager stateManager, Application app){
        super.initialize(stateManager, app);
        this.app = (SimpleApplication) app;
        this.guiNode = this.app.getGuiNode();
        
        //Display map
        Picture map = new Picture("User interface map");
        map.setImage(assetManager, "Interface/FlightMap.png", true);
        map.move(20,100,0);
        map.setHeight(200);
        map.setWidth(150);
        guiNode.attachChild(map);
    }
}