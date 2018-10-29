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

import org.apache.commons.lang.StringUtils;

import org.onosproject.incubator.net.faultmanagement.alarm.AlarmService;
import org.onosproject.incubator.net.faultmanagement.alarm.Alarm;

public class AlturaLinkDiscovery extends AbstractHandlerBehaviour
        implements LinkDiscovery {

    private final Logger log = getLogger(getClass());

    @Override
    public Set<LinkDescription> getLinks() {


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

        Set<LinkDescription> descs = new HashSet<>();

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

        log.info(reply);

        DeviceService deviceService = this.handler().get(DeviceService.class);
        DeviceId localDeviceId = this.handler().data().deviceId();
        Port localPort = deviceService.getPorts(localDeviceId).get(0);

        String vecino = serialNumber(reply);


        //find destination device by remote serial number id
        com.google.common.base.Optional<Device> dev = Iterables.tryFind(
                deviceService.getAvailableDevices(),
                input -> input.serialNumber().equals(vecino));
        if (!dev.isPresent()) {
            log.info("Device with chassis ID {} does not exist");
            return descs;
        }

        AlarmService alarmService = this.handler().get(AlarmService.class);

        try {
            for ( Alarm a : alarmService.getAlarms(localDeviceId)) {
                if ( (a.id().toString().contains("RXS")) || (a.id().toString().contains("Rx LOCK ERR")) ) {
                    return descs;
                }
            }
        }
        catch (Exception e){
            log.info("ERROR EN LINK DISCOVERY");
        }

        Device remoteDevice = dev.get();

        Port remotePort = deviceService.getPorts(remoteDevice.id()).get(0);

        ConnectPoint local = new ConnectPoint(localDeviceId, localPort.number());
        ConnectPoint remote = new ConnectPoint(remoteDevice.id(), remotePort.number());
        DefaultAnnotations annotations = DefaultAnnotations.builder()
                .set("layer", "IP")
                .build();
        descs.add(new DefaultLinkDescription(
                local, remote, Link.Type.OPTICAL, false, annotations));
        return descs;
    }


    /**
     * Retrieving serial number version of device.
     * @param version the return of show version command
     * @return the serial number of the device
     */
    private static String serialNumber(String version) {
        String serialNumber = StringUtils.substringBetween(version, "<deviceneighbors>", "</deviceneighbors>");
        return serialNumber;
    }

    /**
     * Retrieving potencia de recepcion de lado de linea of device.
     * @param version the return of show version command
     * @return potencia de recepcion de lado de linea of the device
     */
    private static String xfpRxPower(String version) {
        String xfpRxPower = StringUtils.substringBetween(version, "<xfp_rx_power>", "</xfp_rx_power>");
        return xfpRxPower;
    }
}
