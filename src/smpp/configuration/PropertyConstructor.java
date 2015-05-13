package smpp.configuration;

import java.net.URI;
import java.net.URISyntaxException;

import org.ric.smpp.domain.common.Common.TRACKING_DATA_TYPE;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

/**
 * YAML constructor with {@link AbstractConstruct} implementations for
 * constructing {@link URI} and {@link TRACKING_DATA_TYPE} from tags.
 * 
 * @author Jack
 * 
 */
public class PropertyConstructor extends Constructor {
	public PropertyConstructor() {
		this.yamlConstructors.put(new Tag("!uri"), new ConstructURI());
		this.yamlConstructors
				.put(new Tag("!TRACKING_DATA_TYPE"), new ConstructTRACKING_DATA_TYPE());
	}

	private class ConstructURI extends AbstractConstruct {

		@Override
		public Object construct(Node node) {
			String val = (String) constructScalar((ScalarNode) node);
			URI uri = null;
			try {
				uri = new URI(val);
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return uri;
		}
	}

	private class ConstructTRACKING_DATA_TYPE extends AbstractConstruct {

		@Override
		public Object construct(Node node) {
			TRACKING_DATA_TYPE type = TRACKING_DATA_TYPE
					.valueOf((String) constructScalar((ScalarNode) node));
			return type;
		}

	}
}
