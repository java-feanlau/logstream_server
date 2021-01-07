package com.boyitech.logstream.core.test;

import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.worker.indexer.SNMPTrapIndexer;
import net.percederberg.mibble.MibLoaderException;
import net.percederberg.mibble.value.ObjectIdentifierValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SNMPTrapIndexerTest {

	public static void main(String args[]) throws IOException, MibLoaderException {
		SNMPTrapIndexer indexer = new SNMPTrapIndexer(null);
//		System.out.println(indexer.register());
		List<String> test = new ArrayList<String>();
//		test.add("CommandResponderEvent[securityModel=2, securityLevel=1, maxSizeResponsePDU=65535, pduHandle=PduHandle[1122554893], stateReference=StateReference[msgID=0,pduHandle=PduHandle[1122554893],securityEngineID=null,securityModel=null,securityName=public,securityLevel=1,contextEngineID=null,contextName=null,retryMsgIDs=null], pdu=TRAP[requestID=1122554893, errorStatus=Success(0), errorIndex=0, VBS[1.3.6.1.2.1.1.3.0 = 1 day, 10:17:36.78; 1.3.6.1.6.3.1.1.4.1.0 = 1.3.6.1.4.1.2021.251.1; 1.3.6.1.2.1.1.6.0 = test]], messageProcessingModel=1, securityName=public, processed=false, peerAddress=172.17.250.213/39200, transportMapping=org.snmp4j.transport.DefaultUdpTransportMapping@5c70f5bc, tmStateReference=null]1.3.6.1.2.1.1.3.0 = 1 day, 10:17:36.78;1.3.6.1.6.3.1.1.4.1.0 = 1.3.6.1.4.1.2021.251.1;1.3.6.1.2.1.1.6.0 = test;");
		for(String s : test) {
			Event e = new Event();
			e.setMessage(s);
			indexer.format(e);
		}
		ObjectIdentifierValue sym = indexer.loader.getRootOid().find("1.3.6.1.4.1.2021.251.1");
//		System.out.println(sym.getSymbol().getName());
//		MibLoader loader = new MibLoader();
//		File f1 = new File(Paths.get(FilePathHelper.ROOTPATH, "mib", "ietf", "TOKEN-RING-RMON-MIB").toString());
//		File f2 = new File(Paths.get(FilePathHelper.ROOTPATH, "mib", "ietf", "UCD-SNMP-MIB").toString());
//		File f3 = new File(Paths.get(FilePathHelper.ROOTPATH, "mib", "ietf", "TRIP-TC-MIB").toString());
//		loader.addAllDirs(Paths.get(FilePathHelper.ROOTPATH, "mib").toFile());
//		loader.load(f2);
//		loader.load(f1);
//		loader.load(f3);
//		for(File f : loader.getDirs()) {
//			System.out.println(f.getAbsolutePath());
//		}
//	    ObjectIdentifierValue iso = loader.getRootOid();
//	    ObjectIdentifierValue match = iso.find("1.3.6.1.4.1.2021.251.1");
//		System.out.println(match.getSymbol().getName());
	}

}
