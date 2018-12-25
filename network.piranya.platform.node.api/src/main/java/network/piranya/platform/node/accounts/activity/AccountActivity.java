package network.piranya.platform.node.accounts.activity;

import static network.piranya.platform.node.utilities.CollectionUtils.*;

import java.util.ArrayList;
import java.util.List;

import network.piranya.platform.node.api.networking.nodes.NodeContacts;

public class AccountActivity {
	
	private final List<AccessPoint> accessPoints;
	public List<AccessPoint> accessPoints() {
		return accessPoints;
	}
	
	public AccessPoint mainAccessPoint() {
		return max(accessPoints(), ap -> ap.lastActivityTime()).get();
	}
	
	public AccountActivity(List<AccessPoint> accessPoints) {
		this.accessPoints = accessPoints;
	}
	
	public AccountActivity() {
		this(new ArrayList<>());
	}
	
	
	public AccountActivity updateAccessPoint(AccessPoint accessPoint) {
		List<AccessPoint> accessPoints = new ArrayList<>(accessPoints());
		find(accessPoints(), ap -> ap.nodeContacts().nodeId().equals(accessPoint.nodeContacts().nodeId())).ifPresent(ap -> accessPoints.remove(ap));
		accessPoints.add(0, accessPoint);
		// sort
		
		return new AccountActivity(accessPoints);
	}
	
	
	public static class AccessPoint {
		
		private final NodeContacts nodeContacts;
		public NodeContacts nodeContacts() {
			return nodeContacts;
		}
		
		private final long lastActivityTime;
		public long lastActivityTime() {
			return lastActivityTime;
		}
		
		public AccessPoint(NodeContacts nodeContacts, long lastActivityTime) {
			this.nodeContacts = nodeContacts;
			this.lastActivityTime = lastActivityTime;
		}
	}
	
	public static class TradingActivity {
		
		private final long fillVolume;
		public long fillVolume() {
			return fillVolume;
		}
		
		public TradingActivity(long fillVolume) {
			this.fillVolume = fillVolume;
		}
	}
}
