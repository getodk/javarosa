package org.javarosa.core.model;

public enum ControlType {
    UNTYPED       (-1),
    INPUT         (1),
    SELECT_ONE    (2),
    SELECT_MULTI  (3),
    TEXTAREA      (4),
    SECRET        (5),
    RANGE         (6),
    UPLOAD        (7),
    SUBMIT        (8),
    TRIGGER       (9),
    IMAGE_CHOOSE  (10),
    LABEL         (11),
    AUDIO_CAPTURE (12),
    VIDEO_CAPTURE (13),
    OSM_CAPTURE   (14),
    FILE_CAPTURE  (15),
    RANK          (16);

    public final int value;

    ControlType(int value) {
        this.value = value;
    }
}