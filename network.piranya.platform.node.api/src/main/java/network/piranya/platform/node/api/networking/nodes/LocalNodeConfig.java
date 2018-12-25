package network.piranya.platform.node.api.networking.nodes;

import network.piranya.platform.api.lang.Optional;
import network.piranya.platform.node.api.local_infrastructure.storage.LocalStorage;
import network.piranya.platform.node.api.local_infrastructure.storage.PropertiesStore;

public class LocalNodeConfig {
	
	public Optional<Zone> zone() {
		return zone;
	}
	public void updateZone(Zone zone) {
		db().updateProperty(ZONE_KEY, zone.stringify());
		this.zone = Optional.of(zone);
	}
	private Optional<Zone> zone;
	
	
	public LocalNodeConfig(LocalStorage localStorage) {
		this.db = localStorage.properties("node_config");
		
		this.zone = db().get(ZONE_KEY).map(zoneStr -> Zone.parse(zoneStr));
	}
	
	private final PropertiesStore db;
	protected PropertiesStore db() {
		return db;
	}
	
	
	private static final String ZONE_KEY = "zone";
	
}
