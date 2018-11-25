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

import com.google.common.collect.ImmutableList;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.onosproject.drivers.utilities.XmlConfigParser;
import org.onosproject.incubator.net.faultmanagement.alarm.*;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.net.driver.DriverHandler;
import org.onosproject.netconf.NetconfController;
import org.onosproject.netconf.NetconfException;
import org.onosproject.mastership.MastershipService;

import org.slf4j.Logger;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import java.nio.charset.StandardCharsets;
import java.io.ByteArrayInputStream;

import static org.onosproject.incubator.net.faultmanagement.alarm.Alarm.SeverityLevel;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.onosproject.drivers.fujitsu.FujitsuVoltXmlUtility.*;
import static org.slf4j.LoggerFactory.getLogger;
import org.onlab.osgi.ServiceDirectory;

import org.onosproject.incubator.net.faultmanagement.alarm.AlarmService;


public class AlturaAlarmConsumer extends AbstractHandlerBehaviour implements AlarmConsumer {
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


        while (it.hasNext()) {
            Alarm b = (Alarm) it.next();
            if (b.deviceId().toString().contains("netconf:172.16.0.") || b.description().contains("netconf-session-start") || b.description().contains("netconf-session-end") || b.description().contains("netconf-config-change") ) {
                alarmService.remove(b.id());
            }
            log.info("ELIMINO");
        }



        log.info("SALGO");


        return alarms;
    }


}
