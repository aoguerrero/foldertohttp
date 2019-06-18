package io.github.aoguerrero.foldertohttp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import io.github.aoguerrero.foldertohttp.model.FileItem;
import io.github.aoguerrero.foldertohttp.utils.TokenValidator;

@RestController
public class PathAccessController {

	private final Logger logger = LoggerFactory.getLogger(PathAccessController.class);

	@Autowired
	private Environment env;

	@Autowired
	private TokenValidator tokenValidator;

	private String basePath;
	
	private String[] extensions;

	@PostConstruct
	private void init() {
		basePath = env.getRequiredProperty("files.basePath");
		extensions = env.getRequiredProperty("files.extensions").split(",");
	}

	@CrossOrigin(origins = "*")
	@RequestMapping(value = "**/list/**", method = RequestMethod.GET)
	public ResponseEntity<List<FileItem>> list(HttpServletRequest request, @RequestHeader HttpHeaders headers) {

		if (!tokenValidator.validateHeader(headers)) {
			return new ResponseEntity<List<FileItem>>(HttpStatus.UNAUTHORIZED);
		}

		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		
		int listLength = path.indexOf("/list/") + "/list/".length();

		try {
			String fullPath = basePath;
			if (path.length() > listLength) {
				fullPath = basePath + "/" + path.substring(listLength, path.length());
			}
			File fileBasePath = new File(fullPath);
			if (fileBasePath.isDirectory()) {
				List<FileItem> items = new ArrayList<>();
				for (String itemName : fileBasePath.list()) {
					File fileItem = new File(fullPath + "/" + itemName);
					if(fileItem.isDirectory()) {
						items.add(new FileItem("D", itemName, fileItem.length()));
					} else {
						for(String ext : extensions) {
							if(ext.equals("*") || itemName.endsWith(ext)) {
								items.add(new FileItem("F", itemName, fileItem.length()));;
								break;
							}
						}
					}
				}
				return new ResponseEntity<List<FileItem>>(items, HttpStatus.OK);
			} else {
				return new ResponseEntity<List<FileItem>>(HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			logger.error("Error listing path " + path, e);
			return new ResponseEntity<List<FileItem>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@CrossOrigin(origins = "*")
	@RequestMapping(value = "**/download/**", method = RequestMethod.GET)
	public void download(@RequestParam String token, HttpServletRequest request, HttpServletResponse response, @RequestHeader HttpHeaders headers)
			throws IOException {

		if(!tokenValidator.validateToken(token)) {
			response.sendError(401);
			return;
		}


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
			response.setContentType("application/force-download");
			response.setContentLength((int) file.length());
			response.setHeader("Content-Transfer-Encoding", "binary");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");

			InputStream inputStream = new FileInputStream(file);
			IOUtils.copy(inputStream, response.getOutputStream());
			response.flushBuffer();
		} catch (Exception e) {
			logger.error("Error downloading file " + path, e);
			response.sendError(500);
			return;
		}
	}

}
