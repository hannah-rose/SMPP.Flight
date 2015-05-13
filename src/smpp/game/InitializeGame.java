/*
 * Initializes plane and plane controls
 */
package smpp.game;

import com.jme3.asset.AssetManager;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.InputManager;
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
    public InitializeGame(Node rootNode, InputManager inputManager, AssetManager assetManager){
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
            Spatial pModel = assetManager.loadModel("Models/Cessna-172/Cessna-172.j3o");
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
    

