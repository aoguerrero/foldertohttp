package io.github.aoguerrero.foldertohttp;

import java.util.Calendar;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import io.github.aoguerrero.foldertohttp.model.LoginData;
import io.github.aoguerrero.foldertohttp.model.Token;
import io.github.aoguerrero.foldertohttp.utils.EncDec;

@RestController
public class LoginController {

	private final Logger logger = LoggerFactory.getLogger(LoginController.class);

	@Autowired
	private EncDec encDec;

	@Autowired
	private Environment env;

	@Value("${login.username}")
	private String username;

	@Value("${login.password}")
	private String password;

	@Value("${jwt.issuer}")
	private String issuer;

	@Value("${jwt.key}")
	private String key;

	@CrossOrigin(origins = "*")
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public ResponseEntity<Token> login(@RequestBody LoginData loginData) {
		try {
			if (loginData.getUsername().equals(username) && loginData.getPassword().equals(password)) {
				Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
				utcCalendar.add(Calendar.MINUTE, 30);
				String token = JWT.create().withClaim("username", username).withExpiresAt(utcCalendar.getTime())
						.withIssuer(issuer).sign(Algorithm.HMAC256(key));
				return new ResponseEntity<Token>(new Token(token), HttpStatus.OK);
			} else {
				return new ResponseEntity<Token>(HttpStatus.UNAUTHORIZED);
			}
		} catch (Exception e) {
			logger.error("Login error", e);
			return new ResponseEntity<Token>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
