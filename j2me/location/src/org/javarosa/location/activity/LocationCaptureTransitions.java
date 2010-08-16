package org.javarosa.location.activity;

import org.javarosa.j2me.services.LocationCaptureService.Fix;

public interface LocationCaptureTransitions {
	
	void captured(Fix fix);
	void captureCancelled();

}
