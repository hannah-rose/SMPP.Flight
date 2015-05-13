package smpp.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

/**
 * static class for managing the motion game configuration. Game configuration
 * data is loaded from the file in the current working directory with name
 * {@link #configName}. The file is in YAML format. And its expected to contain
 * a map with the key {@link #sessionsKey}, which contains itself a map of
 * Strings (user ids) to a list of {@link SetConfig}. 
 * 
 * @author Jack
 * 
 */
public class GameConfiguration {
	private static final Logger LOG = LoggerFactory.getLogger(GameConfiguration.class);

	private static String configName = "game_config.yaml";
	public static final String FEEDBACK_KEY_AUDIO = "audio", FEEDBACK_KEY_SLOTS = "slots",
			FEEDBACK_KEY_COINS = "coins", FEEDBACK_HELPER_PLANE = "helper",
			FEEDBACK_REALTIME = "realtime";
	private static final String setTag = "!set";
	private static final String sessionsKey = "sessions";
	private static Map<String, List<SetConfig>> configMap = new HashMap<String, List<SetConfig>>();
	static Map<String, ?> configuration;
	static Yaml y;

	/**
	 * checks the id -> setConfigs map, and if no mapping is found, attempts to
	 * load the configuration from the appropriate file.
	 * 
	 * @param id
	 *            the userId whose session configuration (ie list of setConfigs)
	 *            should be loaded
	 * @return a list of set configuration ({@link SetConfig}) objects
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private static List<SetConfig> loadConfigForId(String id) throws IOException {
		List<SetConfig> sessionConfig = configMap.get(id);
		if (sessionConfig != null) {
			return sessionConfig;
		}
		if (configuration == null) {
			if (!loadConfiguration())
				return defaultSessionConfig();// defaultSetConfig();
		}
		sessionConfig = ((Map<String, List<SetConfig>>) configuration.get(sessionsKey)).get(id);
		if (sessionConfig == null) // session config for that id couldn't be
									// found. Gen default
			sessionConfig = defaultSessionConfig();
		configMap.put(id, sessionConfig);
		// String test = y.dump(configuration);
		return sessionConfig;
	}

	@SuppressWarnings("unchecked")
	private static boolean loadConfiguration() throws IOException {
		List<SetConfig> sessionConfig;
		File configFile = new File(configName);
		if (configFile.createNewFile()) {
			// file didn't exist
			// TODO: construct default config file
			LOG.warn("no yaml config file found (expecting " + configFile.getAbsolutePath() + ")");
			return false;
		}
		Constructor c = new Constructor();
		TypeDescription sessionDescription = new TypeDescription(SessionConfiguration.class);
		// sessionDescription.putListPropertyType("sessions", SetConfig.class);
		sessionDescription.putMapPropertyType(sessionsKey, String.class, List.class);
		c.addTypeDescription(sessionDescription);
		Tag sTag = new Tag(setTag);
		c.addTypeDescription(new TypeDescription(SetConfig.class, sTag));
		Representer r = new Representer();
		r.addClassTag(SetConfig.class, sTag);
		y = new Yaml(c, r);
		configuration = (Map<String, ?>) y.load(new FileInputStream(configFile));
		LOG.info("parsed yaml config file at " + configFile.getAbsolutePath() + "\n"
				+ "configurations: " + configuration);
		return true;
	}

	private static List<SetConfig> defaultSessionConfig() {
		LOG.info("Generating default session configuration. NOT IMPLEMENTED");
		List<SetConfig> c = new ArrayList<SetConfig>();
		c.add(buildDefaultSetConfig());
		return c;
	}

	private static SetConfig buildDefaultSetConfig() {
		return new SetConfig();
	}

	/**
	 * get the list of Set configuration objects for the given id. Configuration
	 * filename for id is '{id}_config.yaml'. The configurations in the file
	 * should be listed in the order it is desired they be loaded in the
	 * application in.
	 * 
	 * @return list of {@link SetConfig} objects in the order they should be
	 *         applied
	 * @throws IOException
	 */
	public static List<SetConfig> getSetListForId(String id) throws IOException {
		return loadConfigForId(id);
	}

	public static void saveGameConfiguration() throws IOException {
		File f = new File(configName);
		f.delete();
		f.createNewFile();
		Writer w = new OutputStreamWriter(new FileOutputStream(f));
		// w.write("#note, all instances of !" + SetConfig.class +
		// " can be replaced with " + setTag +'\n');
		y.dump(configuration, w);
		w.close();
	}
}
