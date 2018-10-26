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

import com.google.common.collect.ImmutableSet;
import org.onosproject.mastership.MastershipService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.behaviour.MxpConfig;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.net.driver.DriverHandler;
import org.onosproject.netconf.NetconfController;
import org.onosproject.netconf.NetconfException;
import org.slf4j.Logger;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
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
    public String rpcApplyConfig() {
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





}
