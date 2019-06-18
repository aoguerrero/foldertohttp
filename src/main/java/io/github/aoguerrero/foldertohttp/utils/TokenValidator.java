package io.github.aoguerrero.foldertohttp.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class TokenValidator {

	private final Logger logger = LoggerFactory.getLogger(TokenValidator.class);

	@Autowired
	private Environment env;

	@Autowired
	private EncDec encDec;

	private SimpleDateFormat sdf;
	private Long sessionTimeout;
	private String apikey;

	@PostConstruct
	public void init() {
		sdf = new SimpleDateFormat("ddMMyyyyHHmmss");
		sessionTimeout = Long.valueOf(env.getRequiredProperty("login.sessionTimeout")) * 1000;
		apikey = env.getProperty("service.apikey");
	}

	public boolean validateHeader(HttpHeaders headers) {
		List<String> headerValues = headers.get("token");
		if (headerValues == null || headerValues.size() == 0) {
			logger.warn("Invalid headers");
			return false;
		}
		String token = headerValues.get(0);
		return validateToken(token);
	}

	public boolean validateToken(String token) {
		try {
			if(apikey != null) {
				if(token.equals(apikey)) {
					return true;
				}
			}
			
			Date loginDate = sdf.parse(encDec.decrypt(token));
			Date now = new Date();
			Long dif = now.getTime() - loginDate.getTime();
			if (dif > sessionTimeout) {
				logger.warn("Session expired");
				return false;
			}
			return true;
		} catch (Exception e) {
			logger.warn("Token validation failed", e);
			return false;
		}
	}

}
