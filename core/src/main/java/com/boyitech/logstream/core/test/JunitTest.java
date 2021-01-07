package com.boyitech.logstream.core.test;

		import org.junit.platform.launcher.Launcher;
		import org.junit.platform.launcher.LauncherDiscoveryRequest;
		import org.junit.platform.launcher.TestPlan;
		import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
		import org.junit.platform.launcher.core.LauncherFactory;

		import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;
		import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

public class JunitTest {

	public static void main(String[] args) {
		LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
				.selectors(
						selectPackage("new_log_stream_server")
				)
				.filters(
						includeClassNamePatterns(".*Tests")
				)
				.build();

		Launcher launcher = LauncherFactory.create();

		TestPlan testPlan = launcher.discover(request);
		System.out.println(testPlan.containsTests());

	}

}
