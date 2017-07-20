package org.javarosa.xml.util;

import org.javarosa.core.services.locale.Localization;
import org.javarosa.core.util.NoLocalizedTextException;

/**
 * Invalid structure error that can _potentially_ be recovered from via
 * advanced user intervention. Useful for notifying the user that the issue lies
 * on the server.
 *
 * @author Phillip Mates (pmates@dimagi.com).
 */
public class ActionableInvalidStructureException extends InvalidStructureException {
    private final String localizationKey;
    private final String[] localizationParameters;

    public ActionableInvalidStructureException(String localizationKey,
                                                String[] localizationParameters,
                                                String message) {
        super(message);
        this.localizationKey = localizationKey;
        this.localizationParameters = localizationParameters;
    }

    @Override
    public String getLocalizedMessage() {
        if (localizationKey != null) {
            try {
                return Localization.get(localizationKey, localizationParameters);
            } catch (NoLocalizedTextException e) {
                return getMessage();
            }
        } else {
            return getMessage();
        }
    }
}
