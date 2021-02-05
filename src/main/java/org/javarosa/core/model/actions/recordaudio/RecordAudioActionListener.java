package org.javarosa.core.model.actions.recordaudio;

import org.javarosa.core.model.instance.TreeReference;

public interface RecordAudioActionListener {
    void recordAudioTriggered(TreeReference absoluteTargetRef, String quality);
}