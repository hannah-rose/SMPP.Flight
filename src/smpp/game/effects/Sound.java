/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smpp.game.effects;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;

import com.jme3.audio.AudioNode;

import smpp.game.appstates.GamePhysics;

/**
 *
 * @author Hannah
 */

public class Sound extends AbstractAppState {
    //Variables
    public AudioNode coin;
    AssetManager assetManager;
    GamePhysics physics;
    
    //Constructor
    public Sound(AssetManager assetManager){
        this.assetManager = assetManager;
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        
        coin = new AudioNode(assetManager, "Sounds/coin.wav");
        coin.setVolume(8);
    }
    
    @Override
    public void update(float tpf) {
        
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
    }
}
