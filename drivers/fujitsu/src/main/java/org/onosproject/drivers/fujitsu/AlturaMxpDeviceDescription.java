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
import com.google.common.collect.Lists;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.onosproject.drivers.utilities.XmlConfigParser;
import org.onosproject.net.AnnotationKeys;
import org.onosproject.net.ChannelSpacing;
import org.onosproject.net.CltSignalType;
import org.onosproject.net.DefaultAnnotations;
import org.onosproject.net.GridType;
import org.onosproject.net.OchSignal;
import org.onosproject.net.OduSignalType;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DeviceDescription;
import org.onosproject.net.device.DeviceDescriptionDiscovery;
import org.onosproject.net.device.PortDescription;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.netconf.NetconfController;
import org.onosproject.netconf.NetconfException;
import org.onosproject.netconf.NetconfSession;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.onosproject.net.optical.device.OchPortHelper.ochPortDescription;
import static org.onosproject.net.optical.device.OduCltPortHelper.oduCltPortDescription;
import static org.slf4j.LoggerFactory.getLogger;



import org.onosproject.net.DefaultAnnotations;
import java.util.ArrayList;
import java.util.List;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DefaultPortDescription;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DefaultDeviceDescription;
import org.onosproject.net.device.DeviceService;


/**
 * Retrieves the ports (sin informacion por ahora - que puertos?) from a Altura MXP40gb device via netconf.
 */
public class AlturaMxpDeviceDescription extends AbstractHandlerBehaviour
        implements DeviceDescriptionDiscovery {

    private final Logger log = getLogger(getClass());

    @Override
    public DeviceDescription discoverDeviceDetails() {

        NetconfController controller = checkNotNull(handler().get(NetconfController.class));
        NetconfSession session = controller.getDevicesMap().get(handler().data().deviceId()).getSession();
        StringBuilder request = new StringBuilder("<mux-state xmlns=\"http://fulgor.com/ns/cli-mxp\">");
        request.append("<device_manufacturer/>");
        request.append("<device_swVersion/>");
        request.append("<device_hwVersion/>");
        request.append("<device_boardId/>");
        request.append("</mux-state>");
        String version = null;
        try {
            version = session.get(request.toString(), REPORT_ALL);
        } catch (NetconfException e) {
            throw new IllegalStateException(new NetconfException("Failed to retrieve version info.", e));
        }

        //String[] details = TextBlockParserCisco.parseCiscoIosDeviceDetails(version);


        DeviceService deviceService = checkNotNull(handler().get(DeviceService.class));
        DeviceId deviceId = handler().data().deviceId();
        Device device = deviceService.getDevice(deviceId);

        return new DefaultDeviceDescription(device.id().uri(), Device.Type.OTN,
                "ALTURA", "1.0",
                null, null,
                null);
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

        return ports;
    }

}
