package org.javarosa.j2me.log;

import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;

public interface HandledItemStateListener extends ItemStateListener {

	void _itemStateChanged (Item i);
	
}
