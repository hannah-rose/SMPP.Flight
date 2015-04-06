/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smpp.game.control;

import com.jme3.input.KeyInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.renderer.Camera;

import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.Application;
import com.jme3.input.InputManager;

import com.jme3.app.SimpleApplication;


/**
 * Control for moving plane with keyboard inputs
 * @author Hannah
 */
public class Key_Motion extends AbstractAppState {
    //Access game components
    private SimpleApplication app;
    private InputManager inputManager;
    private Node player;
    private Camera cam;
    
    //Constructor
    public Key_Motion(Node player, Camera cam){
        this.player = player;
        this.cam = cam;
    }
    
    @Override
    public void update(float tpf){}
    
    @Override
    public void cleanup() {}
    
    @Override
    public void initialize(AppStateManager stateManager, Application app){
        super.initialize(stateManager, app);
        this.app = (SimpleApplication) app;
        this.inputManager = this.app.getInputManager();
        
        initKeys(inputManager);
    }
    
    //Custom Keybinding: Map named actions to inputs
    private void initKeys(InputManager inputManager){
        //Keys to control plane
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_L));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_I));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_K));
        inputManager.addMapping("In", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("TurnLeft", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("TurnRight", new KeyTrigger(KeyInput.KEY_E));
        inputManager.addMapping("TiltLeft", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("TiltRight", new KeyTrigger(KeyInput.KEY_D));
        
        //Add the names to the listeners
        inputManager.addListener(analogListener, "Left", "Right", "Up", "Down", "In",
                                                 "TurnLeft", "TurnRight", "TiltLeft", "TiltRight");
    }
    
    
    private AnalogListener analogListener = new AnalogListener(){
        public void onAnalog(String name, float value, float tpf){
            if (name.equals("Right")){
                Vector3f v = player.getLocalTranslation();
                player.setLocalTranslation(v.x + value*tpf*100, v.y, v.z);
            }
            if (name.equals("Left")){
                Vector3f v = player.getLocalTranslation();
                player.setLocalTranslation(v.x - value*tpf*100, v.y, v.z);
            }
            if (name.equals("Up")){
                Vector3f v = player.getLocalTranslation();
                player.setLocalTranslation(v.x, v.y + value*tpf*100, v.z);
            }
            if (name.equals("Down")){
                Vector3f v = player.getLocalTranslation();
                player.setLocalTranslation(v.x, v.y - value*tpf*100, v.z);
            }
            if (name.equals("In")){
                Vector3f dir = cam.getDirection();
                Vector3f v = player.getLocalTranslation();
                player.setLocalTranslation(v.x+(value*tpf*150*dir.x), v.y+(value*tpf*150*dir.y), v.z - value*tpf*150);
            }
            if (name.equals("TurnRight")){
                player.rotate(0f,tpf*(-1),0f);
            }
            if (name.equals("TurnLeft")){
                player.rotate(0f,tpf*1,0f);
            }
            if (name.equals("TiltRight")){
                player.rotate(0f,0f,tpf*1);
            }
            if (name.equals("TiltLeft")){
                player.rotate(0f,0f,tpf*(-1));
            }
        }
    };

}
