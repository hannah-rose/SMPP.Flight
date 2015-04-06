/*
 * Draw the scene of the game, only needs to be called once
 * Map
 * Level 1: (35,0,45)
 * Level 2: (-10,0,35)
 * Level 3: (-35,0,20)
 * Level 4: (-25,0,5)
 * Level 5: (5,0,-5)
 * Level 6: (35,0,-15)
 * Level 7: (20,0,-35)
 * Level 8: (0,0,-30)
 * Level 9: (-25,0,-25)
 * Level 10: (-30,0,-45)
 */
package smpp.game;

import com.jme3.light.AmbientLight;
import com.jme3.util.SkyFactory;
import com.jme3.texture.Texture;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.control.BetterCharacterControl;

import com.jme3.asset.AssetManager;
import com.jme3.renderer.ViewPort;
import com.jme3.app.state.AppStateManager;

import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.DirectionalLightShadowRenderer;

/**
 * World
 * @author Hannah
 */

public class World {
    private Node              rootNode;
    private AssetManager      assetmanager;
    private ViewPort          viewPort;
    
    public World(Node rootnode, AssetManager assetManager, ViewPort viewport){
        this.rootNode = rootnode;
        this.assetmanager = assetManager;
        this.viewPort = viewport;
    }
    
    private Spatial sky;
    public Node platforms;
    public Node obstacles;
    public Node rewards;
    public static final String LANDING = "Landing";
    
    public Vector3f Level1 = new Vector3f(35f,0f,45f);
    public Vector3f Level2 = new Vector3f(-10f,0f,35f);
    public Vector3f Level3 = new Vector3f(-35f,0f,20f);
    public Vector3f Level4 = new Vector3f(-25f,0f,5f);
    public Vector3f Level5 = new Vector3f(5f,0f,-5f);
    public Vector3f Level6 = new Vector3f(35f,0f,-15f);
    public Vector3f Level7 = new Vector3f(20f,0f,-35f);
    public Vector3f Level8 = new Vector3f(0f,0f,-30f);
    public Vector3f Level9 = new Vector3f(-25f,0f,-25f);
    public Vector3f Level10 = new Vector3f(-30f,0f,-45f);
    
    Vector3f directionalLightDir = new Vector3f(0, -1, 0);
    private static final int SHADOW_MAP_SZ = (2048 * 2), NB_SPLITS = 3;
   
