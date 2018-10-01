package org.onosproject.drivers.fujitsu;
import org.onosproject.net.behaviour.LinkDiscovery;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DefaultAnnotations;
import org.onosproject.net.DefaultAnnotations.Builder;
import org.onosproject.net.link.DefaultLinkDescription;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Link;
import org.onosproject.net.link.LinkDescription;
import org.onosproject.net.Port;
import org.slf4j.Logger;

import static org.onosproject.drivers.fujitsu.FujitsuVoltXmlUtility.REPORT_ALL;
import static org.slf4j.LoggerFactory.getLogger;
import org.onosproject.net.device.DeviceService;
import com.google.common.collect.Iterables;
import org.onosproject.net.driver.DriverHandler;
import org.onosproject.mastership.MastershipService;
import org.onosproject.netconf.NetconfController;
import org.onosproject.netconf.NetconfException;
import static com.google.common.base.Preconditions.checkNotNull;

public class AlturaLinkDiscovery extends AbstractHandlerBehaviour
        implements LinkDiscovery {

    private final Logger log = getLogger(getClass());

    @Override
    public Set<LinkDescription> getLinks() {



        log.info("ENTRO A LinkDiscovery");


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
            StringBuilder request = new StringBuilder("<mux-config xmlns=\"http://fulgor.com/ns/cli-mxp\">");
            request.append("<deviceneighbors/>");
            request.append("</mux-config>");
            request.append("<mux-state xmlns=\"http://fulgor.com/ns/cli-mxp\">");
            request.append("<xfp_rx_power/>");
            request.append("</mux-state>");

            reply = controller
                    .getDevicesMap()
                    .get(ncDeviceId)
                    .getSession()
                    .get(request.toString(), REPORT_ALL);
        } catch (NetconfException e) {
            log.error("Cannot communicate to device {} exception {}", ncDeviceId, e);
        }

        log.info(reply);

        DeviceService deviceService = this.handler().get(DeviceService.class);
        DeviceId localDeviceId = this.handler().data().deviceId();
        Port localPort = deviceService.getPorts(localDeviceId).get(0);

        String uriremote;
        if (localDeviceId.uri().toString().equals("netconf:172.17.0.2:830")) {
            uriremote="netconf:172.17.0.3:830";
        }
        else
            uriremote="netconf:172.17.0.2:830";

        com.google.common.base.Optional<Device> dev = Iterables.tryFind(
                deviceService.getAvailableDevices(),
                input -> input.id().equals(DeviceId.deviceId(uriremote)));

        Device remoteDevice = dev.get();

        Port remotePort = deviceService.getPorts(remoteDevice.id()).get(0);


        Set<LinkDescription> descs = new HashSet<>();


        ConnectPoint local = new ConnectPoint(localDeviceId, localPort.number());
        ConnectPoint remote = new ConnectPoint(remoteDevice.id(), remotePort.number());
        DefaultAnnotations annotations = DefaultAnnotations.builder()
                .set("layer", "IP")
                .build();
        descs.add(new DefaultLinkDescription(
                local, remote, Link.Type.OPTICAL, false, annotations));
        descs.add(new DefaultLinkDescription(
                remote, local, Link.Type.OPTICAL, false, annotations));

        return descs;
    }
}
