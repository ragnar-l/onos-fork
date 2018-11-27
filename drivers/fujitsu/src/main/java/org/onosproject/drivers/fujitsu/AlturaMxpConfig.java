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
import static org.onosproject.drivers.fujitsu.FujitsuVoltXmlUtility.*;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Implementation to get and set parameters available in vOLT
 * through the Netconf protocol.
 */
public class AlturaMxpConfig extends AbstractHandlerBehaviour
        implements MxpConfig {

    private final Logger log = getLogger(AlturaMxpConfig.class);
    private static final String ADMIN_STATE = "admin-state";
    private static final String PASSWORD = "password";
    private static final Set<String> ONUCONFIGPARAMS =
            ImmutableSet.of(ADMIN_STATE, "pm-enable", "fec-enable", "security-enable", PASSWORD);
    private static final Set<String> ADMINSTATES =
            ImmutableSet.of("enable", "disable");
    private static final Set<String> ENABLES =
            ImmutableSet.of("true", "false");
    private static final String VOLT_ONUS = "volt-onus";
    private static final String ONUS_PERLINK = "onus-perlink";
    private static final String ONUS_LIST = "onus-list";
    private static final String ONU_INFO = "onu-info";
    private static final String ONU_SET_CONFIG = "onu-set-config";
    private static final String CONFIG_INFO = "config-info";
    private static final String VOLT_STATISTICS = "volt-statistics";
    private static final String ONU_STATISTICS = "onu-statistics";
    private static final String ONU_ETH_STATS = "onu-eth-stats";
    private static final String ETH_STATS = "eth-stats";
    private static final String ONU_GEM_STATS = "onu-gem-stats";
    private static final String GEM_STATS = "gem-stats";
    private static final String PASSWORD_PATTERN = "^[a-zA-Z0-9]+$";

    @Override
    public String getOnus(String target) {
        DriverHandler handler = handler();
        NetconfController controller = handler.get(NetconfController.class);
        MastershipService mastershipService = handler.get(MastershipService.class);
        DeviceId ncDeviceId = handler.data().deviceId();
        checkNotNull(controller, "Netconf controller is null");
        String reply = null;
        String[] onuId = null;

        if (!mastershipService.isLocalMaster(ncDeviceId)) {
            log.warn("Not master for {} Use {} to execute command",
                     ncDeviceId,
                     mastershipService.getMasterFor(ncDeviceId));
            return null;
        }

        if (target != null) {
            onuId = checkIdString(target);
            if (onuId == null) {
                log.error("Invalid ONU identifier {}", target);
                return null;
            }
        }

        try {
            StringBuilder request = new StringBuilder();
            request.append(VOLT_NE_OPEN + VOLT_NE_NAMESPACE);
            request.append(ANGLE_RIGHT + NEW_LINE);
            if (onuId != null) {
                request.append(buildStartTag(VOLT_ONUS))
                    .append(buildStartTag(ONUS_PERLINK))
                    .append(buildStartTag(PONLINK_ID, false))
                    .append(onuId[FIRST_PART])
                    .append(buildEndTag(PONLINK_ID));
                if (onuId.length > ONE) {
                    request.append(buildStartTag(ONUS_LIST))
                        .append(buildStartTag(ONU_INFO))
                        .append(buildStartTag(ONU_ID, false))
                        .append(onuId[SECOND_PART])
                        .append(buildEndTag(ONU_ID))
                        .append(buildEndTag(ONU_INFO))
                        .append(buildEndTag(ONUS_LIST));
                }
                request.append(buildEndTag(ONUS_PERLINK))
                    .append(buildEndTag(VOLT_ONUS));
            } else {
                request.append(buildEmptyTag(VOLT_ONUS));
            }
            request.append(VOLT_NE_CLOSE);

            reply = controller
                        .getDevicesMap()
                        .get(ncDeviceId)
                        .getSession()
                        .get(request.toString(), REPORT_ALL);
        } catch (NetconfException e) {
            log.error("Cannot communicate to device {} exception {}", ncDeviceId, e);
        }
        return reply;
    }

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
     * Verifies input string for ponlink-id{-onu-id}.
     *
     * @param target input data in string
     * @return String array containing IDs; may be null if an error is detected
     */
    private String[] checkIdString(String target) {
        String[] onuId = target.split(HYPHEN);
        int pon;
        int onu;

        if (onuId.length > TWO) {
            log.error("Invalid number of arguments for id:{}", onuId.length);
            return null;
        }
        try {
            pon = Integer.parseInt(onuId[FIRST_PART]);
            if (pon <= ZERO) {
                log.error("Invalid integer for ponlink-id:{}", onuId[FIRST_PART]);
                return null;
            }
            if (onuId.length > ONE) {
                onu = Integer.parseInt(onuId[SECOND_PART]);
                if (onu <= ZERO) {
                    log.error("Invalid integer for onu-id:{}", onuId[SECOND_PART]);
                    return null;
                }
            }
        } catch (NumberFormatException e) {
            log.error("Non-number input for id:{}", target);
            return null;
        }
        return onuId;
    }

    /**
     * Verifies input string for valid options.
     *
     * @param name input data in string
     * @param value input data in string
     * @return true/false if the parameter is valid/invalid
     */
    private boolean checkSetParam(String name, String value) {
        if (!ONUCONFIGPARAMS.contains(name)) {
            log.error("Unsupported parameter: {}", name);
            return false;
        }

        switch (name) {
            case ADMIN_STATE:
                if (!validState(ADMINSTATES, name, value)) {
                    return false;
                }
                break;
            case PASSWORD:
                if (!value.matches(PASSWORD_PATTERN)) {
                    log.error("Invalid value for Name {} : Value {}.", name, value);
                    return false;
                }
                break;
            default:
                if (!validState(ENABLES, name, value)) {
                    return false;
                }
                break;
        }
        return true;
    }

    /**
     * Verifies input string for valid options.
     *
     * @param states input data in string for parameter state
     * @param name input data in string for parameter name
     * @param value input data in string for parameter value
     * @return true/false if the parameter is valid/invalid
     */
    private boolean validState(Set<String> states, String name, String value) {
        if (!states.contains(value)) {
            log.error("Invalid value for Name {} : Value {}.", name, value);
            return false;
        }
        return true;
    }

}
