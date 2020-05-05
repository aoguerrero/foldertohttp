package io.github.aoguerrero.foldertohttp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

@RestController
public class PathAccessController {

	private final Logger logger = LoggerFactory.getLogger(PathAccessController.class);

	@Autowired
	private Environment env;

	private String basePath;

	@PostConstruct
	private void init() {
		basePath = env.getRequiredProperty("files.basePath");
	}

	@CrossOrigin(origins = "*")
	@RequestMapping(value = "**/download/**", method = RequestMethod.GET)
	public void download(HttpServletRequest request, HttpServletResponse response, @RequestHeader HttpHeaders headers)
			throws IOException {

		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		int downloadLength = path.indexOf("/download/") + "/download/".length();

		try {
			String fullPath = basePath + "/" + path.substring(downloadLength, path.length());
			File file = new File(fullPath);
			if (!file.exists()) {
				response.sendError(404);
				return;
			}
			if (file.isDirectory()) {
				response.sendError(400);
				return;
			}

			response.setContentType("image/png");
			response.setContentLength((int) file.length());
			response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
			response.setHeader("Pragma", "no-cache");

			InputStream inputStream = new FileInputStream(file);
			IOUtils.copy(inputStream, response.getOutputStream());
			response.flushBuffer();
			
			/* FIXME */
			Thread.sleep(5000);
			
			try {
				file.renameTo(new File(fullPath + ".deleted"));
				File nodisponible = new File(basePath + "/nodisponible.png");
				Files.copy(nodisponible.toPath(), (new File(fullPath)).toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (Exception e) {
				logger.error("Error renaming file", e);
			}
		} catch (Exception e) {
			logger.error("Error downloading file {}", path, e);
			response.sendError(500);
			return;
		}
	}

}
