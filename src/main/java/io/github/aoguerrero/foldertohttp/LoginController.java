package io.github.aoguerrero.foldertohttp;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.github.aoguerrero.foldertohttp.model.LoginData;
import io.github.aoguerrero.foldertohttp.utils.EncDec;

@RestController
public class LoginController {

	private final Logger logger = LoggerFactory.getLogger(LoginController.class);

	@Autowired
	private EncDec encDec;

	@Autowired
	private Environment env;

	private String username;
	private String password;
	private SimpleDateFormat sdf;

	@PostConstruct
	public void init() {
		username = env.getRequiredProperty("login.username");
		password = env.getRequiredProperty("login.password");
		sdf = new SimpleDateFormat("ddMMyyyyHHmmss");
	}

	@CrossOrigin(origins = "*")
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public ResponseEntity<String> login(@RequestBody LoginData loginData) {
		try {
			if (loginData.getUsername().equals(username) && loginData.getPassword().equals(password)) {
				String tLogin = sdf.format(new Date());
				String token = encDec.encrypt(tLogin);
				return new ResponseEntity<String>(token, HttpStatus.OK);
			} else {
				return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);
			}
		} catch (Exception e) {
			logger.error("Login error", e);
			return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
