package org.javarosa.xml.util;

/**
 * @author ctsims
 */
public class UnfullfilledRequirementsException extends Exception {

    private final int severity;
    private final int requirement;
    /**
     * Indicates that this exception was thrown due to an attempt to install an app that was
     * already installed
     */
    private boolean isDuplicateException;

    /**
     * Version Numbers if version is incompatible *
     */
    private final int maR;
    private final int miR;
    private final int maA;
    private final int miA;

    public UnfullfilledRequirementsException(String message, int severity) {
        this(message, severity, -1, -1, -1, -1, -1);
    }

    public UnfullfilledRequirementsException(String message, int severity, boolean isDuplicate) {
        this(message, severity, -1, -1, -1, -1, -1);
        this.isDuplicateException = isDuplicate;
    }

    public UnfullfilledRequirementsException(String message, int severity, int requirement) {
        this(message, severity, requirement, -1, -1, -1, -1);
    }

    /**
     * Constructor for unfulfilled version requirements.
     */
    public UnfullfilledRequirementsException(String message, int severity,
                                             int requirement,
                                             int requiredMajor, int requiredMinor, int availableMajor, int availableMinor) {
        super(message);
        this.severity = severity;
        this.requirement = requirement;

        this.maR = requiredMajor;
        this.miR = requiredMinor;

        this.maA = availableMajor;
        this.miA = availableMinor;
    }

    /**
     * @return A human readable version string describing the required version
     */
    public String getRequiredVersionString() {
        return maR + "." + miR;
    }

    /**
     * @return A human readable version string describing the available version
     */
    public String getAvailableVesionString() {
        return maA + "." + miA;
    }

    public int getSeverity() {
        return severity;
    }

    public int getRequirementCode() {
        return requirement;
    }

    /**
     * @return true if this exception was thrown due to an attempt at installing a duplicate app
     */
    public boolean isDuplicateException() {
        return this.isDuplicateException;
    }
}
