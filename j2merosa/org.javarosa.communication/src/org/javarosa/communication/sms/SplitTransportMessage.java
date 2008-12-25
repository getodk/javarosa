/**
 * Interface:	SplitTransportMessage
 * @author Vijay Umapathy (vijayu)
 * 
 * Used to split TransportMessages into messages with smaller payloads, based on the
 * fact that SMS limits messages to a length of 140-160 characters.
 * 
 * Specific methods not yet implemented.
 * 
 * Since this is only used by SMS for the moment, we can leave it in the SMS package
 * for now, but later should be moved to the core.services.transport package
 */


package org.javarosa.communication.sms;

public interface SplitTransportMessage {

}
