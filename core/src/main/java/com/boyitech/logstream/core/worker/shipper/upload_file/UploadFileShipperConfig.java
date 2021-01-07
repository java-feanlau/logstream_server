package com.boyitech.logstream.core.worker.shipper.upload_file;

import java.util.List;
import java.util.Map;

import com.boyitech.logstream.core.worker.shipper.BaseShipperConfig;

public class UploadFileShipperConfig extends BaseShipperConfig {

	private List<String> filePaths;

	public UploadFileShipperConfig(Map config) {
		super(config);
		filePaths = (List)config.get("file_path");
	}

	public List<String> getFilePaths() {
		return filePaths;
	}

	public void setFilePaths(List<String> filePaths) {
		this.filePaths = filePaths;
	}

}