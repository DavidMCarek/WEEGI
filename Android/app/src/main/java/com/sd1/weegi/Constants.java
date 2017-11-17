package com.sd1.weegi;

import java.util.UUID;

import static java.util.UUID.fromString;

/**
 * Created by DMCar on 10/3/2017.
 */

public final class Constants {
    public static final String WEEGI_DATA_LOCATION = "Weegi";

    public static final class RFduinoService {
        public static final UUID UUID_SERVICE = fromString("00002220-0000-1000-8000-00805f9b34fb");
        public static final class Characteristics {
            public static final UUID UUID_READ = fromString("00002221-0000-1000-8000-00805f9b34fb");
            public static final UUID UUID_WRITE = fromString("00002222-0000-1000-8000-00805f9b34fb");
            public static final UUID UUID_DISCONNECT = fromString("00002223-0000-1000-8000-00805f9b34fb");
        }
    }

    public static final class GenericAccessService {
        public static final UUID UUID_SERVICE = fromString("00001800-0000-1000-8000-00805f9b34fb");
        public static final class Characteristics {
            public static final UUID UUID_DEVICE_NAME = fromString("00002a00-0000-1000-8000-00805f9b34fb");
            public static final UUID UUID_APPEARANCE = fromString("00002a01-0000-1000-8000-00805f9b34fb");
            public static final UUID UUID_PREFERRED_PERIPHERAL_CONNECTION_PARAMETERS = fromString("00002a04-0000-1000-8000-00805f9b34fb");
        }
    }
}
