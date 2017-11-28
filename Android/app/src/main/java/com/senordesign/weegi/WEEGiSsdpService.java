package com.senordesign.weegi;

import io.resourcepool.jarpic.client.response.SsdpResponse;
import io.resourcepool.jarpic.model.SsdpService;

import java.util.Date;

/**
 * This represents a SSDP Service.
 *
 * @author Loïc Ortola on 05/08/2017
 */
public class WEEGiSsdpService extends SsdpService {
    private Date packetReceivedDate;

    /**
     * @param response the raw SsdpResponse
     */
    public WEEGiSsdpService(SsdpResponse response) {
        super(response);
        packetReceivedDate = new Date();
    }

    public Date getPacketReceivedDate() {
        return packetReceivedDate;
    }
}
