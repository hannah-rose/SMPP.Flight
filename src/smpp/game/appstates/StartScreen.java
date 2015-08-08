/*
 * Displays main menu with buttons, such as Play, Options, Quit
 * Responds to clicks on buttons
 * "Play" attaches GameRunning and detaches itself
 * "Options" attaches SettingsScreen and detaches itself
 * "Quit" quits the game
 * 
 */
package smpp.game.appstates;

import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smpp.configuration.Configuration;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioRenderer;
import com.jme3.audio.Listener;
import com.jme3.input.InputManager;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.system.Timer;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.CheckBoxStateChangedEvent;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.tools.SizeValue;

/**
 *
 * @author Hannah
 */
public class StartScreen extends AbstractAppState implements ScreenController {

	private final static Logger LOG = LoggerFactory.getLogger(StartScreen.class);
	//ResourceBundle strings;
	
	/**
	 * nifty GUI element ids.
	 */
	public static final String userIdTextFieldId = "userId";

	/**
	 * nifty GUI screen ids
	 */
	public static final String scrGamePlayGuiId = "inGameGui";
	public static final String scrMainMenuId = "start";
	public static final String scrUserLoginId = "userLogin";
	public static final String scrCalibrate = "calibrate";

	/* AppState specific */
	private SimpleApplication app;
	private Camera cam;
	private Node rootNode;
	private AssetManager assetManager;
	private InputManager inputManager;
	private ViewPort guiViewPort;
	private AudioRenderer audioRenderer;
	private AppStateManager stateManager;
	private boolean flagSoundEnabled = true;
	private Listener listener;
	public Timer timer;

	private Node sceneNode;
	private Nifty nifty;
	public boolean isRunning;
	GameRunning game;
	SettingsScreen settings;


	public StartScreen() {
		super();
		//strings = ResourceBundle.getBundle("strings");
	}

	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app);
		this.stateManager = stateManager;
		this.app = (SimpleApplication) app;
		this.cam = this.app.getCamera();
		this.rootNode = this.app.getRootNode();
		this.assetManager = this.app.getAssetManager();
		this.inputManager = this.app.getInputManager();
		this.guiViewPort = this.app.getGuiViewPort();
		this.audioRenderer = this.app.getAudioRenderer();
		this.listener = this.app.getListener();
		game = new GameRunning(rootNode, assetManager, guiViewPort, inputManager, stateManager, cam, null);
		settings = new SettingsScreen();
		this.timer = app.getTimer();

		initNifty();
	}

	public void bind(Nifty nifty, Screen screen) {
		LOG.debug("bind( " + screen.getScreenId() + ")");
	}

	@Override
	public void onStartScreen() {
		//Screen s = nifty.getCurrentScreen();
	}

	@Override
	public void onEndScreen() {
		//Screen s = nifty.getCurrentScreen();
	}

	private NiftyJmeDisplay niftyDisplay;

	private void initNifty() {
		niftyDisplay = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort);
		nifty = niftyDisplay.getNifty();
		nifty.fromXml("Interface/MainMenuLayout.xml", scrUserLoginId, this);

		// attach the nifty display to the gui view port as a processor
		guiViewPort.addProcessor(niftyDisplay);
		nifty.setDebugOptionPanelColors(false);
	}

	/* called via interaction with button defined in MainMenuLayout.xml */
	public void startGame() {
		// nifty.removeScreen(scrMainMenuId);
		LOG.debug("Start Screen removed\n\tstartGame() called... detaching start");
		stateManager.detach(this);
		LOG.debug("StartScreenAppState detached");
		stateManager.attach(game);
		LOG.debug("GamePlayAppState attached. Going to scrGamePlayGui");
		nifty.gotoScreen(scrGamePlayGuiId);

	}
	/* called via interaction with calibration button in MainMenuLayout.xml */
	public void startSettings(){
		stateManager.detach(this);
		stateManager.attach(settings);
		nifty.gotoScreen("calibrate");
	}


	public static final String usrInputPanelId = "inputPanel";
	public static final String usrInputErrorTextId = "userIdErrorText";

	@NiftyEventSubscriber(id = userIdTextFieldId)
	public void onUserIdEvent(final String id, final NiftyInputEvent e) {
		TextField tf = nifty.getScreen(scrUserLoginId).findNiftyControl(userIdTextFieldId,
				TextField.class);
		if (tf == null) {
			LOG.error("textfield for user input (element id: :" + userIdTextFieldId + " screenId: "
					+ scrUserLoginId + ") was null!");
			return;
		}

		if (NiftyInputEvent.SubmitText.equals(e)) {
			if (tf.getText().length() > 0) {
				// input complete. goto main menu
				Configuration.setUserId(tf.getText());
				nifty.gotoScreen(scrMainMenuId);
			} else {
				// user must enter a non-empty identifier!
				Label errorLabel = nifty.getScreen(scrUserLoginId).findNiftyControl(
						usrInputErrorTextId, Label.class);
				errorLabel.setText("Identifier must have length > 0");
				errorLabel.setWidth(new SizeValue("100px"));
			}
		}
	}

	@NiftyEventSubscriber(id = "MyCheckBoxId")
	public void onAllCheckBoxChanged(final String id, final CheckBoxStateChangedEvent event) {
		// do something with the event
	}

	public void applySettings() {

	}

	public void gotoScreen(String screen) {
		nifty.gotoScreen(screen);
	}

	public void setStartScreen() {
		nifty.gotoScreen("userLogin");
		// nifty.gotoScreen(mainMenuScreenID);
	}

	@Override
	public void cleanup() {
	}

}