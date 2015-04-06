/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smpp.game;

import com.jme3.app.SimpleApplication;
import com.jme3.renderer.RenderManager;
import smpp.game.appstates.*;

/**
 *
 * @author Hannah
 */
public class Main extends SimpleApplication{
    
    public static void main(String[] args){
        Main app = new Main();
        app.start();
    }
    
    //Call StartScreen here when complete
    //For now, go straight to GameRunning
    GameRunning game_running = new GameRunning(rootNode,assetManager,viewPort,inputManager,stateManager,cam,flyCam);
    
    @Override
    public void simpleInitApp(){
        stateManager.attach(game_running);
    }
    
     @Override
    public void simpleUpdate(float tpf) {
        
    }

    @Override
    public void simpleRender(RenderManager rm) {
        
    }
}