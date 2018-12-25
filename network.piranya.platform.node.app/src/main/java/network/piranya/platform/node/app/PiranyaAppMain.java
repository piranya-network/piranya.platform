package network.piranya.platform.node.app;

import java.io.File;

import network.piranya.infrastructure.dcm4j.impl.jvm.PureJvmComponentModel;
import network.piranya.platform.api.accounting.NodeId;
import network.piranya.platform.node.api.booting.NetworkNodeConfig;
import network.piranya.platform.node.core.booting.NetworkNode;
import network.piranya.platform.node.core.local_infrastructure.LocalServicesProviderImpl;
import network.piranya.platform.node.utilities.OperatingSystemInfoUtils;

public class PiranyaAppMain {
	
	public static void main(String[] args) {
		int nodeIndex = 0;
		if (args.length > 0) {
			nodeIndex = Integer.parseInt(args[0]);
		}
		long t1 = System.currentTimeMillis();
		String nodeId = OperatingSystemInfoUtils.machineId() + ":" + nodeIndex;
		NetworkNodeConfig config = new NetworkNodeConfig(new NodeId(OperatingSystemInfoUtils.machineId(), nodeIndex), OperatingSystemInfoUtils.baseFreePort(7000 + (nodeIndex * 10), 10),
				new File(OperatingSystemInfoUtils.appDataDir(), Integer.valueOf(nodeIndex).toString()));
		long t2 = System.currentTimeMillis();
		
		PiranyaApp app = new PiranyaApp(config, "D:/src/network.piranya/app/terminal.web", new PureJvmComponentModel());
		try {
			NetworkNode node = new NetworkNode(null, config, new LocalServicesProviderImpl(config, null), new PureJvmComponentModel(), (eventType, data) -> {});
			node.boot();
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		long t3 = System.currentTimeMillis();
		
		//String appDir = AppDirsFactory.getInstance().getUserDataDir("appName", "1.0.0", "author");
		System.out.println(OperatingSystemInfoUtils.appDataDir());
		
		System.out.println(OperatingSystemInfoUtils.machineId());
		
		System.out.println(t3 - t1);
		System.out.println(t3 - t2);
		//nodeId = machineId + arg.toInt.else(0)
	}
	
}
