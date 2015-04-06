/*
 * Initializes the game physics and monitors motion
 */
package smpp.game.appstates;

import com.jme3.scene.Node;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.Application;
import com.jme3.input.InputManager;
import com.jme3.asset.AssetManager;
import com.jme3.renderer.ViewPort;
import com.jme3.input.FlyByCamera;
import com.jme3.renderer.Camera;
import com.jme3.system.AppSettings;
import com.jme3.scene.CameraNode;
import com.jme3.scene.control.CameraControl;
import com.jme3.math.Vector3f;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.GhostControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;

import smpp.game.*;
import smpp.game.control.*;
import smpp.game.display.*;
import smpp.game.appstates.levels.*;

/**
 * Appstate for running game
 * @author Hannah
 */
public class InitPhysics extends AbstractAppState 
                         implements PhysicsCollisionListener {
    //Access game components
    private SimpleApplication app;
    private InputManager inputManager;
    private AssetManager assetManager;
    private Node rootNode;
    private Node platforms;
    private Node player;
    private Node obstacles;
    private Node rewards;
    private ViewPort viewPort;
    private Camera cam;
    private CameraNode camNode;
    private FlyByCamera flyCam;
    private AppSettings settings;
    private AppStateManager stateManager;
    private BulletAppState bulletAppState;
    private RigidBodyControl scenePhy;
    private RigidBodyControl obPhy;
    private RigidBodyControl rewardPhy;
    private RigidBodyControl playerControl;
    
    private boolean hit_platform = false;
    private boolean hit_obstacle = false;
    private boolean hit_reward = false;
    private Material mat2;
    private Status game_status;
    private World world;
    private Vector3f center;
    private Quaternion rotation;
    
    //Constructor
    public InitPhysics(AppStateManager stateManager, World world, Node player, Status game_status ){
        this.stateManager = stateManager;
        this.platforms = world.platforms;
        this.obstacles = world.obstacles;
        this.rewards = world.rewards;
        this.world = world;
        this.player = player;
        this.game_status = game_status;
        
    }
    
    public void collision(PhysicsCollisionEvent event){
        if ( (event.getNodeA().getName().equals("platforms")
             && event.getNodeB().getName().equals("player") )
             || ( event.getNodeA().getName().equals("player")
             && event.getNodeB().getName().equals("platforms")) )
        {
            hit_platform = true;
        }
        else{
            hit_platform = false;
        }
        
        if ( (event.getNodeA().getName().equals("obstacles")
             && event.getNodeB().getName().equals("player") )
             || ( event.getNodeA().getName().equals("player")
             && event.getNodeB().getName().equals("obstacles")) )
        {
            hit_obstacle = true;
        }
        else {
            hit_obstacle = false;
        }
        
        if ( (event.getNodeA().getName().equals("rewards")
             && event.getNodeB().getName().equals("player") )
             || ( event.getNodeA().getName().equals("player")
             && event.getNodeB().getName().equals("rewards")) )
        {
            hit_reward = true;
        }
        else {
            hit_reward = false;
        }
    }
  
    
    @Override
    public void update(float tpf){
        //Logic to deal with obstacle collisions
        if (hit_obstacle){
            hit_obstacle=false;
            player.setLocalTranslation(center);
            game_status.score = game_status.score - 1;
        }
        
        //Logic to deal with reward collisions
        if (hit_reward){
            hit_reward=false;
            game_status.score = game_status.score + 1;
        }
        
        //Logic to move through levels and repetitions
        if (hit_platform){
            if(game_status.rep==3){
                game_status.rep=0;
                switch(game_status.level){
                case 1:
                    center = new Vector3f(world.Level2.x,world.Level2.y-3f,world.Level2.z+10f);
                    game_status.level=2;
                    break;
                case 2:
                    center = new Vector3f(world.Level3.x,world.Level3.y-3f,world.Level3.z+10f);
                    game_status.level=3;
                    break;
                case 3:
                    center = new Vector3f(world.Level4.x,world.Level4.y-3f,world.Level4.z+10f);
                    game_status.level=4;
                    break;
                case 4:
                    center = new Vector3f(world.Level5.x,world.Level5.y-3f,world.Level5.z+10f);
                    game_status.level=5;
                    break;
                case 5:
                    center = new Vector3f(world.Level6.x,world.Level6.y-3f,world.Level6.z+10f);
                    game_status.level=6;
                    break;
                case 6:
                    center = new Vector3f(world.Level7.x,world.Level7.y-3f,world.Level7.z+10f);
                    game_status.level=7;
                    break;
                case 7:
                    center = new Vector3f(world.Level8.x,world.Level8.y-3f,world.Level8.z+10f);
                    game_status.level=8;
                    break;
                case 8:
                    center = new Vector3f(world.Level9.x,world.Level9.y-3f,world.Level9.z+10f);
                    game_status.level=9;
                    break;
                case 9:
                    center = new Vector3f(world.Level10.x,world.Level10.y-3f,world.Level10.z+10f);
                    game_status.level=10;
                    break;
                case 10:
                    center = new Vector3f(world.Level1.x,world.Level1.y-3f,world.Level1.z+10f);
                    game_status.level=1;
                    break;
                default:
            }
            }
        hit_platform=false;
        player.setLocalTranslation(center);
        player.setLocalRotation(rotation);
        game_status.rep = game_status.rep + 0.5f;  
        }         
    }
    
    @Override
    public void cleanup() {}
    
    
    @Override
    public void initialize(AppStateManager stateManager, Application app){
        super.initialize(stateManager, app);
        this.app = (SimpleApplication) app;
        this.inputManager = this.app.getInputManager();
        this.assetManager = this.app.getAssetManager();
        this.viewPort = this.app.getViewPort();
        this.flyCam = this.app.getFlyByCamera();
        this.cam = this.app.getCamera();
        
        player.setName("player");
        obstacles.setName("obstacles");
        center = new Vector3f(world.Level1.x,world.Level1.y-3f,world.Level1.z+10f);
        rotation = player.getLocalRotation();

        //Test material
        mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat2.setColor("Color", ColorRGBA.Magenta);
        
        //Activate physics simulation
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        
        //make the platforms node static
        scenePhy = new RigidBodyControl(0f);
        platforms.addControl(scenePhy);
        bulletAppState.getPhysicsSpace().add(platforms);
        
        //make obstacle node static
        obPhy = new RigidBodyControl(0f);
        obstacles.addControl(obPhy);
        bulletAppState.getPhysicsSpace().add(obstacles);
        
        //make reward node static
        rewardPhy = new RigidBodyControl(0f);
        rewards.addControl(rewardPhy);
        bulletAppState.getPhysicsSpace().add(rewards);
        
        //Add control to the plane
        playerControl = new RigidBodyControl(50f);
        player.addControl(playerControl);
        playerControl.setKinematic(true);
        bulletAppState.getPhysicsSpace().add(player);
        
        //Add hit_platform detector
        bulletAppState.getPhysicsSpace().addCollisionListener(this);
        

        
        //bulletAppState.getPhysicsSpace().enableDebug(assetManager);
           
        
    }
}
