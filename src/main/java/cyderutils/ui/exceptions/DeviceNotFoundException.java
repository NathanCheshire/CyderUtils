package cyderutils.ui.exceptions;

/**
 * An exception for when a device such as a monitor or keyboard cannot be located.
 */
public class DeviceNotFoundException extends IllegalArgumentException {
    /**
     * Constructs a new DeviceNotFoundException using the provided error message.
     */
    public DeviceNotFoundException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Constructs a new DeviceNotFoundException from the provided exception.
     */
    public DeviceNotFoundException(Exception e) {
        super(e);
    }
}
