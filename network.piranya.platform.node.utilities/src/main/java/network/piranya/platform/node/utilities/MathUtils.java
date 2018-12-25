package network.piranya.platform.node.utilities;

public abstract class MathUtils {
	
	public static double max(double... numbers) {
		if (numbers.length == 0) {
			throw new IllegalArgumentException("numbers must have at least one item");
		}
		
		double currentMax = -Double.MAX_VALUE;
		for (double number : numbers) {
			if (number > currentMax) {
				currentMax = number;
			}
		}
		return currentMax;
	}
	
	
	private MathUtils() { }
	
}
