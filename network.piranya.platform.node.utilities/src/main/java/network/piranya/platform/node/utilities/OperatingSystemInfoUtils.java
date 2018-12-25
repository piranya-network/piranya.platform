package network.piranya.platform.node.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;

import org.jutils.jhardware.HardwareInfo;

public abstract class OperatingSystemInfoUtils {
	
	public static File appDataDir() {
		if (appDataDir == null) {
			appDataDir = new File(getOsAppDataDir(), "piranya");
			if (!appDataDir.exists()) {
				appDataDir.mkdirs();
			}
		}
		return appDataDir;
	}
	private static File appDataDir;
	
	public static String machineId() {
		if (machineId == null) {
			try {
				File machineIdFile  = new File(appDataDir(), "machine_id.txt");
				if (!machineIdFile.exists()) {
					machineId = CollectionUtils.find(HardwareInfo.getBiosInfo().getFullInfo().entrySet(), e -> e.getKey().toLowerCase().equals("serialnumber"))
							.orElseThrow(() -> new RuntimeException("Bios SerialNumber can not be found")).getValue();
					try (FileWriter writer = new FileWriter(machineIdFile)) {
						writer.write(machineId);
					}
				} else {
					try (BufferedReader reader = new BufferedReader(new FileReader(machineIdFile))) {
						machineId = reader.readLine();
					}
				}
			} catch (IOException ex) {
				throw new RuntimeException("Unexpected IO Exception", ex);
			}
		}
		return machineId;
	}
	private static String machineId;
	
	public static int baseFreePort(int startFrom, int portsAmount) {
		int basePort = startFrom;
		while (true) {
			boolean allPortsAreFree = true;
			for (int i = 0; i < portsAmount; i++) {
				if (!isLocalPortFree(startFrom + i)) {
					allPortsAreFree = false;
					break;
				}
			}
			
			if (allPortsAreFree) {
				return basePort;
			} else {
				basePort += portsAmount;
			}
		}
	}
	
	private static boolean isLocalPortFree(int port) {
	    try {
	        new ServerSocket(port).close();
	        return true;
	    } catch (IOException ex) {
	        return false;
	    }
	}
	
	protected static String getOsAppDataDir() {
		String workingDirectory;
		//here, we assign the name of the OS, according to Java, to a variable...
		String OS = (System.getProperty("os.name")).toUpperCase();
		//to determine what the workingDirectory is.
		//if it is some version of Windows
		if (OS.contains("WIN"))
		{
		    //it is simply the location of the "AppData" folder
		    workingDirectory = System.getenv("AppData");
		}
		//Otherwise, we assume Linux or Mac
		else
		{
		    //in either case, we would start in the user's home directory
		    workingDirectory = System.getProperty("user.home");
		    //if we are on a Mac, we are not done, we look for "Application Support"
		    workingDirectory += "/Library/Application Support";
		}
		return workingDirectory;
	}
	
	
	private OperatingSystemInfoUtils() { }
	
}
