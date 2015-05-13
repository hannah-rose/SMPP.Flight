/*
 * Initializes the game physics and monitors motion
 */
package smpp.game.appstates;

import com.jme3.scene.Node;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.Application;
import com.jme3.math.Vector3f;

import com.jme3.app.SimpleApplication;
import com.jme3.math.Quaternion;

import smpp.game.*;
import smpp.game.display.*;
import smpp.game.effects.*;

/**
 * Appstate for running game
 * @author Hannah
 */
public class GamePhysics extends AbstractAppState {
    //Access game components
    private SimpleApplication app;
    private Node player;
    private Node obstacles;
    public boolean hit_platform = false;
    public boolean hit_obstacle = false;
    public boolean hit_reward = false;
    private Status game_status;
    private Sound sound;
    private World world;
    private Vector3f center;
    private Vector3f plane_init;
    private Vector3f position;
    private Quaternion rotation;
    private boolean rep_check = true;
    
    //Constructor
    public GamePhysics(AppStateManager stateManager, World world, Node player, 
                    Status game_status, Sound sound ){
        this.obstacles = world.obstacles;
        this.world = world;
        this.player = player;
        this.game_status = game_status;
        this.sound = sound;
        
    }
    
    
    @Override
    public void update(float tpf){
        //Get position of plane and set relative scene coordinates
        position = player.getLocalTranslation();
        rotation = player.getLocalRotation();
        
        System.out.println(position);
        System.out.println(rotation.getX());
        
        //Logic to deal with obstacle collisions
        //If you are in the z coordinates of the obstacle, but you're not in the
        //correct x and y coordinates with the proper rotation, restart
        if (((position.z>center.z-0.5)&&(position.z<center.z+0.5)&&(position.x<center.x))
             &&((position.x>center.x-2.5)||(position.x<center.x-3.5)
                ||(position.y>center.y-1)||(position.y<center.y-2)
                ||(rotation.getX()<0.1f)||(rotation.getX()>0.3f))){
            player.setLocalTranslation(plane_init);
            game_status.score = game_status.score - 1;
        }
        
        //Logic to deal with reward collisions
        //1x1x1 collision cube, centered around the center of the coin
        if ((position.x<center.x+4.5)&&(position.x>center.x+3.5)
            &&(position.y<center.y-1)&&(position.y>center.y-2)
            &&(position.z<center.z+0.5)&&(position.z>center.z-0.5)){
            if(rep_check){
                //Avoid incrementing score more than once per level
                rep_check=false;
                sound.coin.play();
                game_status.score = game_status.score + 1;
            }
        }
        
        //Logic to move through levels and repetitions
        //Check for landing on platform1, platform2, or platform3
        if (((position.x<center.x+0.5)&&(position.x>center.x-0.5)
             &&(position.y<center.y)&&(position.y>center.y-1.5)
             &&(position.z<center.z-9)&&(position.z>center.z-10.5))
           ||((position.x<center.x-7.5)&&(position.x>center.x-8.5)
             &&(position.y<center.y+1)&&(position.y>center.y-2)
             &&(position.z<center.z-9)&&(position.z>center.z-10.5))
           ||((position.x<center.x+8.5)&&(position.x>center.x+7.5)
             &&(position.y<center.y+1)&&(position.y>center.y-2)
             &&(position.z<center.z-9)&&(position.z>center.z-10.5))){
            rep_check=true;
            if(game_status.rep==3){
                game_status.rep=0;
                switch(game_status.level){
                case 1:
                    plane_init = new Vector3f(world.Level2.x,world.Level2.y-3f,world.Level2.z+10f);
                    center=world.Level2;
                    game_status.level=2;
                    break;
                case 2:
                    plane_init = new Vector3f(world.Level3.x,world.Level3.y-3f,world.Level3.z+10f);
                    center=world.Level3;
                    game_status.level=3;
                    break;
                case 3:
                    plane_init = new Vector3f(world.Level4.x,world.Level4.y-3f,world.Level4.z+10f);
                    center=world.Level4;
                    game_status.level=4;
                    break;
                case 4:
                    plane_init = new Vector3f(world.Level5.x,world.Level5.y-3f,world.Level5.z+10f);
                    center=world.Level5;
                    game_status.level=5;
                    break;
                case 5:
                    plane_init = new Vector3f(world.Level6.x,world.Level6.y-3f,world.Level6.z+10f);
                    center=world.Level6;
                    game_status.level=6;
                    break;
                case 6:
                    plane_init = new Vector3f(world.Level7.x,world.Level7.y-3f,world.Level7.z+10f);
                    center=world.Level7;
                    game_status.level=7;
                    break;
                case 7:
                    plane_init = new Vector3f(world.Level8.x,world.Level8.y-3f,world.Level8.z+10f);
                    center=world.Level8;
                    game_status.level=8;
                    break;
                case 8:
                    plane_init = new Vector3f(world.Level9.x,world.Level9.y-3f,world.Level9.z+10f);
                    center=world.Level9;
                    game_status.level=9;
                    break;
                case 9:
                    plane_init = new Vector3f(world.Level10.x,world.Level10.y-3f,world.Level10.z+10f);
                    center=world.Level10;
                    game_status.level=10;
                    break;
                default:
                    plane_init = new Vector3f(world.Level1.x,world.Level1.y-3f,world.Level1.z+10f);
                    center=world.Level1;
                    game_status.level=1;
                    break;
                }
                //platform1 = new Vector3f(plane_init.x,plane_init.y-1,plane_init.z-10);
                //platform2 = new Vector3f(plane_init.x+8,plane_init.y,plane_init.z-10);
                //platform3 = new Vector3f(plane_init.x-8,plane_init.y,plane_init.z-10);
                //coin = new Vector3f(plane_init.x+4,plane_init.y-1.5f,plane_init.z);
                //obstacle = new Vector3f(plane_init.x-3,plane_init.y-1.5f,plane_init.z);
            }
        player.setLocalTranslation(plane_init);
        //player.setLocalRotation(rotation);
        game_status.rep = game_status.rep + 1f;  
        }         
    }
    
    @Override
    public void cleanup() {}
    
    
    @Override
    public void initialize(AppStateManager stateManager, Application app){
        super.initialize(stateManager, app);
        this.app = (SimpleApplication) app;
        this.app.getInputManager();
        this.app.getAssetManager();
        this.app.getViewPort();
        this.app.getFlyByCamera();
        this.app.getCamera();
        
        player.setName("player");
        obstacles.setName("obstacles");
        center = world.Level1;
        plane_init = new Vector3f(world.Level1.x,world.Level1.y-3f,world.Level1.z+10f);
        //rotation = player.getLocalRotation();
        
//        platform1 = new Vector3f(plane_init.x,plane_init.y-1,plane_init.z-10);
//        platform2 = new Vector3f(plane_init.x+8,plane_init.y,plane_init.z-10);
//        platform3 = new Vector3f(plane_init.x-8,plane_init.y,plane_init.z-10);
//        coin = new Vector3f(plane_init.x+4,plane_init.y-1.5f,plane_init.z);
//        obstacle = new Vector3f(plane_init.x-3,plane_init.y-1.5f,plane_init.z);
           
        
    }
}
