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
import com.google.common.collect.Iterables;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.StringUtils;
import org.onosproject.drivers.utilities.XmlConfigParser;
import org.onosproject.incubator.net.faultmanagement.alarm.*;
import org.onosproject.net.Device;
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

        DeviceService deviceService = this.handler().get(DeviceService.class);


        while (it.hasNext()) {
            Alarm b = (Alarm) it.next();
            Device localdevice = deviceService.getDevice(b.deviceId());
            if ((localdevice.manufacturer().equals("ALTURA")) && (b.description().contains("netconf-session-start") || b.description().contains("netconf-session-end") || b.description().contains("netconf-config-change") ) ) {
                alarmService.remove(b.id());
                log.info("ELIMINO");
            }
        }

        /**
         * Tengo que esperar hasta que el dispositivo se conecte con ONOS.
         * Una vez se conecte, ONOS consulta por la descripcion del dispositivo. Si el mismo es OTN, puedo seguir en getLinks.
         * De lo contrario, retorno null.
         */
        Device localdevice = deviceService.getDevice(ncDeviceId);

        if ( (localdevice.swVersion().equals("1.0")) && (localdevice.type().toString().equals("OTN")) ) {
            log.debug("Dispositivo no listo");
        }
        else{
            log.debug("Dispositivo listo");
            return alarms;
        }


        /**
         * Pregunto al dispositivo local quien es su vecino.
         */
        String reply = null;
        try {
            StringBuilder request = new StringBuilder("<mux-config xmlns=\"http://fulgor.com/ns/cli-mxp\">");
            request.append("<deviceneighbors/>");
            request.append("</mux-config>");

            reply = controller
                    .getDevicesMap()
                    .get(ncDeviceId)
                    .getSession()
                    .get(request.toString(), REPORT_ALL);
        } catch (NetconfException e) {
            log.error("Cannot communicate to device {} exception {}", ncDeviceId, e);
        }

        String vecino = serialNumber(reply);

        /**
         * Se busca en los dispositivos actualmente conectados si hay alguno con un numero de serie que coincida con el indicado por el dispositivo como vecino.
         */
        com.google.common.base.Optional<Device> dev = Iterables.tryFind(
                deviceService.getAvailableDevices(),
                input -> input.serialNumber().equals(vecino));
        if (!dev.isPresent()) {
            log.info("Device with chassis ID {} does not exist");
            return alarms;
        }


        /**
         * Pregunto al dispositivo local que configuracion tiene.
         */
        String local = null;
        try {
            StringBuilder request = new StringBuilder("<mux-config xmlns=\"http://fulgor.com/ns/cli-mxp\">");
            request.append("<tipo_trafico/>");
            request.append("</mux-config>");

            local = controller
                    .getDevicesMap()
                    .get(ncDeviceId)
                    .getSession()
                    .get(request.toString(), REPORT_ALL);
        } catch (NetconfException e) {
            log.error("Cannot communicate to device {} exception {}", ncDeviceId, e);
        }

        local = serialNumberr(local);

        /**
         * Pregunto al dispositivo vecino que configuracion tiene.
         */
        String vecin = null;
        try {
            StringBuilder request = new StringBuilder("<mux-config xmlns=\"http://fulgor.com/ns/cli-mxp\">");
            request.append("<tipo_trafico/>");
            request.append("</mux-config>");
            log.info("ENTRO ACASAAA");
            vecin = controller
                    .getDevicesMap()
                    .get(dev)
                    .getSession()
                    .get(request.toString(), REPORT_ALL);
        } catch (NetconfException e) {
            log.error("Cannot communicate to device {} exception {}", dev, e);
        }

        log.info("la respuesta esssss {}",vecin);

        vecin = serialNumberr(vecin);


        log.info(local);
        log.info(vecin);

        if (local.toString().equals(vecin.toString())) {
            log.info("SON IGUALESsssssssSSsS");
        }
        else {
            log.info("SON Distintosssssssssss");
        }

        log.info("SALGO");


        return alarms;
    }

    /**
     * Retrieving serial number version of device.
     * @param version the return of show version command
     * @return the serial number of the device
     */
    private String serialNumber(String version) {
        log.info(version);
        String serialNumber = StringUtils.substringBetween(version, "<deviceneighbors>", "</deviceneighbors>");
        return serialNumber;
    }

    /**
     * Retrieving serial number version of device.
     * @param version the return of show version command
     * @return the serial number of the device
     */
    private String serialNumberr(String version) {
        log.info(version);
        String serialNumber = StringUtils.substringBetween(version, "<tipo_trafico>", "</tipo_trafico>");
        return serialNumber;
    }


}
