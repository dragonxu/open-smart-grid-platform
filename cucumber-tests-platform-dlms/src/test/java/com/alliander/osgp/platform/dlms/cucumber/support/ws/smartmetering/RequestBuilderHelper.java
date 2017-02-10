package com.alliander.osgp.platform.dlms.cucumber.support.ws.smartmetering;

import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import com.alliander.osgp.platform.cucumber.core.ScenarioContext;
import com.alliander.osgp.platform.cucumber.steps.Keys;

public class RequestBuilderHelper {

    private RequestBuilderHelper() {
        // Private constructor for utility class.
    }

    public static String getCorrelationUidFromScenarioContext() {
        final String correlationUid = (String) ScenarioContext.Current().get(Keys.KEY_CORRELATION_UID);
        if (correlationUid == null) {
            throw new AssertionError("ScenarioContext must contain the correlation UID for key \""
                    + Keys.KEY_CORRELATION_UID + "\" before creating an async request.");
        }
        return correlationUid;
    }

    public static String getDeviceIdentificationFromStepData(final Map<String, String> requestParameters) {
        final String deviceIdentification = requestParameters.get(Keys.KEY_DEVICE_IDENTIFICATION);
        if (deviceIdentification == null) {
            throw new AssertionError("The Step DataTable must contain the device identification for key \""
                    + Keys.KEY_DEVICE_IDENTIFICATION + "\" when creating a request.");
        }
        return deviceIdentification;
    }

    public static byte[] hexDecodeDeviceKey(final String hexString, final String keyType) {
        if (hexString == null) {
            return null;
        }
        try {
            return Hex.decodeHex(hexString.toCharArray());
        } catch (final DecoderException e) {
            throw new AssertionError("Key value \"" + hexString + "\" for \"" + keyType + "\" is not hex binary", e);
        }
    }
}
