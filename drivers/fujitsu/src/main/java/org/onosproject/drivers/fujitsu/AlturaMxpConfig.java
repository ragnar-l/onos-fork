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
import org.onosproject.incubator.net.faultmanagement.alarm.Alarm;
import org.onosproject.incubator.net.faultmanagement.alarm.AlarmId;
import org.onosproject.incubator.net.faultmanagement.alarm.AlarmService;
import org.onosproject.incubator.net.faultmanagement.alarm.DefaultAlarm;
import org.onosproject.mastership.MastershipService;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.behaviour.MxpConfig;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.net.driver.DriverHandler;
import org.onosproject.netconf.NetconfController;
import org.onosproject.netconf.NetconfException;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.onosproject.drivers.fujitsu.FujitsuVoltXmlUtility.*;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Implementation to get and set parameters available in vOLT
 * through the Netconf protocol.
 */
public class AlturaMxpConfig extends AbstractHandlerBehaviour
        implements MxpConfig {

    private final Logger log = getLogger(AlturaMxpConfig.class);

    @Override
    public String setTipoTrafico(String tipo_trafico) {
        DriverHandler handler = handler();
        DeviceService deviceService = this.handler().get(DeviceService.class);
        NetconfController controller = handler.get(NetconfController.class);
        MastershipService mastershipService = handler.get(MastershipService.class);
        DeviceId ncDeviceId = handler.data().deviceId();
        checkNotNull(controller, "Netconf controller is null");
        String reply = null;

        if (!mastershipService.isLocalMaster(ncDeviceId)) {
            log.warn("Not master for {} Use {} to execute command",
                     ncDeviceId,
                     mastershipService.getMasterFor(ncDeviceId));
            return null;
        }

        while ( !deviceService.getDevice(ncDeviceId).type().toString().equals("OTN") ) {
            log.debug("No termino de conectarse el dispositivo, espero.");
        }


        if ((!tipo_trafico.equals("xge")) && !(tipo_trafico.equals("otu2"))) {
            log.error("Invalid value of arguments. value: {} result: {}",tipo_trafico, ((tipo_trafico != "xge") && (tipo_trafico != "otu2")) );
            return null;
        }

        try {
            StringBuilder request = new StringBuilder("<edit-config xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">");
            request.append("<target>");
            request.append("<running/>");
            request.append("</target>");
            request.append("<default-operation>merge</default-operation>");
            request.append("<test-option>set</test-option>");
            request.append("<config>");
            request.append("<mux-config xmlns=\"http://fulgor.com/ns/cli-mxp\">");
            request.append("<tipo_trafico xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"replace\">");
            request.append(tipo_trafico);
            request.append("</tipo_trafico>");
            request.append("</mux-config>");
            request.append("</config>");
            request.append("</edit-config>");


            reply = controller
                        .getDevicesMap()
                        .get(ncDeviceId)
                        .getSession()
                        .doWrappedRpc(request.toString());
        } catch (NetconfException e) {
            log.error("Cannot communicate to device {} exception {}", ncDeviceId, e);
        }
        return reply;
    }

    @Override
    public String setTipoFecLinea(String tipo_fec_linea) {
        DriverHandler handler = handler();
        NetconfController controller = handler.get(NetconfController.class);
        MastershipService mastershipService = handler.get(MastershipService.class);
        DeviceId ncDeviceId = handler.data().deviceId();
        checkNotNull(controller, "Netconf controller is null");
        String reply = null;

        if (!mastershipService.isLocalMaster(ncDeviceId)) {
            log.warn("Not master for {} Use {} to execute command",
                    ncDeviceId,
                    mastershipService.getMasterFor(ncDeviceId));
            return null;
        }

        DeviceService deviceService = this.handler().get(DeviceService.class);
        while ( !deviceService.getDevice(ncDeviceId).type().toString().equals("OTN") ) {
            log.debug("No termino de conectarse el dispositivo, espero.");
        }


        if ((!tipo_fec_linea.equals("gfec")) && !(tipo_fec_linea.equals("cerofec"))) {
            log.error("Invalid value of arguments. value: {} result: {}",tipo_fec_linea, ((tipo_fec_linea != "gfec") && (tipo_fec_linea != "cerofec")) );
            return null;
        }

        try {
            StringBuilder request = new StringBuilder("<edit-config xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">");
            request.append("<target>");
            request.append("<running/>");
            request.append("</target>");
            request.append("<default-operation>merge</default-operation>");
            request.append("<test-option>set</test-option>");
            request.append("<config>");
            request.append("<mux-config xmlns=\"http://fulgor.com/ns/cli-mxp\">");
            request.append("<tipo_fec_linea xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"replace\">");
            request.append(tipo_fec_linea);
            request.append("</tipo_fec_linea>");
            request.append("</mux-config>");
            request.append("</config>");
            request.append("</edit-config>");

            reply = controller
                    .getDevicesMap()
                    .get(ncDeviceId)
                    .getSession()
                    .doWrappedRpc(request.toString());
        } catch (NetconfException e) {
            log.error("Cannot communicate to device {} exception {}", ncDeviceId, e);
        }
        return reply;
    }


    @Override
    public String setTipoFecCliente(String tipo_fec_cliente) {
        DriverHandler handler = handler();
        NetconfController controller = handler.get(NetconfController.class);
        MastershipService mastershipService = handler.get(MastershipService.class);
        DeviceId ncDeviceId = handler.data().deviceId();
        checkNotNull(controller, "Netconf controller is null");
        String reply = null;

        if (!mastershipService.isLocalMaster(ncDeviceId)) {
            log.warn("Not master for {} Use {} to execute command",
                    ncDeviceId,
                    mastershipService.getMasterFor(ncDeviceId));
            return null;
        }

        DeviceService deviceService = this.handler().get(DeviceService.class);
        while ( !deviceService.getDevice(ncDeviceId).type().toString().equals("OTN") ) {
            log.debug("No termino de conectarse el dispositivo, espero.");
        }


        if ( (!tipo_fec_cliente.equals("gfec_cliente")) && !(tipo_fec_cliente.equals("cerofec_cliente")) && !(tipo_fec_cliente.equals("nulofec_cliente")) ) {
            log.error("Invalid value of arguments.");
            return null;
        }

        try {
            StringBuilder request = new StringBuilder("<edit-config xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">");
            request.append("<target>");
            request.append("<running/>");
            request.append("</target>");
            request.append("<default-operation>merge</default-operation>");
            request.append("<test-option>set</test-option>");
            request.append("<config>");
            request.append("<mux-config xmlns=\"http://fulgor.com/ns/cli-mxp\">");
            request.append("<tipo_fec_cliente xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"replace\">");
            request.append(tipo_fec_cliente);
            request.append("</tipo_fec_cliente>");
            request.append("</mux-config>");
            request.append("</config>");
            request.append("</edit-config>");


            reply = controller
                    .getDevicesMap()
                    .get(ncDeviceId)
                    .getSession()
                    .doWrappedRpc(request.toString());
        } catch (NetconfException e) {
            log.error("Cannot communicate to device {} exception {}", ncDeviceId, e);
        }
        return reply;
    }


    @Override
    public String setEdfaOutPower(String edfa_output_power) {
        DriverHandler handler = handler();
        NetconfController controller = handler.get(NetconfController.class);
        MastershipService mastershipService = handler.get(MastershipService.class);
        DeviceId ncDeviceId = handler.data().deviceId();
        checkNotNull(controller, "Netconf controller is null");
        String reply = null;

        if (!mastershipService.isLocalMaster(ncDeviceId)) {
            log.warn("Not master for {} Use {} to execute command",
                    ncDeviceId,
                    mastershipService.getMasterFor(ncDeviceId));
            return null;
        }

        DeviceService deviceService = this.handler().get(DeviceService.class);
        while ( !deviceService.getDevice(ncDeviceId).type().toString().equals("OTN") ) {
            log.debug("No termino de conectarse el dispositivo, espero.");
        }


        try {
            StringBuilder request = new StringBuilder("<edit-config xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">");
            request.append("<target>");
            request.append("<running/>");
            request.append("</target>");
            request.append("<default-operation>merge</default-operation>");
            request.append("<test-option>set</test-option>");
            request.append("<config>");
            request.append("<mux-config xmlns=\"http://fulgor.com/ns/cli-mxp\">");
            request.append("<edfa_output_power_config xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"replace\">");
            request.append(edfa_output_power);
            request.append("</edfa_output_power_config>");
            request.append("</mux-config>");
            request.append("</config>");
            request.append("</edit-config>");

            reply = controller
                    .getDevicesMap()
                    .get(ncDeviceId)
                    .getSession()
                    .doWrappedRpc(request.toString());
        } catch (NetconfException e) {
            log.error("Cannot communicate to device {} exception {}", ncDeviceId, e);
        }
        return reply;
    }


    @Override
    public String setTimeToNotify(String time_notify_config) {
        DriverHandler handler = handler();
        NetconfController controller = handler.get(NetconfController.class);
        MastershipService mastershipService = handler.get(MastershipService.class);
        DeviceId ncDeviceId = handler.data().deviceId();
        checkNotNull(controller, "Netconf controller is null");
        String reply = null;

        if (!mastershipService.isLocalMaster(ncDeviceId)) {
            log.warn("Not master for {} Use {} to execute command",
                    ncDeviceId,
                    mastershipService.getMasterFor(ncDeviceId));
            return null;
        }

        DeviceService deviceService = this.handler().get(DeviceService.class);
        while ( !deviceService.getDevice(ncDeviceId).type().toString().equals("OTN") ) {
            log.debug("No termino de conectarse el dispositivo, espero.");
        }


        try {
            StringBuilder request = new StringBuilder("<edit-config xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">");
            request.append("<target>");
            request.append("<running/>");
            request.append("</target>");
            request.append("<default-operation>merge</default-operation>");
            request.append("<test-option>set</test-option>");
            request.append("<config>");
            request.append("<mux-config xmlns=\"http://fulgor.com/ns/cli-mxp\">");
            request.append("<time_notify_config xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"replace\">");
            request.append(time_notify_config);
            request.append("</time_notify_config>");
            request.append("</mux-config>");
            request.append("</config>");
            request.append("</edit-config>");

            reply = controller
                    .getDevicesMap()
                    .get(ncDeviceId)
                    .getSession()
                    .doWrappedRpc(request.toString());
        } catch (NetconfException e) {
            log.error("Cannot communicate to device {} exception {}", ncDeviceId, e);
        }
        return reply;
    }


    @Override
    public String setValueEdfaNotify(String value_notify_config) {
        DriverHandler handler = handler();
        NetconfController controller = handler.get(NetconfController.class);
        MastershipService mastershipService = handler.get(MastershipService.class);
        DeviceId ncDeviceId = handler.data().deviceId();
        checkNotNull(controller, "Netconf controller is null");
        String reply = null;

        if (!mastershipService.isLocalMaster(ncDeviceId)) {
            log.warn("Not master for {} Use {} to execute command",
                    ncDeviceId,
                    mastershipService.getMasterFor(ncDeviceId));
            return null;
        }


        DeviceService deviceService = this.handler().get(DeviceService.class);
        while ( !deviceService.getDevice(ncDeviceId).type().toString().equals("OTN") ) {
            log.debug("No termino de conectarse el dispositivo, espero.");
        }

        try {
            StringBuilder request = new StringBuilder("<edit-config xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">");
            request.append("<target>");
            request.append("<running/>");
            request.append("</target>");
            request.append("<default-operation>merge</default-operation>");
            request.append("<test-option>set</test-option>");
            request.append("<config>");
            request.append("<mux-config xmlns=\"http://fulgor.com/ns/cli-mxp\">");
            request.append("<value_notify_config xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"replace\">");
            request.append(value_notify_config);
            request.append("</value_notify_config>");
            request.append("</mux-config>");
            request.append("</config>");
            request.append("</edit-config>");

            reply = controller
                    .getDevicesMap()
                    .get(ncDeviceId)
                    .getSession()
                    .doWrappedRpc(request.toString());
        } catch (NetconfException e) {
            log.error("Cannot communicate to device {} exception {}", ncDeviceId, e);
        }
        return reply;
    }


    @Override
    public String setValueRxPowerNotify(String value_rx_power_notify_config) {
        DriverHandler handler = handler();
        NetconfController controller = handler.get(NetconfController.class);
        MastershipService mastershipService = handler.get(MastershipService.class);
        DeviceId ncDeviceId = handler.data().deviceId();
        checkNotNull(controller, "Netconf controller is null");
        String reply = null;

        if (!mastershipService.isLocalMaster(ncDeviceId)) {
            log.warn("Not master for {} Use {} to execute command",
                    ncDeviceId,
                    mastershipService.getMasterFor(ncDeviceId));
            return null;
        }


        DeviceService deviceService = this.handler().get(DeviceService.class);
        while ( !deviceService.getDevice(ncDeviceId).type().toString().equals("OTN") ) {
            log.debug("No termino de conectarse el dispositivo, espero.");
        }

        try {
            StringBuilder request = new StringBuilder("<edit-config xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">");
            request.append("<target>");
            request.append("<running/>");
            request.append("</target>");
            request.append("<default-operation>merge</default-operation>");
            request.append("<test-option>set</test-option>");
            request.append("<config>");
            request.append("<mux-config xmlns=\"http://fulgor.com/ns/cli-mxp\">");
            request.append("<value_rx_power_notify_config xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"replace\">");
            request.append(value_rx_power_notify_config);
            request.append("</value_rx_power_notify_config>");
            request.append("</mux-config>");
            request.append("</config>");
            request.append("</edit-config>");

            reply = controller
                    .getDevicesMap()
                    .get(ncDeviceId)
                    .getSession()
                    .doWrappedRpc(request.toString());
        } catch (NetconfException e) {
            log.error("Cannot communicate to device {} exception {}", ncDeviceId, e);
        }
        return reply;
    }




    @Override
    public String createOrReplaceNeighbor(String local_port, String neighbor, String remote_port) {
        DriverHandler handler = handler();
        NetconfController controller = handler.get(NetconfController.class);
        MastershipService mastershipService = handler.get(MastershipService.class);
        DeviceId ncDeviceId = handler.data().deviceId();
        checkNotNull(controller, "Netconf controller is null");
        String reply = null;

        if (!mastershipService.isLocalMaster(ncDeviceId)) {
            log.warn("Not master for {} Use {} to execute command",
                    ncDeviceId,
                    mastershipService.getMasterFor(ncDeviceId));
            return null;
        }


        DeviceService deviceService = this.handler().get(DeviceService.class);
        while ( !deviceService.getDevice(ncDeviceId).type().toString().equals("OTN") ) {
            log.debug("No termino de conectarse el dispositivo, espero.");
        }

        try {
            StringBuilder request = new StringBuilder("<edit-config xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">");
            request.append("<target>");
            request.append("<running/>");
            request.append("</target>");
            request.append("<default-operation>merge</default-operation>");
            request.append("<test-option>set</test-option>");
            request.append("<config>");
            request.append("<mux-config xmlns=\"http://fulgor.com/ns/cli-mxp\">");
            request.append("<ports xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"replace\">");

            request.append("<port>");
            request.append(local_port);
            request.append("</port>");

            request.append("<neighbor>");
            request.append(neighbor);
            request.append("</neighbor>");

            request.append("<port_neighbor>");
            request.append(remote_port);
            request.append("</port_neighbor>");

            request.append("</ports>");
            request.append("</mux-config>");
            request.append("</config>");
            request.append("</edit-config>");

            reply = controller
                    .getDevicesMap()
                    .get(ncDeviceId)
                    .getSession()
                    .doWrappedRpc(request.toString());
        } catch (NetconfException e) {
            log.error("Cannot communicate to device {} exception {}", ncDeviceId, e);
        }
        return reply;
    }

    @Override
    public String removeNeighbor(String puerto) {
        DriverHandler handler = handler();
        NetconfController controller = handler.get(NetconfController.class);
        MastershipService mastershipService = handler.get(MastershipService.class);
        DeviceId ncDeviceId = handler.data().deviceId();
        checkNotNull(controller, "Netconf controller is null");
        String reply = null;

        if (!mastershipService.isLocalMaster(ncDeviceId)) {
            log.warn("Not master for {} Use {} to execute command",
                    ncDeviceId,
                    mastershipService.getMasterFor(ncDeviceId));
            return null;
        }

        DeviceService deviceService = this.handler().get(DeviceService.class);
        while ( !deviceService.getDevice(ncDeviceId).type().toString().equals("OTN") ) {
            log.debug("No termino de conectarse el dispositivo, espero.");
        }

        try {
            StringBuilder request = new StringBuilder("<edit-config xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">");
            request.append("<target>");
            request.append("<running/>");
            request.append("</target>");
            request.append("<default-operation>merge</default-operation>");
            request.append("<test-option>set</test-option>");
            request.append("<config>");
            request.append("<mux-config xmlns=\"http://fulgor.com/ns/cli-mxp\">");
            request.append("<ports xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"remove\">");

            request.append("<port>");
            request.append(puerto);
            request.append("</port>");

            request.append("</ports>");
            request.append("</mux-config>");
            request.append("</config>");
            request.append("</edit-config>");

            reply = controller
                    .getDevicesMap()
                    .get(ncDeviceId)
                    .getSession()
                    .doWrappedRpc(request.toString());
        } catch (NetconfException e) {
            log.error("Cannot communicate to device {} exception {}", ncDeviceId, e);
        }
        return reply;
    }




    @Override
    public String setDeviceNeighbors(String deviceneighbors) {
        DriverHandler handler = handler();
        NetconfController controller = handler.get(NetconfController.class);
        MastershipService mastershipService = handler.get(MastershipService.class);
        DeviceId ncDeviceId = handler.data().deviceId();
        checkNotNull(controller, "Netconf controller is null");
        String reply = null;

        if (!mastershipService.isLocalMaster(ncDeviceId)) {
            log.warn("Not master for {} Use {} to execute command",
                    ncDeviceId,
                    mastershipService.getMasterFor(ncDeviceId));
            return null;
        }


        DeviceService deviceService = this.handler().get(DeviceService.class);
        while ( !deviceService.getDevice(ncDeviceId).type().toString().equals("OTN") ) {
            log.debug("No termino de conectarse el dispositivo, espero.");
        }

        try {
            StringBuilder request = new StringBuilder("<edit-config xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">");
            request.append("<target>");
            request.append("<running/>");
            request.append("</target>");
            request.append("<default-operation>merge</default-operation>");
            request.append("<test-option>set</test-option>");
            request.append("<config>");
            request.append("<mux-config xmlns=\"http://fulgor.com/ns/cli-mxp\">");
            request.append("<deviceneighbors xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"replace\">");
            request.append(deviceneighbors);
            request.append("</deviceneighbors>");
            request.append("</mux-config>");
            request.append("</config>");
            request.append("</edit-config>");

            reply = controller
                    .getDevicesMap()
                    .get(ncDeviceId)
                    .getSession()
                    .doWrappedRpc(request.toString());
        } catch (NetconfException e) {
            log.error("Cannot communicate to device {} exception {}", ncDeviceId, e);
        }
        return reply;
    }

    @Override
    public String rpcApplyConfig() {
        DriverHandler handler = handler();
        NetconfController controller = handler.get(NetconfController.class);
        MastershipService mastershipService = handler.get(MastershipService.class);
        DeviceId ncDeviceId = handler.data().deviceId();
        checkNotNull(controller, "Netconf controller is null");


        if (!mastershipService.isLocalMaster(ncDeviceId)) {
            log.warn("Not master for {} Use {} to execute command",
                    ncDeviceId,
                    mastershipService.getMasterFor(ncDeviceId));
            return null;
        }

        DeviceService deviceService = this.handler().get(DeviceService.class);
        while ( !deviceService.getDevice(ncDeviceId).type().toString().equals("OTN") ) {
            log.debug("No termino de conectarse el dispositivo, espero.");
        }



        String local_config = null;

        try {
            StringBuilder request = new StringBuilder("<mux-config xmlns=\"http://fulgor.com/ns/cli-mxp\">");
            request.append("<ports/>");
            request.append("</mux-config>");

            local_config = controller
                    .getDevicesMap()
                    .get(ncDeviceId)
                    .getSession()
                    .get(request.toString(), REPORT_ALL);
        } catch (NetconfException e) {
            log.error("Cannot communicate to device {} exception {}", ncDeviceId, e);
        }

        ArrayList<String> lista_vecinos = getVecino(local_config);

        if ( !lista_vecinos.isEmpty() ) {

            log.info("lista no vacia, recorro vecinos");

            ListIterator<String> vecinosIterator = lista_vecinos.listIterator();

            whileIteratorVecinos: while ( vecinosIterator.hasNext() ) {

                String vecino = vecinosIterator.next();

                log.info("el vecino que se evalua es: {}",vecino);
                // Se busca en los dispositivos actualmente conectados si hay alguno con un numero de serie que coincida con el indicado por el dispositivo como vecino.

                com.google.common.base.Optional<Device> dev = Iterables.tryFind(
                        deviceService.getAvailableDevices(),
                        input -> input.serialNumber().equals(vecino));

                if (!dev.isPresent()) {
                    log.info("Device with chassis ID {} does not exist");
                    AlarmService alarmService = this.handler().get(AlarmService.class);
                    Iterator it = alarmService.getActiveAlarms(ncDeviceId).iterator();

                    while (it.hasNext()) {
                        Alarm b = (Alarm) it.next();

                        if ( b.description().contains("[WARNING] mux-notify xmlns; Inconsistent config with neighbor "+ vecino)  ) {
                            alarmService.remove(b.id());
                            log.info("ELIMINO");
                        }
                    }

                    continue whileIteratorVecinos;
                }

                else {


                    while ( !dev.get().type().toString().equals("OTN") ) {
                        log.debug("No termino de conectarse el dispositivo, espero.");
                    }

                    String remote_config = null;

                    log.info("Se encontro el vecino, pregunto por su config");
                    try {
                        StringBuilder request = new StringBuilder("<mux-config xmlns=\"http://fulgor.com/ns/cli-mxp\">");
                        request.append("<tipo_trafico/>");
                        request.append("</mux-config>");

                        remote_config = controller
                                .getDevicesMap()
                                .get(dev.get().id())
                                .getSession()
                                .get(request.toString(), REPORT_ALL);
                    } catch (NetconfException e) {
                        log.error("Cannot communicate to device {} exception {}", ncDeviceId, e);
                    }

                    local_config = null;


                    try {
                        StringBuilder request = new StringBuilder("<mux-config xmlns=\"http://fulgor.com/ns/cli-mxp\">");
                        request.append("<tipo_trafico/>");
                        request.append("</mux-config>");

                        local_config = controller
                                .getDevicesMap()
                                .get(ncDeviceId)
                                .getSession()
                                .get(request.toString(), REPORT_ALL);
                    } catch (NetconfException e) {
                        log.error("Cannot communicate to device {} exception {}", ncDeviceId, e);
                    }


                    String tipo_local = getTipoTrafico(local_config);
                    String tipo_remoto = getTipoTrafico(remote_config);

                    log.info("local,remoto : {} {}",tipo_local,tipo_remoto);

                    if (tipo_local.equals(tipo_remoto)) {

                        log.info("Config iguales, se elimina alarma del dispositivo");

                        AlarmService alarmService = this.handler().get(AlarmService.class);
                        Iterator alarmas_device_local = alarmService.getActiveAlarms(ncDeviceId).iterator();


                        Iterator alarmas_device_vecino = alarmService.getActiveAlarms(dev.get().id()).iterator();




                        while (alarmas_device_local.hasNext()) {
                            Alarm b = (Alarm) alarmas_device_local.next();

                            if ( b.description().contains("[WARNING] mux-notify xmlns; Inconsistent config with neighbor "+ vecino)  ) {
                                alarmService.remove(b.id());
                                log.info("ELIMINO");
                            }
                        }

                        while (alarmas_device_vecino.hasNext()) {
                            Alarm b = (Alarm) alarmas_device_vecino.next();

                            if ( b.description().contains("[WARNING] mux-notify xmlns; Inconsistent config with neighbor "+ deviceService.getDevice(ncDeviceId).serialNumber() )  ) {
                                alarmService.remove(b.id());
                                log.info("ELIMINO");
                            }
                        }

                    }

                    else {

                        log.info("Config distintas, se crea alarma en el dispositivo");


                        try {
                            StringBuilder request = new StringBuilder("<edit-config xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">");
                            request.append("<target>");
                            request.append("<running/>");
                            request.append("</target>");
                            request.append("<default-operation>merge</default-operation>");
                            request.append("<test-option>set</test-option>");
                            request.append("<config>");
                            request.append("<mux-config xmlns=\"http://fulgor.com/ns/cli-mxp\">");
                            request.append("<warning_config xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"replace\">");
                            request.append(vecino);
                            request.append("</warning_config>");
                            request.append("</mux-config>");
                            request.append("</config>");
                            request.append("</edit-config>");

                            controller
                                    .getDevicesMap()
                                    .get(ncDeviceId)
                                    .getSession()
                                    .doWrappedRpc(request.toString());
                        } catch (NetconfException e) {
                            log.error("Cannot communicate to device {} exception {}", ncDeviceId, e);
                        }

                    }
                }
            }

        }
        else {

            AlarmService alarmService = this.handler().get(AlarmService.class);
            Iterator it = alarmService.getActiveAlarms(ncDeviceId).iterator();

            while (it.hasNext()) {
                Alarm b = (Alarm) it.next();

                if ( b.description().contains("[WARNING] mux-notify xmlns; Inconsistent config with neighbor")  ) {
                    alarmService.remove(b.id());
                    log.info("ELIMINO");
                }
            }

        }


        String reply = null;
        /*
        try {
            StringBuilder request = new StringBuilder("<mux-apply-config xmlns=\"http://fulgor.com/ns/cli-mxp\"/>");

            reply = controller
                    .getDevicesMap()
                    .get(ncDeviceId)
                    .getSession()
                    .doWrappedRpc(request.toString());
        } catch (NetconfException e) {
            log.error("Cannot communicate to device {} exception {}", ncDeviceId, e);
        }
        */


        return reply;
    }

    @Override
    public String rpcSettingsConfig() {
        DriverHandler handler = handler();
        NetconfController controller = handler.get(NetconfController.class);
        MastershipService mastershipService = handler.get(MastershipService.class);
        DeviceId ncDeviceId = handler.data().deviceId();
        checkNotNull(controller, "Netconf controller is null");
        String reply = null;

        if (!mastershipService.isLocalMaster(ncDeviceId)) {
            log.warn("Not master for {} Use {} to execute command",
                    ncDeviceId,
                    mastershipService.getMasterFor(ncDeviceId));
            return null;
        }

        DeviceService deviceService = this.handler().get(DeviceService.class);
        while ( !deviceService.getDevice(ncDeviceId).type().toString().equals("OTN") ) {
            log.debug("No termino de conectarse el dispositivo, espero.");
        }


        try {
            StringBuilder request = new StringBuilder("<mux-settings xmlns=\"http://fulgor.com/ns/cli-mxp\"/>");

            reply = controller
                    .getDevicesMap()
                    .get(ncDeviceId)
                    .getSession()
                    .doWrappedRpc(request.toString());
        } catch (NetconfException e) {
            log.error("Cannot communicate to device {} exception {}", ncDeviceId, e);
        }

        return reply;
    }


    /**
     * Retrieving serial number version of device.
     * @return the serial number of the device
     */
    private ArrayList<String> getVecino(String config) {

        ArrayList<String> lista_vecinos = new ArrayList<String>();

        while (config.contains("<neighbor>")) {
            log.info(config);
            String vecino = StringUtils.substringBetween(config, "<neighbor>", "</neighbor>");
            config = config.replaceFirst("(?s)<neighbor>.*?</neighbor>", ""); // Borro el primer vecino encontrado. (?s) significa que se aplica a todas las lineas del string.
            lista_vecinos.add(vecino);
        }

        log.info("lista vecinos es:");
        log.info(lista_vecinos.toString());

        return lista_vecinos;
    }


    /**
     * Retrieving serial number version of device.
     * @param version the return of show version command
     * @return the serial number of the device
     */
    private String getTipoTrafico(String version) {

        log.info(version);

        String tipo_trafico = StringUtils.substringBetween(version, "<tipo_trafico>", "</tipo_trafico>");
        log.info("Tipo trafico es:");
        log.info(tipo_trafico);
        return tipo_trafico;
    }


}
