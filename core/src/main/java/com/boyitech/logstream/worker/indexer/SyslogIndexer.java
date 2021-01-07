package com.boyitech.logstream.worker.indexer;

import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.util.IndexHelper;
import com.boyitech.logstream.core.util.enums.Month;
import com.boyitech.logstream.core.util.enums.SyslogFacility;
import com.boyitech.logstream.core.util.enums.SyslogSeverity;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import com.boyitech.logstream.core.worker.indexer.FormatException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class SyslogIndexer extends BaseIndexer {

	private String timeZone = "+08:00";

	public SyslogIndexer(BaseWorkerConfig config) {
		super(config);
		if(config!=null && config.getConfig()!=null && config.getConfig().get("timeZone")!=null) {
			this.timeZone = config.getConfig().get("timeZone").toString();
		}
	}

	public SyslogIndexer(String indexerID,BaseWorkerConfig config) {
		super(indexerID,config);
		if(config!=null && config.getConfig()!=null && config.getConfig().get("timeZone")!=null) {
			this.timeZone = config.getConfig().get("timeZone").toString();
		}
	}

	@Override
	public boolean register() {
		return true;
	}

	@Override
	public void tearDown() {

	}

	@Override
	public boolean format(Event event) {
		// RFC 3164
		// <PRI>TIMESTAMP HOSTNAME APP-NAME[PROCID]: sourcetype="SOURCETYPE" key1="val1" key2="val2" etc.
		// RFC 5424
		// <PRI>VER TIMESTAMP HOSTNAME APP-NAME PROCID MSGID [SOURCETYPE@NM_IANA key1="val1" key2="val2" etc.] MSG
		DateTime now = DateTime.now();
			List<String> tags = new ArrayList<String>();
			Map<String, Object> formated = event.getFormat();
			boolean is3164 = true;
			event.setLogType(this.getType());
			String message = event.getMessage();
			formated.put("message", message);
			formated.put("Metafield.type", this.getType());
			if(event.getSource()!=null) {
				formated.put("Metafield.source", event.getSource());
		}
		try {
			if(message.startsWith("<")) { // 以<开始的才作为syslog日志处理
				int index = message.indexOf(">");
				if(index == -1) {
					throw new FormatException("格式错误，无法处理");
				}else {
					// 处理PRI
					String priStr = message.substring(1, index);
					int pri = Integer.parseInt(priStr);
					int facility = pri/8;
					formated.put("facility.name", SyslogFacility.getFacility(facility));
					formated.put("facility.code", facility);
					int severity = pri%8;
					formated.put("severity.name", SyslogSeverity.getSeverity(severity));
					formated.put("severity.code", severity);
					formated.put("Metafield.loglevel", severity);
					message = message.substring(index + 1);
					String[] tmp = this.nextSplitBySingleSpace(message);
					// 处理可能存在的Version
					if(IndexHelper.MONTHS.contains(tmp[0])) { // RFC 3164
						is3164 = true;
						formated.put("Metafield.type", "syslog-3164");
					}else { // RFC 5424
						is3164 = false;
						formated.put("Metafield.type", "syslog-5424");
						formated.put("version", tmp[0]);
						message = tmp[1];
					}
					// 处理TIMESTAMP
					tmp = this.nextSplitBySingleSpace(message);
					if(is3164) {
						String month = tmp[0];
						tmp = this.nextSplitBySingleSpace(tmp[1]);
						String day = tmp[0];
						tmp = this.nextSplitBySingleSpace(tmp[1]);
						String time = tmp[0];
						String dateTimeStr = now.getYear() + "-" + Month.of(month).getValue() + "-" + day + "T" + time + timeZone;
						DateTime dt = DateTime.parse(dateTimeStr).withZone(DateTimeZone.UTC);
						formated.put("@timestamp", dt.toString(IndexHelper.DateTimeFormatter));
						event.setTimestamp(dt);
					}else {
						String dateTime = tmp[0];
						DateTime dt = DateTime.parse(dateTime).withZone(DateTimeZone.UTC);
						formated.put("@timestamp", dt.toString(IndexHelper.DateTimeFormatter));
						event.setTimestamp(dt);
					}
					// 处理HOSTNAME
					tmp = this.nextSplitBySingleSpace(tmp[1]);
					String hostName = tmp[0];
					formated.put("hostname", hostName);
					if(!formated.containsKey("hostname")) {
						formated.put("Metafield.object", hostName);
					}
					// 根据不同类型处理后续部分
					if(is3164) {
						// 处理MSG部分
						String msg = tmp[1];
						Matcher nameWithId = IndexHelper.SYSLOG3164APPNAMEWITHPROCID.matcher(msg);
						Matcher name = IndexHelper.SYSLOG3164APPNAME.matcher(msg);
						if(nameWithId.lookingAt()) { // contains APPNAME[PROCID]:
							String s = nameWithId.group();
							String[] p = s.split("\\[");
							formated.put("Metafield.subject", p[0]);
							formated.put("procid", p[1].replaceAll("\\]", "").replace(":", ""));
							formated.put("Metafield.description", msg.substring(nameWithId.end()+1));
						}else if(name.lookingAt()) { //contains APPNAME:
							formated.put("Metafield.subject", name.group().replaceAll(":", ""));
							formated.put("Metafield.description", msg.substring(name.end()+1));
						}else { //only MSG
							formated.put("Metafield.description", tmp[1]);
						}
					}else {
						//APP-NAME PROCID MSGID [SOURCETYPE@NM_IANA key1="val1" key2="val2" etc.] MSG
						tmp = this.nextSplitBySingleSpace(tmp[1]);
						String appName = tmp[0];
						formated.put("Metafield.subject", appName);
						tmp = this.nextSplitBySingleSpace(tmp[1]);
						formated.put("procid", tmp[0]);
						tmp = this.nextSplitBySingleSpace(tmp[1]);
						formated.put("msgid", tmp[0]);
						tmp = this.nextSplitBySingleSpace(tmp[1]);
						formated.put("Metafield.description", tmp[1]);
					}
				}
			}else {
				throw new FormatException("日志格式错误");
			}
		} catch (FormatException e) {
			tags.add(e.getMessage());
			this.recordException("",e);
		}
		// 检查是否有错误发生并记录到格式化结果中
		if(tags.size()>0) {
			formated.put("tags", tags);
			return false;
		}else {
			return true;
		}

	}

	private String[] nextSplitBySingleSpace(String str) throws FormatException {
		if(str==null || str.equals("")) {
			throw new FormatException("日志格式错误");
		}
		String[] result = new String[2];
		str = str.trim();
		int i = str.indexOf(" ");
		result[0] = str.substring(0, i);
		result[1] = str.substring(i + 1);
		return result;
	}

	public static Map getMapping() {
		String mapping = "{\"properties\":{"
				+ "\"@timestamp\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
				+ "\"receivedAt\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
				+ "\"portedAt\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
				+ "\"version\":{\"type\":\"keyword\"},"
				+ "\"tags\":{\"type\":\"text\"},"
				+ "\"message\":{\"type\":\"text\"},"
				+ "\"Metafield\":{"
					+ "\"properties\": {"
						+ "\"type\":{\"type\":\"keyword\"},"
						+ "\"category\":{\"type\":\"keyword\"},"
						+ "\"subject\":{\"type\":\"keyword\"},"
						+ "\"object\":{\"type\":\"keyword\"},"
						+ "\"loglevel\":{\"type\":\"keyword\", \"fields\": {\"int\": {\"type\": \"integer\"}}},"
						+ "\"source\":{\"type\":\"keyword\"},"
						+ "\"description\":{\"type\":\"text\", \"fields\": {\"raw\": {\"type\": \"keyword\"}}}"
					+ "}"
				+ "},"
				+ "\"hostname\":{\"type\":\"keyword\"},"
				+ "\"procid\":{\"type\":\"keyword\"},"
				+ "\"msgid\":{\"type\":\"keyword\"},"
				+ "\"tags\":{\"type\":\"keyword\"},"
				+ "\"facility\":{"
					+ "\"properties\": {"
						+ "\"name\":{\"type\":\"keyword\"},"
						+ "\"code\":{\"type\":\"integer\"}"
					+ "}"
				+ "},"
				+ "\"severity\":{"
					+ "\"properties\": {"
						+ "\"name\":{\"type\":\"keyword\"},"
						+ "\"code\":{\"type\":\"integer\"}"
					+ "}"
				+ "}"
				+ "}}";
		return GsonHelper.fromJson(mapping);
	}

	public static String getType() {
		return "syslog";
	}


}
