package strategies;
import automail.Clock;
import automail.MailItem;
import automail.PriorityMailItem;
import automail.StorageTube;

public class MyRobotBehaviour implements IRobotBehaviour {
	
	private boolean strong;
	private int newPriority; // Used if we are notified that a priority item has arrived. 
		
	public MyRobotBehaviour(boolean strong) {
		this.strong = strong;
		newPriority = 0;
	}
	
	public void startDelivery() {
		newPriority = 0;
	}
	
	@Override
    public void priorityArrival(int priority, int weight) {
    	if (priority > newPriority) newPriority = priority;  // Only the strong robot will deliver priority items so weight of no interest
    }
 
	private int tubePriority(StorageTube tube) {  // Assumes at least one item in tube
		MailItem item = tube.peek();
		return (item instanceof PriorityMailItem) ? ((PriorityMailItem) item).getPriorityLevel() : 0;
	}
	
	@Override
	public boolean returnToMailRoom(StorageTube tube) {
		if (tube.isEmpty()) {
			return false; // Empty tube means we are returning anyway
		} else {
			// Return true for the strong robot if the one waiting is higher priority than the one we have
			// Assumes that the one at the top of the tube has the highest priority
			return strong && newPriority > tubePriority(tube);
		}
	}
	
}
