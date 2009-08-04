/**
 * 
 */
package org.javarosa.core.util;

/**
 * I can't believe MIDP doesn't have a Queue. There's a stack,
 * but no Queue.
 * 
 * Queue is a FIFO collections object.
 * 
 * @author ctsims
 *
 */
public class Queue {
	LinkedObject head;
	LinkedObject tail;
	
	public Queue() {
		
	}
	
	public void queue(Object o) {
		if(head == null) {
			head = new LinkedObject(o);
			tail = new LinkedObject(o);
		} else {
			LinkedObject lo = new LinkedObject(o);
			tail.next = lo;
			this.tail = lo;
		}
	}
	
	public Object poll() {
		if(head == null) {
			return null;
		}
		Object o = head.o;
		head=head.next;
		return o;
	}
	
	public boolean empty() {
		return head == null;
	}
	
	public Object peek() {
		if(head == null) {
			return null;
		}
		return head.o;
	}
	
	private class LinkedObject {
		Object o;
		LinkedObject next;
		
		public LinkedObject(Object o) {
			this.o = o;
		}
	}
}
