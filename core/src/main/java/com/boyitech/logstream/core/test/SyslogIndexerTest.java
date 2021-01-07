package com.boyitech.logstream.core.test;

import com.boyitech.logstream.core.info.Event;
import com.boyitech.logstream.worker.indexer.SyslogIndexer;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class SyslogIndexerTest {

	public static void main(String args[]) throws UnknownHostException {
		// RFC 3164
		// <PRI>TIMESTAMP HOSTNAME APP-NAME[PROCID]: sourcetype="SOURCETYPE" key1="val1" key2="val2" etc.
		// RFC 5424
		// <PRI>VER TIMESTAMP HOSTNAME APP-NAME PROCID MSGID [SOURCETYPE@NM_IANA key1="val1" key2="val2" etc.] MSG
		SyslogIndexer indexer = new SyslogIndexer(null);
		List<String> test = new ArrayList<String>();
//		test.add("<86>May  5 15:48:06 localhost sshd[57625]: Accepted password for root from 172.17.250.250 port 52927 ssh2");
//		test.add("<34>Oct 11 22:14:15 mymachine su: 'su root' failed for lonvick on /dev/pts/8");
//		test.add("<13>Feb  5 17:32:18 10.0.0.99 Use the BFG!");
//		test.add("<34>1 2003-10-11T22:14:15.003Z mymachine.example.com su - ID47 - BOM'su root' failed for lonvick on /dev/pts/8");
//		test.add("<165>1 2003-08-24T05:14:15.000003-07:00 192.0.2.1 myproc 8710 - - %% It's time to make the do-nuts.");
//		test.add("<165>1 2003-10-11T22:14:15.003Z mymachine.example.com evntslog - ID47 [exampleSDID@32473 iut=\"3\" eventSource=\"Application\""
//				+ " eventID=\"1011\"] BOMAn application event log entry...");
		test.add("<38>May 24 16:25:52 ys systemd-logind: Removed session 2270.");
		for(String s : test) {
			Event event = new Event();
			event.setMessage(s);
			indexer.format(event);
//			System.out.println(GsonHelper.toJson(event.getFormat()));
//			break;
		}

	}

}
