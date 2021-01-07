package com.boyitech.logstream.worker.indexer;

import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.core.util.FilePathHelper;
import com.boyitech.logstream.core.util.GsonHelper;
import com.boyitech.logstream.core.worker.BaseWorkerConfig;
import com.boyitech.logstream.core.worker.indexer.BaseIndexer;
import net.percederberg.mibble.Mib;
import net.percederberg.mibble.MibLoader;
import net.percederberg.mibble.MibLoaderException;
import net.percederberg.mibble.value.ObjectIdentifierValue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SNMPTrapIndexer extends BaseIndexer {

	public MibLoader loader;
	public Mib mib;

	public SNMPTrapIndexer(BaseWorkerConfig config) {
		super(config);
	}

	public SNMPTrapIndexer(String indexerID, BaseWorkerConfig config) {
		super(indexerID, config);
	}

	@Override
	public boolean register() {
		loader = new MibLoader();
		loader.addAllDirs(Paths.get(FilePathHelper.ROOTPATH, "mib").toFile());
		File dir = new File(Paths.get(FilePathHelper.ROOTPATH, "mib").toString());
		if(dir.isDirectory()) {
			String[] strs = dir.list();
			for(String pathStr : strs) {
				try {
					mib = loader.load(pathStr);
				} catch (IOException | MibLoaderException e) {
					this.recordException("",e);
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	@Override
	public void tearDown() {

	}

	@Override
	public boolean format(Event event) {
		String message = event.getMessage();
		Map formated = new HashMap();
		List<String> failure = new ArrayList<String>();
		event.setFormat(formated);
		if (event.getSource() != null) {
			formated.put("Metafield.source", event.getSource());
			formated.put("Metafield.subject", event.getSource());
		}
		formated.put("Metafield.loglevel", 0);
		formated.put("Metafield.description", message);
		formated.put("Metafield.type", "SNMP Trap");
		formated.put("@timestamp", event.getTimestamp().toString());
		Map m = GsonHelper.getGson().fromJson(message, Map.class);
		List list = new ArrayList();
		for(Object e : m.entrySet()) {
			Entry en = (Entry)e;
			Map map = new HashMap();
			ObjectIdentifierValue sym = loader.getRootOid().find(en.getKey().toString());
			map.put("oid", en.getKey().toString());
			map.put("name", sym.getName());
			if(sym.getName().equals("snmpTrapOID")) {
				ObjectIdentifierValue type = loader.getRootOid().find(en.getValue().toString());
				map.put("value", type.getName());
			}else {
				map.put("value", en.getValue());
			}
			list.add(map);
		}
		formated.put("oid_info", list);
		if(failure.size()>0) {
			formated.put("failure", failure);
			return false;
		}else {
			return true;
		}
	}

	public static Map getMapping() {
		String mapping = "{\"properties\":{"
				+ "\"@timestamp\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
				+ "\"receivedAt\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
				+ "\"portedAt\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},"
				+ "\"version\":{\"type\":\"keyword\"},"
				+ "\"failure\":{\"type\":\"keyword\"},"
				+ "\"log-body\":{\"type\":\"keyword\"},"
				+ "\"oid_info\":{\"type\":\"nested\"},"
				+ "\"Metafield\":{"
					+ "\"properties\": {"
						+ "\"type\":{\"type\":\"keyword\"},"
						+ "\"subject\":{\"type\":\"keyword\"},"
						+ "\"object\":{\"type\":\"keyword\"},"
						+ "\"loglevel\":{\"type\":\"keyword\", \"fields\": {\"int\": {\"type\": \"integer\"}}},"
						+ "\"source\":{\"type\":\"keyword\"},"
						+ "\"description\":{\"type\":\"text\", \"fields\": {\"raw\": {\"type\": \"keyword\"}}}"
					+ "}"
				+ "}"
				+ "}}";
		return GsonHelper.fromJson(mapping);
	}

	public static String getType() {
		return "snmp_trap";
	}

}
