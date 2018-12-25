package network.piranya.platform.api.exceptions;

public class QueueNotExistsException extends PiranyaException {
	
	public QueueNotExistsException(String queueId) {
		super(String.format("Queue '%s' does not exist", queueId));
	}
	
	private static final long serialVersionUID = ("urn:" + QueueNotExistsException.class.getName()).hashCode();
	
}
