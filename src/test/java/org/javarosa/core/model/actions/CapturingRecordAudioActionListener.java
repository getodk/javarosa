/*
 * Copyright 2021 ODK
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.javarosa.core.model.actions;

import org.javarosa.core.model.actions.recordaudio.RecordAudioActionListener;
import org.javarosa.core.model.instance.TreeReference;

public class CapturingRecordAudioActionListener implements RecordAudioActionListener {
    private TreeReference absoluteTargetRef;
    private String quality;

    @Override
    public void recordAudioTriggered(TreeReference absoluteTargetRef, String quality) {
        this.absoluteTargetRef = absoluteTargetRef;
        this.quality = quality;
    }

    public TreeReference getAbsoluteTargetRef() {
        return absoluteTargetRef;
    }

    public String getQuality() {
        return quality;
    }
}
