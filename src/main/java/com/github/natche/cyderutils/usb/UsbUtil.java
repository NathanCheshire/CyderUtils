package com.github.natche.cyderutils.usb;

import com.google.common.collect.ImmutableList;
import com.github.natche.cyderutils.constants.CyderRegexPatterns;
import com.github.natche.cyderutils.exceptions.FatalException;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.process.ProcessResult;
import com.github.natche.cyderutils.process.ProcessUtil;
import com.github.natche.cyderutils.strings.CyderStrings;
import com.github.natche.cyderutils.threads.CyderThreadFactory;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Utility methods related to usb devices. */
public final class UsbUtil {
    /** The PowerShell executable name. */
    private static final String POWER_SHELL = "powershell.exe";

    /** The command to list USB devices connected to the host computer. */
    private static final String usbConnectedDevicesCommand
            = "Get-PnpDevice -PresentOnly | Where-Object { $_.InstanceId -match '^USB' }";

    /** The number of lines from the usb query output to ignore. */
    private static final int headerLines = 2;

    /** The number of members contained in a {@link UsbDevice}. */
    private static final int usbDeviceMemberLength = 4;

    /** The name for the executor service returned by {@link #getUsbDevices()}. */
    private static final String USB_DEVICE_THREAD_NAME = "USB Device Getter";

    /** Suppress default constructor. */
    private UsbUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns a list of usb devices connected to this computer.
     *
     * @return a list of usb devices connected to this computer
     */
    public static Future<ImmutableList<UsbDevice>> getUsbDevices() {
        ArrayList<UsbDevice> ret = new ArrayList<>();

        String command = POWER_SHELL + " " + usbConnectedDevicesCommand;

        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory(USB_DEVICE_THREAD_NAME)).submit(() -> {
            Future<ProcessResult> futureResult = ProcessUtil.getProcessOutput(command);
            while (!futureResult.isDone()) {
                Thread.onSpinWait();
            }

            ProcessResult result = futureResult.get();

            if (result.containsErrors()) {
                throw new FatalException("Exception whilst trying to query USB devices");
            }

            ImmutableList<String> standardOutput = result.getStandardOutput();
            if (standardOutput.size() > headerLines) {
                standardOutput.stream().filter(line -> !line.isEmpty()).skip(headerLines).forEach(line -> {
                    String[] parts = line.split(CyderRegexPatterns.multipleWhiteSpaceRegex);
                    if (parts.length == usbDeviceMemberLength) {
                        int index = 0;
                        String status = parts[index++];
                        String clazz = parts[index++];
                        String friendlyName = parts[index++];
                        String instanceId = parts[index];
                        ret.add(new UsbDevice(status, clazz, friendlyName, instanceId));
                    }
                });
            }

            return ImmutableList.copyOf(ret);
        });
    }
}
