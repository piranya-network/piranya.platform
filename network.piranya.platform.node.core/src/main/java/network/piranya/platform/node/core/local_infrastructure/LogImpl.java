package network.piranya.platform.node.core.local_infrastructure;

import java.util.logging.Level;
import java.util.logging.Logger;

import network.piranya.platform.node.api.local_infrastructure.Log;

public class LogImpl implements Log {
	
	@Override
	public void info(String message) {
		log.log(Level.INFO, message);
	}
	
	@Override
	public void info(String message, Object... args) {
		log.log(Level.INFO, String.format(message, args));
	}
	
	@Override
	public void warning(String message) {
		log.log(Level.WARNING, message);
	}
	
	@Override
	public void warning(String message, Throwable error) {
		log.log(Level.WARNING, message, error);
	}
	
	
	public LogImpl(Object owner) {
		this.log = Logger.getLogger(owner.getClass().getSimpleName());
	}
	
	private final Logger log;
	
}
