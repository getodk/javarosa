/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.core.model;

/**
 * Constants shared throughout classes in the containing package.
 * <p/>
 * Where possible use {@link DataType} instead of the DATATYPE_*
 * “int enum” fields.
 */
public class Constants {

    public static final String EMPTY_STRING = "";

    public static final int NO_SELECTION = -1;

    /** ID not set to a value */
    public static final int NULL_ID = -1;

    /** Connection type not specified */
    public static final int CONNECTION_NONE = 0;

    /** Infrared connection */
    public static final int CONNECTION_INFRARED = 1;

    /** Bluetooth connection */
    public static final int CONNECTION_BLUETOOTH = 2;

    /** Data cable connection. Can be USB or Serial. */
    public static final int CONNECTION_CABLE = 3;

    /** Over The Air or HTTP Connection */
    public static final int CONNECTION_OTA = 4;

    public static final int DATATYPE_UNSUPPORTED    = DataType.UNSUPPORTED.value;
    public static final int DATATYPE_NULL           = DataType.NULL.value;
    public static final int DATATYPE_TEXT           = DataType.TEXT.value;
    public static final int DATATYPE_INTEGER        = DataType.INTEGER.value;
    public static final int DATATYPE_DECIMAL        = DataType.DECIMAL.value;
    public static final int DATATYPE_DATE           = DataType.DATE.value;
    public static final int DATATYPE_TIME           = DataType.TIME.value;
    public static final int DATATYPE_DATE_TIME      = DataType.DATE_TIME.value;
    public static final int DATATYPE_CHOICE         = DataType.CHOICE.value;
    /** A list of items used for selecting multiple answers or ordering them */
    public static final int DATATYPE_MULTIPLE_ITEMS = DataType.MULTIPLE_ITEMS.value;
    /** The same as {@link Constants#DATATYPE_MULTIPLE_ITEMS} (for backwards compatibility) */
    public static final int DATATYPE_CHOICE_LIST    = DataType.MULTIPLE_ITEMS.value;
    public static final int DATATYPE_BOOLEAN        = DataType.BOOLEAN.value;
    public static final int DATATYPE_GEOPOINT       = DataType.GEOPOINT.value;
    public static final int DATATYPE_BARCODE        = DataType.BARCODE.value;
    public static final int DATATYPE_BINARY         = DataType.BINARY.value;
    public static final int DATATYPE_LONG           = DataType.LONG.value;
    public static final int DATATYPE_GEOSHAPE       = DataType.GEOSHAPE.value;
    public static final int DATATYPE_GEOTRACE       = DataType.GEOTRACE.value;

    public static final int CONTROL_UNTYPED         = ControlType.UNTYPED.value;
    public static final int CONTROL_INPUT           = ControlType.INPUT.value;
    public static final int CONTROL_SELECT_ONE      = ControlType.SELECT_ONE.value;
    public static final int CONTROL_SELECT_MULTI    = ControlType.SELECT_MULTI.value;
    public static final int CONTROL_TEXTAREA        = ControlType.TEXTAREA.value;
    public static final int CONTROL_SECRET          = ControlType.SECRET.value;
    public static final int CONTROL_RANGE           = ControlType.RANGE.value;
    public static final int CONTROL_UPLOAD          = ControlType.UPLOAD.value;
    public static final int CONTROL_SUBMIT          = ControlType.SUBMIT.value;
    public static final int CONTROL_TRIGGER         = ControlType.TRIGGER.value;
    public static final int CONTROL_IMAGE_CHOOSE    = ControlType.IMAGE_CHOOSE.value;
    public static final int CONTROL_LABEL           = ControlType.LABEL.value;
    public static final int CONTROL_AUDIO_CAPTURE   = ControlType.AUDIO_CAPTURE.value;
    public static final int CONTROL_VIDEO_CAPTURE   = ControlType.VIDEO_CAPTURE.value;
    public static final int CONTROL_OSM_CAPTURE     = ControlType.OSM_CAPTURE.value;
    /** generic upload */
    public static final int CONTROL_FILE_CAPTURE    = ControlType.FILE_CAPTURE.value;
    public static final int CONTROL_RANK            = ControlType.RANK.value;

    /* constants for xform tags */
    public static final String XFTAG_UPLOAD = "upload";
}