    public void draw_world(AssetManager assetmanager, ViewPort viewPort, Node rootNode){
        //Sky
        Texture west = assetmanager.loadTexture("Textures/Sky_1.jpg");
        Texture east = assetmanager.loadTexture("Textures/Sky_1.jpg");
        Texture north = assetmanager.loadTexture("Textures/Sky_1.jpg");
        Texture south = assetmanager.loadTexture("Textures/Sky_1.jpg");
        Texture up = assetmanager.loadTexture("Textures/Sky_up.jpg");
        Texture down = assetmanager.loadTexture("Textures/Sky_down.JPG");

        sky = SkyFactory.createSky(assetmanager, west, east, north, south, up, down);
        rootNode.attachChild(sky);
        
        //sun
        initLight(assetmanager, viewPort, rootNode);
        
        //Create a platforms node at (0,0,0) and attach it to the root node
        platforms = new Node("platforms");
        obstacles = new Node("obstacles");
        rewards = new Node("rewards");
        rootNode.attachChild(platforms); //put this node in the scene
        rootNode.attachChild(obstacles);
        rootNode.attachChild(rewards);
        
        //Draw all levels
        drawLevel(Level1, assetmanager);
        drawLevel(Level2, assetmanager);
        drawLevel(Level3, assetmanager);
        drawLevel(Level4, assetmanager);
        drawLevel(Level5, assetmanager);
        drawLevel(Level6, assetmanager);
        drawLevel(Level7, assetmanager);
        drawLevel(Level8, assetmanager);
        drawLevel(Level9, assetmanager);
        drawLevel(Level10, assetmanager);
    }
    
    
    /*Function takes in origin of level and draws landing platforms
     *obstacles relative to the origin
     *Will eventually split into different functions for each level*/
    private void drawLevel(Vector3f position, AssetManager assetmanager){
        //create a solid platform at relative coordinates (0,-1,-10)
        Box platformMesh = new Box(Vector3f.ZERO, 1f, 0.25f, 1f);
        Geometry platformGeo1 = new Geometry("Landing", platformMesh);
        //Material stoneMat = assetmanager.loadMaterial("Materials/pebbles.j3m");
        //platformGeo.setMaterial(stoneMat);
        Material mat1 = new Material(assetmanager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat1.setColor("Color", ColorRGBA.Yellow);
        platformGeo1.setMaterial(mat1);
        platformGeo1.move(position.x,position.y-1,position.z-10);
        
        
        //create a solid platform at relative coordinates (8,0,-10)
        //Box platformMesh2 = new Box(Vector3f.ZERO, 1f, 0.25f, 1f);
        Geometry platformGeo2 = new Geometry("Landing", platformMesh);
        Material mat2 = new Material(assetmanager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat2.setColor("Color", ColorRGBA.Red);
        platformGeo2.setMaterial(mat2);
        platformGeo2.move(position.x+8,position.y,position.z-10);
        
        //create a solid platform at relative coordinates (-8,0,-10)
        //Box platformMesh3 = new Box(Vector3f.ZERO, 1f, 0.25f, 1f);
        Geometry platformGeo3 = new Geometry("Landing", platformMesh);
        Material mat3 = new Material(assetmanager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat3.setColor("Color", ColorRGBA.Green);
        platformGeo3.setMaterial(mat3);
        platformGeo3.move(position.x-8,position.y,position.z-10);
        
        //Solid obstacle to rotate around
        Sphere obstacleMesh = new Sphere(16,16,0.2f);
        Geometry obstacleGeo1 = new Geometry("Obstacle",obstacleMesh);
        Material mat4 = new Material(assetmanager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat4.setColor("Color", ColorRGBA.Gray);
        obstacleGeo1.setMaterial(mat4);
        obstacleGeo1.move(position.x-3,position.y-1.5f,position.z);
        
        //Rewards to gather
        Spatial coin = assetmanager.loadModel("Models/coin/coin.j3o");
        Material mat5 = new Material(assetmanager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat5.setColor("Color", ColorRGBA.Yellow);
        coin.setMaterial(mat5);
        coin.move(position.x+4,position.y-1.5f,position.z);
        coin.setLocalScale(0.4f,0.4f,0.4f);
        coin.rotate(90*FastMath.DEG_TO_RAD, 30*FastMath.DEG_TO_RAD, 0f);
        
        //Attach the three boxes to the platforms node
        platforms.attachChild(platformGeo1);
        platforms.attachChild(platformGeo2);
        platforms.attachChild(platformGeo3);
        
        //Attach obstacles to obstacle node
        obstacles.attachChild(obstacleGeo1);
        
        //Attach rewards to reward node
        rewards.attachChild(coin);
    }
    
    private void initLight(AssetManager assetmanager, ViewPort viewPort, Node rootNode) {
		// construct and add point light to scene
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White);
		rootNode.addLight(al);
		DirectionalLight pl = new DirectionalLight();
		pl.setColor(ColorRGBA.White);
		pl.setDirection(directionalLightDir);
		rootNode.addLight(pl);
		// set shadow mode for whole scene
		rootNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
		// construct point shadow renderer
		DirectionalLightShadowRenderer plsr = new DirectionalLightShadowRenderer(assetmanager,
				SHADOW_MAP_SZ, NB_SPLITS);
		plsr.setLight(pl);
		viewPort.addProcessor(plsr);

	}
}
    

