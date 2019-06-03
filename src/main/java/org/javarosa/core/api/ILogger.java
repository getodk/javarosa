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

/**
 *
 */
package org.javarosa.core.api;

import java.io.IOException;
import java.util.Date;
import org.javarosa.core.log.IFullLogSerializer;
import org.javarosa.core.log.StreamLogSerializer;

/**
 * <b>Warning:</b> This class is unused and should remain that way. It will be removed in a future release.
 *
 * IIncidentLogger's are used for instrumenting applications to identify usage
 * patterns, usability errors, and general trajectories through applications.
 *
 * @author Clayton Sims
 * @deprecated Use {@link org.slf4j.LoggerFactory#getLogger(Class)} instead
 */
@Deprecated
public interface ILogger {
    /**
     * @deprecated Use {@link org.slf4j.Logger#info} instead
     */
    @Deprecated
    public void log(String type, String message, Date logDate);

    /**
     * @deprecated Use {@link org.slf4j.Logger} instead
     */
    @Deprecated
    public void clearLogs();

    /**
     * @deprecated Use {@link org.slf4j.Logger} instead
     */
    @Deprecated
    public <T> T serializeLogs(IFullLogSerializer<T> serializer);

    /**
     * @deprecated Use {@link org.slf4j.Logger} instead
     */
    @Deprecated
    public void serializeLogs(StreamLogSerializer serializer) throws IOException;

    /**
     * @deprecated Use {@link org.slf4j.Logger} instead
     */
    @Deprecated
    public void serializeLogs(StreamLogSerializer serializer, int limit) throws IOException;

    /**
     * @deprecated Use {@link org.slf4j.Logger} instead
     */
    @Deprecated
    public void panic();

    /**
     * @deprecated Use {@link org.slf4j.Logger} instead
     */
    @Deprecated
    public int logSize();

    /**
     * @deprecated Use {@link org.slf4j.Logger} instead
     */
    @Deprecated
    public void halt();
}
