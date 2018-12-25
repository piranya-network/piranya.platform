package network.piranya.platform.api;

import java.io.Serializable;

public class Version implements Serializable, Comparable<Version> {
	
	private final int major;
	public int major() {
		return major;
	}
	
	private final int minor;
	public int minor() {
		return minor;
	}
	
	private final int micro;
	public int micro() {
		return micro;
	}
	
	public Version(int major, int minor, int micro) {
		this.major = major;
		this.minor = minor;
		this.micro = micro;
	}
	
	public int intValue() {
		return major() * 1000 * 1000 + minor() * 1000 + micro();
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public String toString() {
		return String.format("%s.%s.%s", major, minor, micro);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Version) {
			Version other = (Version)obj;
			return major() == other.major() && minor() == other.minor() && micro() == other.micro();
		} else {
			return false;
		}
	}
	
	@Override
	public int compareTo(Version o) {
		return intValue() - o.intValue();
	}

	private static final long serialVersionUID = ("urn:" + Version.class.getName()).hashCode();
}
