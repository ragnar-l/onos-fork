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
import org.onosproject.incubator.net.faultmanagement.alarm.AlarmService;
import org.onosproject.mastership.MastershipService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Port;
import org.onosproject.net.behaviour.MxpConfig;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.net.driver.DriverHandler;
import org.onosproject.net.link.LinkAdminService;
import org.onosproject.netconf.NetconfController;
import org.onosproject.netconf.NetconfException;
import org.slf4j.Logger;
import java.util.ArrayList;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.onosproject.drivers.fujitsu.FujitsuVoltXmlUtility.*;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.concurrent.TimeUnit;

public class AlturaMxpConfig extends AbstractHandlerBehaviour
        implements MxpConfig {


    private final Logger log = getLogger(AlturaMxpConfig.class);

    private static final String CONSULTA_PORTS_LEAF = "<mux-config xmlns=\"http://fulgor.com/ns/cli-mxp\">" +
            "<ports/>" +
            "</mux-config>";

    private static final String CONSULTA_TIPO_TRAFICO_LEAF = "<mux-config xmlns=\"http://fulgor.com/ns/cli-mxp\">" +
            "<tipo_trafico/>" +
            "</mux-config>";

    private static final String SET_LEAF_VALUE_INCIO = "<edit-config xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
            "<target>" +
            "<running/>" +
            "</target>" +
            "<default-operation>merge</default-operation>" +
            "<test-option>set</test-option>" +
            "<config>" +
            "<mux-config xmlns=\"http://fulgor.com/ns/cli-mxp\">";

    private static final String SET_LEAF_VALUE_FINAL = "</mux-config>" +
            "</config>" +
            "</edit-config>";

    private static final String WARNING_CONFIG_ALARM = "[WARNING] mux-notify xmlns; Inconsistent config with neighbor ";

    private static final String MUX_SETTINGS_RPC = "<mux-settings xmlns=\"http://fulgor.com/ns/cli-mxp\"/>";


    private static final String CREATE_REPLACE_NEIGHBOR_START = "<edit-config xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
            "<target>" +
            "<running/>" +
            "</target>" +
            "<default-operation>merge</default-operation>" +
            "<test-option>set</test-option>" +
            "<config>" +
            "<mux-config xmlns=\"http://fulgor.com/ns/cli-mxp\">" +
            "<ports xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"replace\">";

    private static final String CREATE_REPLACE_NEIGHBOR_END = "</ports>" +
            "</mux-config>" +
            "</config>" +
            "</edit-config>";



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
            try {
                TimeUnit.SECONDS.sleep(10);
            }
            catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }


        if ((!tipo_trafico.equals("xge")) && !(tipo_trafico.equals("otu2"))) {
            log.error("Invalid value of arguments. value: {}",tipo_trafico);
            return null;
        }

        try {
            reply = controller
                    .getDevicesMap()
                    .get(ncDeviceId)
                    .getSession()
                    .doWrappedRpc(setLeaf("tipo_trafico",tipo_trafico));
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
            try {
                TimeUnit.SECONDS.sleep(10);
            }
            catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }


        if ((!tipo_fec_linea.equals("gfec")) && !(tipo_fec_linea.equals("cerofec"))) {
            log.error("Invalid value of arguments. value: {} ",tipo_fec_linea );
            return null;
        }

        try {
            reply = controller
                    .getDevicesMap()
                    .get(ncDeviceId)
                    .getSession()
                    .doWrappedRpc(setLeaf("tipo_fec_linea",tipo_fec_linea));
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
            try {
                TimeUnit.SECONDS.sleep(10);
            }
            catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }


        if ( (!tipo_fec_cliente.equals("gfec_cliente")) && !(tipo_fec_cliente.equals("cerofec_cliente")) && !(tipo_fec_cliente.equals("nulofec_cliente")) ) {
            log.error("Invalid value of arguments.");
            return null;
        }

        try {
            reply = controller
                    .getDevicesMap()
                    .get(ncDeviceId)
                    .getSession()
                    .doWrappedRpc(setLeaf("tipo_fec_cliente",tipo_fec_cliente));
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
            try {
                TimeUnit.SECONDS.sleep(10);
            }
            catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }


        try {
            reply = controller
                    .getDevicesMap()
                    .get(ncDeviceId)
                    .getSession()
                    .doWrappedRpc(setLeaf("edfa_output_power",edfa_output_power));
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
            try {
                TimeUnit.SECONDS.sleep(10);
            }
            catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }


        try {
            reply = controller
                    .getDevicesMap()
                    .get(ncDeviceId)
                    .getSession()
                    .doWrappedRpc(setLeaf("time_notify_config",time_notify_config));
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
            try {
                TimeUnit.SECONDS.sleep(10);
            }
            catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        try {
            reply = controller
                    .getDevicesMap()
                    .get(ncDeviceId)
                    .getSession()
                    .doWrappedRpc(setLeaf("value_notify_config",value_notify_config));
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
            try {
                TimeUnit.SECONDS.sleep(10);
            }
            catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        try {
            reply = controller
                    .getDevicesMap()
                    .get(ncDeviceId)
                    .getSession()
                    .doWrappedRpc(setLeaf("value_rx_power_notify_config",value_rx_power_notify_config));
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
            try {
                TimeUnit.SECONDS.sleep(10);
            }
            catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        checkLink(local_port);

        try {

            reply = controller
                    .getDevicesMap()
                    .get(ncDeviceId)
                    .getSession()
                    .doWrappedRpc(CREATE_REPLACE_NEIGHBOR_START +
                            "<port>" +
                            local_port +
                            "</port>" +
                            "<neighbor>" +
                            neighbor +
                            "</neighbor>" +
                            "<port_neighbor>" +
                            remote_port +
                            "</port_neighbor>" +
                            CREATE_REPLACE_NEIGHBOR_END

                    );
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
            try {
                TimeUnit.SECONDS.sleep(10);
            }
            catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        checkLink(puerto);

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
            try {
                TimeUnit.SECONDS.sleep(10);
            }
            catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        try {
            reply = controller
                    .getDevicesMap()
                    .get(ncDeviceId)
                    .getSession()
                    .doWrappedRpc(setLeaf("deviceneighbors",deviceneighbors));
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
            log.info("No termino de conectarse el dispositivo, espero.");
            try {
                TimeUnit.SECONDS.sleep(10);
            }
            catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }


        /**
         * Realizo la consulta de los vecinos que tiene el dispositivo local.
         */
        String ports_config = "";
        try {
            ports_config = controller
                    .getDevicesMap()
                    .get(ncDeviceId)
                    .getSession()
                    .get(CONSULTA_PORTS_LEAF, REPORT_ALL);
        } catch (NetconfException e) {
            log.error("Cannot communicate to device {} exception {}", ncDeviceId, e);
        }

        /**
         * Se llama al metodo getVecino para obtener los diferentes numeros de serie de los vecinos que tiene el dispositivo local.
         */
        ArrayList<String> lista_vecinos = getVecino(ports_config);

        AlarmService alarmService = this.handler().get(AlarmService.class);
        ArrayList<Alarm> alarmas_local = new ArrayList<>();
        alarmas_local.addAll(alarmService.getActiveAlarms(ncDeviceId));

        if ( !lista_vecinos.isEmpty() ) {

            // La lista de vecinos no esta vacia, debo recorrer la lista, preguntar si los vecinos realmente estan conectados en ONOS y comparar su configuracion.

            String local_config = "";
            try {
                local_config = controller
                        .getDevicesMap()
                        .get(ncDeviceId)
                        .getSession()
                        .get(CONSULTA_TIPO_TRAFICO_LEAF, REPORT_ALL);
            } catch (NetconfException e) {
                log.error("Cannot communicate to device {} exception {}", ncDeviceId, e);
            }


            int len_vecinos = lista_vecinos.size();


            forVecinos: for (int i_vecinos = 0; i_vecinos < len_vecinos; i_vecinos++) {

                String vecino = lista_vecinos.get(i_vecinos);

                /**
                 * Se busca en los dispositivos actualmente conectados si hay alguno con un numero de serie que coincida con el indicado por el dispositivo como vecino.
                 */
                com.google.common.base.Optional<Device> dev = Iterables.tryFind(
                        deviceService.getAvailableDevices(),
                        input -> input.serialNumber().equals(vecino));


                if (!dev.isPresent()) {
                    // Si el dispositivo vecino no esta presente, recorro las alarmas en el dispositivo local
                    // De existir alguna con la descripcion correspondiente al warning, elimino esa alarma.

                    log.info("[rpcApplyConfig] Dispositivo vecino no presente, se borran alarmas");

                    int len_alarmas = alarmas_local.size();
                    for (int i_alarmas = 0; i_alarmas < len_alarmas; i_alarmas++) {
                        Alarm alarma_local = alarmas_local.get(i_alarmas);
                        if ( alarma_local.description().contains(WARNING_CONFIG_ALARM + vecino)  ) {
                            alarmService.remove(alarma_local.id());
                        }
                    }

                    continue forVecinos;
                }

                else {

                    // Si el dispositivo vecino esta presente, comparo su configuracion.

                    while ( !dev.get().type().toString().equals("OTN") ) {
                        log.debug("No termino de conectarse el dispositivo, espero.");
                    }

                    String remote_config = "";

                    try {
                        remote_config = controller
                                .getDevicesMap()
                                .get(dev.get().id())
                                .getSession()
                                .get(CONSULTA_TIPO_TRAFICO_LEAF, REPORT_ALL);
                    } catch (NetconfException e) {
                        log.error("Cannot communicate to device {} exception {}", ncDeviceId, e);
                    }

                    String tipo_trafico_local = getTipoTrafico(local_config);
                    String tipo_trafico_remoto = getTipoTrafico(remote_config);

                    if (tipo_trafico_local.equals(tipo_trafico_remoto)) {

                        //Si la configuracion es la misma, borro alarmas.

                        log.info("[rpcApplyConfig] Misma configuracion entre dispositivos, se borran alarmas");
                        int len_alarmas = alarmas_local.size();
                        for (int i_alarmas = 0; i_alarmas < len_alarmas; i_alarmas++) {
                            Alarm alarma_local = alarmas_local.get(i_alarmas);
                            if ( alarma_local.description().contains(WARNING_CONFIG_ALARM + vecino)  ) {
                                alarmService.remove(alarma_local.id());
                            }
                        }

                        ArrayList<Alarm> alarmas_vecino = new ArrayList<>();
                        alarmas_vecino.addAll(alarmService.getActiveAlarms(dev.get().id()));


                        len_alarmas = alarmas_vecino.size();
                        for (int i_alarmas = 0; i_alarmas < len_alarmas; i_alarmas++) {
                            Alarm alarma_vecino = alarmas_vecino.get(i_alarmas);
                            if ( alarma_vecino.description().contains(WARNING_CONFIG_ALARM + deviceService.getDevice(ncDeviceId).serialNumber())  ) {
                                alarmService.remove(alarma_vecino.id());
                            }
                        }

                    }

                    else {

                        log.info("[rpcApplyConfig] Distinta configuracion entre dispositivos, se crea alarma");

                        try {
                            controller
                                    .getDevicesMap()
                                    .get(ncDeviceId)
                                    .getSession()
                                    .doWrappedRpc( setLeaf("warning_config",vecino) );
                        } catch (NetconfException e) {
                            log.error("Cannot communicate to device {} exception {}", ncDeviceId, e);
                        }

                    }
                }
            }

        }

        else {
            // no tiene vecinos, por lo tanto elimino todas las alarmas WARNING en ONOS.
            log.info("[rpcApplyConfig] Sin vecinos, se borran alarmas");
            int len_alarmas = alarmas_local.size();
            for (int i_alarmas = 0; i_alarmas < len_alarmas; i_alarmas++) {
                Alarm alarma_local = alarmas_local.get(i_alarmas);
                if ( alarma_local.description().contains(WARNING_CONFIG_ALARM)  ) {
                    alarmService.remove(alarma_local.id());
                }
            }
        }


        String reply = null;

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
            try {
                TimeUnit.SECONDS.sleep(10);
            }
            catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }


        try {
            reply = controller
                    .getDevicesMap()
                    .get(ncDeviceId)
                    .getSession()
                    .doWrappedRpc(MUX_SETTINGS_RPC);
        } catch (NetconfException e) {
            log.error("Cannot communicate to device {} exception {}", ncDeviceId, e);
        }

        return reply;
    }


    /**
     * Devuelve una lista de vecinos.
     * @param config es la respuesta a la consulta por la hoja "ports".
     * @return lista de string con los numeros de serie de los vecinos.
     */
    private ArrayList<String> getVecino(String config) {

        ArrayList<String> lista_vecinos = new ArrayList<String>();

        if (config.equals("") || config=="") {
            return lista_vecinos;
        }


        while (config.contains("<neighbor>")) {
            String vecino = StringUtils.substringBetween(config, "<neighbor>", "</neighbor>");
            config = config.replaceFirst("(?s)<neighbor>.*?</neighbor>", ""); // Borro el primer vecino encontrado. (?s) significa que se aplica a todas las lineas del string.
            lista_vecinos.add(vecino);
        }

        return lista_vecinos;
    }


    /**
     * Devuelve el tipo de trafico.
     * @param config respuesta a la consulta por la hoja "tipo_trafico".
     * @return string con el tipo de trafico.
     */
    private String getTipoTrafico(String config) {
        String tipo_trafico = "";
        if (config.contains("<tipo_trafico>")) {
            tipo_trafico = StringUtils.substringBetween(config, "<tipo_trafico>", "</tipo_trafico>");
        }
        return tipo_trafico;
    }


    /**
     * Devuelve el tipo de trafico.
     * @param leaf leaf a editar.
     * @param value valor a setear en el leaf.
     * @return string con el tipo de trafico.
     */
    private String setLeaf(String leaf, String value) {

        String request = SET_LEAF_VALUE_INCIO +
                "<" + leaf + " xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"replace\">" +
                value + "</"+leaf+">" +
                SET_LEAF_VALUE_FINAL;

        return request;
    }


    /**
     * Retrieving ports topology of device.
     * @param parse la respuesta del dispositivo a la consulta por sus puertos.
     * @return una lista de puertos.
     */
    private ArrayList<AlturaMxpPuertos> getPuertos(String parse) {

        ArrayList<AlturaMxpPuertos> lista_puertos = new ArrayList<AlturaMxpPuertos>();


        while (parse.contains("ports")) {
            AlturaMxpPuertos p = new AlturaMxpPuertos();

            String info = StringUtils.substringBetween(parse, "<ports>", "</ports>"); // a esto tengo que sacar la info sobre puerto, vecino y puerto vecino

            String info_nombre_puerto = StringUtils.substringBetween(info, "<port>", "</port>");
            p.setPuerto(Integer.valueOf(info_nombre_puerto));

            String info_nombre_vecino = StringUtils.substringBetween(info, "<neighbor>", "</neighbor>");
            p.setVecino(info_nombre_vecino);

            String info_nombre_puerto_vecino = StringUtils.substringBetween(info, "<port_neighbor>", "</port_neighbor>");
            p.setPuertoVecino(Integer.valueOf(info_nombre_puerto_vecino));

            parse = parse.replaceFirst("(?s)<ports>.*?</ports>", ""); // (?s) significa que se aplica a todas las lineas del string.

            lista_puertos.add(p);
        }

        return lista_puertos;
    }




    /**
     * Borra los links formados (si los hubiera) entre el device local y el vecino.
     * @param puerto_afectado el puerto local que se borrara o modificara.
     */
    private void checkLink(String puerto_afectado) {

        //pruebo que no sea puerto transmisor - receptor
        if (Integer.parseInt(puerto_afectado) > 1) {
            DriverHandler handler = handler();
            NetconfController controller = handler.get(NetconfController.class);
            MastershipService mastershipService = handler.get(MastershipService.class);
            DeviceId ncDeviceId = handler.data().deviceId();
            checkNotNull(controller, "Netconf controller is null");
            DeviceService deviceService = this.handler().get(DeviceService.class);


            String reply = new String();
            try {
                StringBuilder consulta_vecinos = new StringBuilder("<mux-config xmlns=\"http://fulgor.com/ns/cli-mxp\">");
                consulta_vecinos.append("<ports/>");
                consulta_vecinos.append("</mux-config>");

                reply = controller
                        .getDevicesMap()
                        .get(ncDeviceId)
                        .getSession()
                        .get(consulta_vecinos.toString(), REPORT_ALL);
            } catch (NetconfException e) {
                log.error("Cannot communicate to device {} exception {}", ncDeviceId, e);
            }

            ArrayList<AlturaMxpPuertos> lista_puertos = getPuertos(reply);

            int len_vecinos = lista_puertos.size();
            for (int i_vecinos = 0; i_vecinos < len_vecinos; i_vecinos++) {

                if ( lista_puertos.get(i_vecinos).getPuerto() == Integer.parseInt(puerto_afectado) ) {

                    if (Integer.parseInt(lista_puertos.get(i_vecinos).getVecino())<10) {
                        int a = Integer.parseInt(lista_puertos.get(i_vecinos).getVecino());

                        com.google.common.base.Optional<Device> dev = Iterables.tryFind(
                                deviceService.getAvailableDevices(),
                                input -> input.id().toString().equals("of:000000000000000"+Integer.toString( a )));

                        if ( !dev.isPresent() ) {
                            log.info("no esta el of");
                            return;
                        }

                        else {
                            LinkAdminService linkService  = handler.get(LinkAdminService.class);
                            Port localPort = deviceService.getPorts(ncDeviceId).get(Integer.parseInt(puerto_afectado));
                            ConnectPoint local = new ConnectPoint(ncDeviceId, localPort.number());


                            Device remoteDevice = dev.get();
                            Port remotePort = deviceService.getPorts(remoteDevice.id()).get(1);
                            ConnectPoint remote = new ConnectPoint(remoteDevice.id(), remotePort.number());

                            if (linkService.getLink(local,remote)!=null) {
                                log.info("borro link");
                                //linkService.removeLink(local,remote);
                                linkService.removeLink(remote,local);
                            }
                        }

                    }
                }
            }
        }

    }



}