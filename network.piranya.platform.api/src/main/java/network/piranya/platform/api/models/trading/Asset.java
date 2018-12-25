package network.piranya.platform.api.models.trading;

import java.io.Serializable;

public class Asset implements Comparable<Asset>, Serializable {
	
	private final String symbol;
	public String symbol() { return symbol; }
	
	private final String product;
	public String product() { return product; }
	
	private final AssetType assetType;
	public AssetType assetType() { return assetType; }
	
	private final String extraDetails;
	public String extraDetails() { return extraDetails; }
	
	public Asset(String symbol) {
		this.symbol = symbol;
		
		if (symbol.indexOf('/') >= 0 || symbol.indexOf('>') >= 0) {
			throw new IllegalArgumentException(String.format("Symbol '%s' is an instrument not an asset", symbol));
		}
		
		int separatorIndex = symbol.indexOf('-');
		if (separatorIndex >= 0) {
			this.product = symbol.substring(0, separatorIndex);
			char assetType = symbol.charAt(separatorIndex + 1);
			String extraDetails = symbol.substring(separatorIndex + 2);
			switch (assetType) {
			case 'F':
				this.assetType = AssetType.FUTURE;
				// parse details
				break;
			
			case 'O':
				this.assetType = AssetType.OPTION;
				break;
				
			default:
				throw new IllegalArgumentException(String.format("Unknown asset type '%s' for symbol '%s'", assetType, symbol));
			}
			this.extraDetails = extraDetails;
		} else {
			this.product = symbol;
			this.assetType = AssetType.SPOT;
			this.extraDetails = null;
		}
	}
	
	
	public static enum AssetType { SPOT, FUTURE, OPTION }
	
	@Override
	public int compareTo(Asset o) {
		return symbol().compareTo(o.symbol());
	}
	
	@Override
	public int hashCode() {
		return symbol().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof Asset ? symbol().equals(((Asset)obj).symbol()) : false;
	}
	
	@Override
	public String toString() {
		return symbol();
	}
	
	private static final long serialVersionUID = ("urn:" + Asset.class.getName()).hashCode();
	
}
