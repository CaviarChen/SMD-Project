package strategies;

import automail.MailItem;
import automail.StorageTube;

/**
 * addToPool is called when there are mail items newly arrived at the building to add to the MailPool or
 * if a robot returns with some undelivered items - these are added back to the MailPool.
 * The data structure and algorithms used in the MailPool is your choice.
 * 
 */
public interface IMailPool {
	
	/**
     * Adds an item to the mail pool
     * @param mailItem the mail item being added.
     */
    void addToPool(MailItem mailItem);
    
    /**
     * @param tube refers to the pack the robot uses to deliver mail.
     * @param strong is whether the tube belongs to a strong robot.
     */
	void fillStorageTube(StorageTube tube, boolean strong);
    
}
