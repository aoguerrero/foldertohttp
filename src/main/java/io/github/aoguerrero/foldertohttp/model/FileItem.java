package io.github.aoguerrero.foldertohttp.model;

public class FileItem {

	private final String type;
	private final String name;
	private final Long size;

	public FileItem(String type, String name, Long size) {
		this.type = type;
		this.name = name;
		this.size = size;
	}

	public FileItem(String type, String name) {
		this.type = type;
		this.name = name;
		this.size = 0l;
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public Long getSize() {
		return size;
	}
}
