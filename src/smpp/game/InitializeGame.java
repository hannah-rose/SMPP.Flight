/*
 * Initializes plane and plane controls
 */
package smpp.game;

import com.jme3.asset.AssetManager;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.renderer.ViewPort;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl;
import com.jme3.scene.CameraNode;
import com.jme3.input.FlyByCamera;
import com.jme3.renderer.Camera;


/**
 * Initialize game
 * @author Hannah
 */

public class InitializeGame{
    private Node              rootNode;
    private AssetManager      assetManager;
    private AppStateManager   stateManager;
    private InputManager      inputManager;
    private ViewPort          viewPort;
    
    public InitializeGame(Node rootNode, InputManager inputManager, AssetManager assetManager){
        this.rootNode = rootNode;
        this.inputManager = inputManager;
        this.assetManager = assetManager;
    }
   
    public Node player;
    

    public void initialize_game(Camera cam, FlyByCamera flyCam, Node rootNode, InputManager inputManager, 
                                  AssetManager assetManager, Node pivot, AppStateManager stateManager){
        
        float planeScale = 0.2f;
        // load plane model
        player = buildPlane(planeScale, assetManager);
        
        //Camera that follows player
        // Disable the default flyby cam
        flyCam.setEnabled(false);
        //create the camera Node
        CameraNode camNode = new CameraNode("Camera Node", cam);
        //This mode means that camera copies the movements of the target:
        camNode.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
        //Attach the camNode to the target:
        player.attachChild(camNode);
        //Move camNode, e.g. behind and above the target:
        camNode.setLocalTranslation(new Vector3f(0f, 2f, -20f));
        //Rotate the camNode to look at the target:
        camNode.lookAt(player.getLocalTranslation(), Vector3f.UNIT_Y);
        
        rootNode.attachChild(player);
        //initKeys();
        
    }

    
//      //Custom Keybinding: Map named actions to inputs
//    private void initKeys(InputManager inputManager){
//        //You can map one or several inputs to one named action
//        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_J));
//        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_L));
//        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_I));
//        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_K));
//        inputManager.addMapping("In", new KeyTrigger(KeyInput.KEY_SPACE));
//        
//        //Add the names to the listeners
//        inputManager.addListener(analogListener, "Left", "Right", "Up", "Down", "In");
//    }
//    
//    
//    private AnalogListener analogListener = new AnalogListener(){
//        public void onAnalog(String name, float value, float tpf){
//            if (name.equals("Right")){
//                Vector3f v = player.getLocalTranslation();
//                player.setLocalTranslation(v.x + value*tpf, v.y, v.z);
//            }
//            if (name.equals("Left")){
//                Vector3f v = player.getLocalTranslation();
//                player.setLocalTranslation(v.x - value*tpf, v.y, v.z);
//            }
//            if (name.equals("Up")){
//                Vector3f v = player.getLocalTranslation();
//                player.setLocalTranslation(v.x, v.y + value*tpf, v.z);
//            }
//            if (name.equals("Down")){
//                Vector3f v = player.getLocalTranslation();
//                player.setLocalTranslation(v.x, v.y - value*tpf, v.z);
//            }
//            if (name.equals("In")){
//                Vector3f v = player.getLocalTranslation();
//                player.setLocalTranslation(v.x, v.y, v.z - value*tpf*2);
//            }
//            else {
//                System.out.println("Press P to unpause.");
//            }
//        }
//    }; 
    
    /**
     * builds the plane node. Uses 'Models/Cessna-172.obj' 3d object and
     * associated materials. Adds GhostControl with BoxCollisionShape to plane
     * spatial
     * 
     * @param planeScale
     *            scaling factor for the plane.
     * @return
     */
    private Node buildPlane(float planeScale, AssetManager assetManager) {
            Spatial pModel = assetManager.loadModel("Models/Cessna-172.obj");
            Node plane = new Node();
            // BoundingBox pbox = (BoundingBox) pModel.getWorldBound();
            pModel.setName("plane");

            plane.attachChild(pModel);
            plane.scale(planeScale);
            plane.setLocalTranslation(35f, -3f, 55f);
            plane.rotate(0f, 180 * FastMath.DEG_TO_RAD, 0f);
            //plane.addLight();
            plane.setShadowMode(RenderQueue.ShadowMode.Inherit);
            return plane;
    }
}
    

