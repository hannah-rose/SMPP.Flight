package smpp.configuration;

public class ConfigurationNotInitException extends Exception {

    public ConfigurationNotInitException() {
        super("Configuration Not initialized exception! You must first call"
              + "Configuration.init() before trying to access configuration values");
    }

    /**
     * 
     */
    private static final long serialVersionUID = -3473285425260479091L;

}
