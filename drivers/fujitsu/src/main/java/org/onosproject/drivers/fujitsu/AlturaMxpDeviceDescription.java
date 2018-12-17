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


import org.onosproject.net.AnnotationKeys;
import org.onosproject.net.DefaultAnnotations;

import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DeviceDescription;
import org.onosproject.net.device.DeviceDescriptionDiscovery;
import org.onosproject.net.device.PortDescription;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.netconf.*;
import org.slf4j.Logger;
import java.util.List;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.ArrayList;
import org.onosproject.net.Port;
import org.onosproject.net.device.DefaultPortDescription;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DefaultDeviceDescription;
import org.onosproject.net.device.DeviceService;

import org.apache.commons.lang.StringUtils;
import org.onlab.packet.ChassisId;

/**
 * Retrieves the ports (que puertos?) from a Altura MXP40gb device via netconf.
 */
public class AlturaMxpDeviceDescription extends AbstractHandlerBehaviour
        implements DeviceDescriptionDiscovery {

    private final Logger log = getLogger(getClass());

    @Override
    public DeviceDescription discoverDeviceDetails() {

        DeviceService devicecontroller = checkNotNull(handler().get(DeviceService.class));
        DeviceId deviceId = handler().data().deviceId();

        /**
         * Lo primero que hago es ver el tiempo que paso desde que se conecto el dispositivo.
         * El mismo, debe ser mayor a 30 segundos para dar tiempo al mxp a conectarse correctamente con onos.
         */
        String tiempo_conectado = devicecontroller.localStatus(deviceId); //obtengo tiempo transcurrido en string
        tiempo_conectado = tiempo_conectado.replaceAll("\\D+",""); //obtengo solo la parte entera
        if (tiempo_conectado.equals("")) {
            tiempo_conectado = "0"; //si no tiene info, es 0
        }
        int tiempo_conectado_int = Integer.parseInt(tiempo_conectado); // casteo de String a int
        while (tiempo_conectado_int < 30) { //mientras que es menor a 30 segundos, repetir
            devicecontroller = checkNotNull(handler().get(DeviceService.class));
            deviceId = handler().data().deviceId();
            tiempo_conectado = devicecontroller.localStatus(deviceId);
            tiempo_conectado = tiempo_conectado.replaceAll("\\D+","");
            if(tiempo_conectado.equals("")){
                tiempo_conectado = "0";
            }
            tiempo_conectado_int = Integer.parseInt(tiempo_conectado);
        }


        log.info("Descubriendo dispositivo...");
        NetconfController controller = checkNotNull(handler().get(NetconfController.class));
        NetconfSession session = controller.getDevicesMap().get(handler().data().deviceId()).getSession();


        NetconfDevice ncDevice = controller.getDevicesMap().get(handler().data().deviceId());
        if (ncDevice == null) {
            log.error("Internal ONOS Error. Device has been marked as reachable, " +
                            "but deviceID {} is not in Devices Map. Continuing with empty description",
                    handler().data().deviceId());
            return null;
        }


        StringBuilder request = new StringBuilder("<get>");
        request.append("<filter type=\"subtree\">");
        request.append("<mux-state xmlns=\"http://fulgor.com/ns/cli-mxp\">");
        request.append("<device_manufacturer/>");
        request.append("<device_swVersion/>");
        request.append("<device_hwVersion/>");
        request.append("<device_boardId/>");
        request.append("</mux-state>");
        request.append("</filter>");
        request.append("</get>");

        String info_device = null;
        try {
            info_device = session.doWrappedRpc(request.toString());
        } catch (NetconfException e) {
            log.info("NetconfException en AlturaMxpDeviceDescription - Info device");
            throw new IllegalStateException(new NetconfException("Failed to retrieve version info.", e));
        }

        String[] details = new String[4];
        details[0] = getManufacturer(info_device);
        details[1] = getHwVersion(info_device);
        details[2] = getSwVersion(info_device);
        details[3] = serialNumber(info_device);

        StringBuilder suscribe = new StringBuilder("<create-subscription xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">");
        suscribe.append("</create-subscription>");
        try {
            info_device = session.doWrappedRpc(suscribe.toString());
            //session.startSubscription("mux-notify");
        } catch (NetconfException e) {
            log.info("NetconfException en AlturaMxpDeviceDescription - Subscribe");
            throw new IllegalStateException(new NetconfException("Failed to retrieve version info.", e));
        }

        DeviceDescription defaultDescription = new DefaultDeviceDescription(deviceId.uri(), Device.Type.OTN,
                details[0], details[1],
                details[2], details[3],
                new ChassisId(), false, DefaultAnnotations.EMPTY);



        return defaultDescription;

    }


    @Override
    public List<PortDescription> discoverPortDetails() {

        List<PortDescription> ports = new ArrayList<PortDescription>();

        DefaultAnnotations annotationOptics = DefaultAnnotations.builder().set(AnnotationKeys.PORT_NAME, "Optics")
                .build();
        PortDescription optics = DefaultPortDescription.builder()
                .withPortNumber(PortNumber.portNumber(0))
                .isEnabled(true)
                .type(Port.Type.FIBER)
                .portSpeed(1000)
                .annotations(annotationOptics)
                .build();
        ports.add(optics);

        DefaultAnnotations annotationHost = DefaultAnnotations.builder().set(AnnotationKeys.PORT_NAME, "Host").build();
        PortDescription host = DefaultPortDescription.builder()
                .withPortNumber(PortNumber.portNumber(1))
                .isEnabled(true)
                .type(Port.Type.COPPER)
                .portSpeed(1000)
                .annotations(annotationHost)
                .build();
        ports.add(host);

        host = DefaultPortDescription.builder()
                .withPortNumber(PortNumber.portNumber(2))
                .isEnabled(true)
                .type(Port.Type.COPPER)
                .portSpeed(1000)
                .annotations(annotationHost)
                .build();
        ports.add(host);

        host = DefaultPortDescription.builder()
                .withPortNumber(PortNumber.portNumber(3))
                .isEnabled(true)
                .type(Port.Type.COPPER)
                .portSpeed(1000)
                .annotations(annotationHost)
                .build();
        ports.add(host);

        host = DefaultPortDescription.builder()
                .withPortNumber(PortNumber.portNumber(4))
                .isEnabled(true)
                .type(Port.Type.COPPER)
                .portSpeed(1000)
                .annotations(annotationHost)
                .build();
        ports.add(host);

        host = DefaultPortDescription.builder()
                .withPortNumber(PortNumber.portNumber(5))
                .isEnabled(true)
                .type(Port.Type.COPPER)
                .portSpeed(1000)
                .annotations(annotationHost)
                .build();
        ports.add(host);

        return ports;
    }



    /**
     * Retrieving manufacturer of device.
     * @param version the return of show version command
     * @return the manufacturer of the device
     */
    private static String getManufacturer(String version) {
        String manufacturer = StringUtils.substringBetween(version, "<device_manufacturer>", "</device_manufacturer>");
        return manufacturer;
    }


    /**
     * Retrieving sw version of device.
     * @param version the return of show version command
     * @return the sw version of the device
     */
    private static String getSwVersion(String version) {
        String swVersion = StringUtils.substringBetween(version, "<device_swVersion>", "</device_swVersion>");
        return swVersion;
    }


    /**
     * Retrieving hw version of device.
     * @param version the return of show version command
     * @return the hw version of the device
     */
    private static String getHwVersion(String version) {
        String hwVersion = StringUtils.substringBetween(version, "<device_hwVersion>", "</device_hwVersion>");
        return hwVersion;
    }


    /**
     * Retrieving serial number version of device.
     * @param version the return of show version command
     * @return the serial number of the device
     */
    private static String serialNumber(String version) {
        String serialNumber = StringUtils.substringBetween(version, "<device_boardId>", "</device_boardId>");
        return serialNumber;
    }




}
