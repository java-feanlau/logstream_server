package com.boyitech.logstream.core.worker.shipper.upload_file;

import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.filter_rule.MultilineStateMachine;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.shipper.BaseShipper;
import com.boyitech.logstream.core.worker.shipper.BaseShipperConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class UploadFileShipper extends BaseShipper {

	private UploadFileShipperConfig uploadFileConfig;
	private List<Path> filePaths = new ArrayList<Path>();
	private boolean isDone = false;
	private MultilineStateMachine multilineStateMachine;

	public UploadFileShipper(BaseShipperConfig config) {
		super(config);
		uploadFileConfig = (UploadFileShipperConfig) config;
		if(uploadFileConfig.isMultiline()) {
			multilineStateMachine = new MultilineStateMachine(uploadFileConfig.getMultilineRule());
		}
	}

	public UploadFileShipper(String shipperID,BaseShipperConfig config) {
		super(shipperID,config);
		uploadFileConfig = (UploadFileShipperConfig) config;
		if(uploadFileConfig.isMultiline()) {
			multilineStateMachine = new MultilineStateMachine(uploadFileConfig.getMultilineRule());
		}
	}

	@Override
	public boolean register() {
		for (String pathString : uploadFileConfig.getFilePaths()) {
			Path path = Paths.get(pathString);
			filePaths.add(path);
		}
		return true;
	}

	@Override
	public void tearDown() {
		// TODO Auto-generated method stub

	}

	@Override
	public void execute() throws InterruptedException {
		for (Path path : filePaths) {
			File file = new File(path.toUri());
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(file), 2048); // 设置缓存
				try {
					List<Event> list = new ArrayList<Event>(uploadFileConfig.getBatchSize());
					String tempString = null;
					while ((tempString = reader.readLine()) != null) {
						if(multilineStateMachine!=null) { // 如果有多行合并
							if(multilineStateMachine.in(tempString)) {
								tempString = multilineStateMachine.out();
							}else {
								continue;
							}
						}
						list.add(initEvent(tempString, path));
						if(list.size()==uploadFileConfig.getBatchSize()) {
							count.addAndGet(list.size());
							lv1Cache.put(list);
							list = new ArrayList<Event>(uploadFileConfig.getBatchSize());
						}
					}
					if(multilineStateMachine!=null) { // 循环结束后检查状态机中是否还有可以输出的数据
						String m = multilineStateMachine.merge();
						if(m!=null) {
							list.add(initEvent(m, path));
						}
					}
					if(list.size()>0) // 发送最后获取的数据
						lv1Cache.put(list);
				} finally {
					if (reader != null) {
						try {
							reader.close();
						} catch (IOException e) {
							LOGGER.debug("关闭文件读取时发生异常", e);
						}
					}
				}
			} catch (IOException e) {
				LOGGER.error(file.getAbsolutePath()+" 读取失败" , e);
			}
		}
		isDone = true;
	}

		private Event initEvent(String message, Path path) {
		if(message==null || message.equals(""))
			return null;
		Event event = new Event();
		event.setMessage(message);
		event.setSource(path.toString());
		if(uploadFileConfig.isChangeIndex())
			event.setIndex(uploadFileConfig.getIndex());
		if(this.mark!=null) {
			event.setMark(this.mark);
		}
		return event;
	}

	@Override
	public void run() {
		try {
			execute();
		} catch (InterruptedException e) {
			LOGGER.error("shipper worker运行被中断");
		}
		if(countDownLatch!=null) {
			countDownLatch.countDown();
		}
		LOGGER.debug(Thread.currentThread().getName()+"结束运行");
	}

	public boolean isDone() {
		return this.isDone;
	}

}
