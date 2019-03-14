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

import org.onosproject.net.behaviour.MxpGetAll;
import org.onosproject.mastership.MastershipService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.net.driver.DriverHandler;
import org.onosproject.netconf.NetconfController;
import org.onosproject.netconf.NetconfException;
import org.slf4j.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.onosproject.drivers.fujitsu.FujitsuVoltXmlUtility.*;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Implementation to get all available data in vOLT
 * through the Netconf protocol.
 */
public class AlturaMxpGetAll extends AbstractHandlerBehaviour
        implements MxpGetAll {

    private final Logger log = getLogger(AlturaMxpGetAll.class);


    @Override
    public String getAll() {
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
            StringBuilder request = new StringBuilder("<mux-config xmlns=\"http://fulgor.com/ns/cli-mxp\"/>");
            request.append("<mux-state xmlns=\"http://fulgor.com/ns/cli-mxp\"/>");
            request.append("<mux-state-TX-RX-alarms xmlns=\"http://fulgor.com/ns/cli-mxp\"/>");
            request.append("<mux-state-misc xmlns=\"http://fulgor.com/ns/cli-mxp\"/>");
            request.append("<mux-state-power xmlns=\"http://fulgor.com/ns/cli-mxp\"/>");
            request.append("<mux-state-dsp xmlns=\"http://fulgor.com/ns/cli-mxp\"/>");
            request.append("<mux-state-edfa xmlns=\"http://fulgor.com/ns/cli-mxp\"/>");
            request.append("<mux-state-temp-hum xmlns=\"http://fulgor.com/ns/cli-mxp\"/>");
            request.append("<mux-state-XFP1 xmlns=\"http://fulgor.com/ns/cli-mxp\"/>");
            request.append("<mux-state-XFP2 xmlns=\"http://fulgor.com/ns/cli-mxp\"/>");
            request.append("<mux-state-XFP3 xmlns=\"http://fulgor.com/ns/cli-mxp\"/>");
            request.append("<mux-state-XFP4 xmlns=\"http://fulgor.com/ns/cli-mxp\"/>");


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
    public String getConfigContainer() {
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
            StringBuilder request = new StringBuilder("<mux-config xmlns=\"http://fulgor.com/ns/cli-mxp\"/>");
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
    public String getStateContainer() {
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
            StringBuilder request = new StringBuilder("<mux-state xmlns=\"http://fulgor.com/ns/cli-mxp\"/>");
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
    public String getStateMiscContainer() {
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
            StringBuilder request = new StringBuilder("<mux-state-misc xmlns=\"http://fulgor.com/ns/cli-mxp\"/>");
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
    public String getStateTxRxAlarmContainer() {
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
            StringBuilder request = new StringBuilder("<mux-state-TX-RX-alarms xmlns=\"http://fulgor.com/ns/cli-mxp\"/>");
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
    public String getStatePowerContainer() {
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
            StringBuilder request = new StringBuilder("<mux-state-power xmlns=\"http://fulgor.com/ns/cli-mxp\"/>");
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
    public String getDspContainer() {
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
            StringBuilder request = new StringBuilder("<mux-state-dsp xmlns=\"http://fulgor.com/ns/cli-mxp\"/>");
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
    public String getEdfaContainer() {
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
            StringBuilder request = new StringBuilder("<mux-state-edfa xmlns=\"http://fulgor.com/ns/cli-mxp\"/>");
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
    public String getTempHumContainer() {
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
            StringBuilder request = new StringBuilder("<mux-state-temp-hum xmlns=\"http://fulgor.com/ns/cli-mxp\"/>");
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
    public String getXFPOneContainer() {
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
            StringBuilder request = new StringBuilder("<mux-state-XFP1 xmlns=\"http://fulgor.com/ns/cli-mxp\"/>");
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
    public String getXFPTwoContainer() {
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
            StringBuilder request = new StringBuilder("<mux-state-XFP2 xmlns=\"http://fulgor.com/ns/cli-mxp\"/>");
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
    public String getXFPThreeContainer() {
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
            StringBuilder request = new StringBuilder("<mux-state-XFP3 xmlns=\"http://fulgor.com/ns/cli-mxp\"/>");
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
    public String getXFPFourContainer() {
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
            StringBuilder request = new StringBuilder("<mux-state-XFP4 xmlns=\"http://fulgor.com/ns/cli-mxp\"/>");
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

}
