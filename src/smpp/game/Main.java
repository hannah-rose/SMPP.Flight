/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smpp.game;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import org.slf4j.LoggerFactory;

import com.jme3.app.SimpleApplication;
import com.jme3.renderer.RenderManager;

import smpp.game.appstates.*;
import smpp.configuration.Configuration;

/**
 *
 * @author Hannah
 */
public class Main extends SimpleApplication{
	
    public static void main(String[] args){
        Main game = new Main();
        
        // Initialize configuration and configuration paramaters 
        try {
			Configuration.init();

		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        game.start();
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