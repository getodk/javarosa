package org.javarosa.media.image.activity;

import org.javarosa.core.data.IDataPointer;

public interface DataCaptureTransitions {
	void cancel();
	void captured(IDataPointer data);
	void captured(IDataPointer[] data);
	void noCapture();
}
