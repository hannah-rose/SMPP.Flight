package smpp.configuration;

import java.util.List;
import java.util.Map;

public class SessionConfiguration {
	//TODO: ids unnecessary?
	private List<String> ids;
	
	private Map<String, List<SetConfig>> sessions;

	public List<String> getIds() {
		return ids;
	}

	public void setIds(List<String> ids) {
		this.ids = ids;
	}

	public Map<String, List<SetConfig>> getSessions() {
		return sessions;
	}

	public void setSessions(Map<String, List<SetConfig>> sessions) {
		this.sessions = sessions;
	}
}
