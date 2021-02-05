package org.javarosa.core.model.actions.recordaudio;

public class RecordAudioActions {
    private RecordAudioActions() {

    }

    /**
     * Global reference to a client class that want to get updates about triggered record audio actions. Recording audio
     * needs to be handled entirely client-side and there's no convenient object to hook into to get information
     * about triggered actions.
     */
    private static RecordAudioActionListener recordAudioListener;

    public static void setRecordAudioListener(RecordAudioActionListener listener) {
        recordAudioListener = listener;
    }

    public static RecordAudioActionListener getRecordAudioListener() {
        return recordAudioListener;
    }
}
