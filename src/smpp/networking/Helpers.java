package smpp.networking;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

public class Helpers {
	public static void loginAndSaveJsessionIdCookie(final String user,
			final String password, int port, final HttpHeaders headersToUpdate) {

		String url = "http://localhost:" + port + "/j_spring_security_check";

		new RestTemplate().execute(url, HttpMethod.POST,

		new RequestCallback() {
			@Override
			public void doWithRequest(ClientHttpRequest request)
					throws IOException {
				MultiValueMap<String, String> map;
                            map = new LinkedMultiValueMap<>();
				map.add("j_username", user);
				map.add("j_password", password);
				new FormHttpMessageConverter().write(map,
						MediaType.APPLICATION_FORM_URLENCODED, request);
			}
		},

		new ResponseExtractor<Object>() {
			@Override
			public Object extractData(ClientHttpResponse response)
					throws IOException {
				String strSetCookie = response.getHeaders().getFirst(
						"Set-Cookie");
				if (!strSetCookie.isEmpty()) {
					String sessionId = strSetCookie.split(";")[0];
					headersToUpdate.add("Cookie", sessionId);
				}

				return null;
			}
		});
	}
}
