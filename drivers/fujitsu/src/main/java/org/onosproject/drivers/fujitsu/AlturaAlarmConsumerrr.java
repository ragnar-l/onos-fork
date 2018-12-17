/*
 * Copyright 2016-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onosproject.drivers.fujitsu;

import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;
import org.onosproject.incubator.net.faultmanagement.alarm.*;
import org.onosproject.mastership.MastershipService;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.net.driver.DriverHandler;
import org.onosproject.netconf.NetconfController;
import org.onosproject.netconf.NetconfException;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.onosproject.drivers.fujitsu.FujitsuVoltXmlUtility.REPORT_ALL;
import static org.onosproject.incubator.net.faultmanagement.alarm.Alarm.SeverityLevel;
import static org.slf4j.LoggerFactory.getLogger;


public class AlturaAlarmConsumerrr extends AbstractHandlerBehaviour implements AlarmConsumer {
    private final Logger log = getLogger(getClass());
    private DeviceId ncDeviceId;




    @Override
    public List<Alarm> consumeAlarms() {
        DriverHandler handler = handler();
        NetconfController controller = handler.get(NetconfController.class);
        MastershipService mastershipService = handler.get(MastershipService.class);
        ncDeviceId = handler.data().deviceId();
        checkNotNull(controller, "Netconf controller is null");


        List<Alarm> alarms = new ArrayList<>();
        if (!mastershipService.isLocalMaster(ncDeviceId)) {
            log.warn("Not master for {} Use {} to execute command",
                    ncDeviceId,
                    mastershipService.getMasterFor(ncDeviceId));
            return alarms;
        }


        AlarmService alarmService = this.handler().get(AlarmService.class);
        Iterator it = alarmService.getActiveAlarms().iterator();

        DeviceService deviceService = this.handler().get(DeviceService.class);


        log.info("soy el SEGUNDOOOOOOO");


        return alarms;
    }

    /**
     * Retrieving serial number version of device.
     * @param version the return of show version command
     * @return the serial number of the device
     */
    private String getVecino(String version) {
        log.info(version);
        String serialNumber = StringUtils.substringBetween(version, "<deviceneighbors>", "</deviceneighbors>");
        return serialNumber;
    }

    /**
     * Retrieving serial number version of device.
     * @param version the return of show version command
     * @return the serial number of the device
     */
    private String getTipoTrafico(String version) {
        log.info(version);
        String serialNumber = StringUtils.substringBetween(version, "<tipo_trafico>", "</tipo_trafico>");
        return serialNumber;
    }


}
