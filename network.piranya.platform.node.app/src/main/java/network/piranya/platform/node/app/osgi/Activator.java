package network.piranya.platform.node.app.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import network.piranya.platform.node.app.PiranyaAppMain;

public class Activator implements BundleActivator {
	
	@Override
	public void start(BundleContext context) throws Exception {
		PiranyaAppMain.main(new String[0]);
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
	}
	
}
