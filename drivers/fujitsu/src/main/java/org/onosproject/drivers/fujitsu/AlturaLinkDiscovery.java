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

import java.util.ArrayList;
import java.util.ListIterator;


public class AlturaLinkDiscovery extends AbstractHandlerBehaviour
        implements LinkDiscovery {

    private final Logger log = getLogger(getClass());

    @Override
    public Set<LinkDescription> getLinks() {

        log.info("LinkDiscovery");
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
        Device localdevice = deviceService.getDevice(ncDeviceId);

        /**
         * Tengo que esperar hasta que el dispositivo se conecte con ONOS.
         * Una vez se conecte, ONOS consulta por la descripcion del dispositivo. Si el mismo es OTN, puedo seguir en getLinks.
         * De lo contrario, retorno null.
         */

        if ( (localdevice.swVersion().equals("1.0") || localdevice.swVersion().equals("2.0")) && (localdevice.type().toString().equals("OTN")) ) {
            log.debug("SON IGUALES");
        }
        else{
            log.debug("NO SON IGUALES");
            return null;
        }


        Set<LinkDescription> descs = new HashSet<>();

        /**
         * Se busca el of:1
         */
        com.google.common.base.Optional<Device> dev = Iterables.tryFind(
                deviceService.getAvailableDevices(),
                input -> input.id().toString().equals("of:0000000000000001"));
        if (!dev.isPresent()) {
            log.info("no esta el of:1");
        } else {
            if (this.handler().data().deviceId().toString().equals("netconf:172.16.0.141:830")) {
                try {
                    StringBuilder request = new StringBuilder("<mux-state-XFP1 xmlns=\"http://fulgor.com/ns/cli-mxp\">");
                    request.append("<Presence/>");
                    request.append("</mux-state-XFP1>");

                    reply = controller
                            .getDevicesMap()
                            .get(ncDeviceId)
                            .getSession()
                            .get(request.toString(), REPORT_ALL);
                } catch (NetconfException e) {
                    log.error("Cannot communicate to device {} exception {}", ncDeviceId, e);
                }
                if (presenceOfModule(reply).equals("Yes")) {
                    DeviceId localDeviceId = this.handler().data().deviceId();
                    Port localPort = deviceService.getPorts(localDeviceId).get(1);
                    ConnectPoint local = new ConnectPoint(localDeviceId, localPort.number());

                    Device remoteDevice = dev.get();
                    Port remotePort = deviceService.getPorts(remoteDevice.id()).get(1); // el puerto con el que formo el enlace vecino.
                    ConnectPoint remote = new ConnectPoint(remoteDevice.id(), remotePort.number());
                    DefaultAnnotations annotations = DefaultAnnotations.builder().set("layer", "IP").build();
                    descs.add(new DefaultLinkDescription(
                            local, remote, Link.Type.OPTICAL, false, annotations));
                    descs.add(new DefaultLinkDescription(
                            remote, local, Link.Type.OPTICAL, false, annotations));
                }
            }
        }

        /**
         * Se busca el of:2
         */
        dev = Iterables.tryFind(
                deviceService.getAvailableDevices(),
                input -> input.id().toString().equals("of:0000000000000002"));
        if (!dev.isPresent()) {
            log.info("no esta el of:2");
        } else {
            if (this.handler().data().deviceId().toString().equals("netconf:172.16.0.142:830")) {
                try {
                    StringBuilder request = new StringBuilder("<mux-state-XFP1 xmlns=\"http://fulgor.com/ns/cli-mxp\">");
                    request.append("<Presence/>");
                    request.append("</mux-state-XFP1>");

                    reply = controller
                            .getDevicesMap()
                            .get(ncDeviceId)
                            .getSession()
                            .get(request.toString(), REPORT_ALL);
                } catch (NetconfException e) {
                    log.error("Cannot communicate to device {} exception {}", ncDeviceId, e);
                }
                if (presenceOfModule(reply).equals("Yes")) {
                    DeviceId localDeviceId = this.handler().data().deviceId();
                    Port localPort = deviceService.getPorts(localDeviceId).get(4);
                    ConnectPoint local = new ConnectPoint(localDeviceId, localPort.number());

                    Device remoteDevice = dev.get();
                    Port remotePort = deviceService.getPorts(remoteDevice.id()).get(4); // el puerto con el que formo el enlace vecino.
                    ConnectPoint remote = new ConnectPoint(remoteDevice.id(), remotePort.number());
                    DefaultAnnotations annotations = DefaultAnnotations.builder().set("layer", "IP").build();
                    descs.add(new DefaultLinkDescription(
                            local, remote, Link.Type.OPTICAL, false, annotations));
                    descs.add(new DefaultLinkDescription(
                            remote, local, Link.Type.OPTICAL, false, annotations));
                }
            }
        }



        /**
         * Pregunto al dispositivo local quien es su vecino.
         */


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




        ArrayList<String> vecino = serialNumber(reply);

        ListIterator<String> listIterator = vecino.listIterator();

        log.info("MI VECINO ESSSSSSSSSSSSS");
        int p = 0;
        while (listIterator.hasNext()){
            DeviceId localDeviceId = this.handler().data().deviceId();
        Port localPort = deviceService.getPorts(localDeviceId).get(p);
        String pru = listIterator.next();
        log.info(pru);

        /**
         * Se busca en los dispositivos actualmente conectados si hay alguno con un numero de serie que coincida con el indicado por el dispositivo como vecino.
         */
        dev = Iterables.tryFind(
                deviceService.getAvailableDevices(),
                input -> input.serialNumber().equals(pru));
        if (!dev.isPresent()) {
            log.info("Device with chassis ID {} does not exist");
            return descs;
        }


        /**
         * Tengo que ver si el dispositivo local tiene alarmas referidas al enlace.
         * Si las tiene, no armo enlace.
         */
        AlarmService alarmService = this.handler().get(AlarmService.class);
        try {
            for ( Alarm a : alarmService.getAlarms(localDeviceId)) {
                if ( (a.id().toString().contains("RXS")) || (a.id().toString().contains("Rx LOCK ERR")) ) {
                    return descs;
                }
            }
        }

        catch (Exception e){
            log.info("LinkDiscovery ERROR - alarms");
        }

        Device remoteDevice = dev.get();
        Port remotePort = deviceService.getPorts(remoteDevice.id()).get(p); // el puerto con el que formo el enlace vecino.

        ConnectPoint local = new ConnectPoint(localDeviceId, localPort.number());
        ConnectPoint remote = new ConnectPoint(remoteDevice.id(), remotePort.number());

        DefaultAnnotations annotations = DefaultAnnotations.builder().set("layer", "IP").build();

        descs.add(new DefaultLinkDescription(
                local, remote, Link.Type.OPTICAL, false, annotations));
        p = p +1;
        }

        return descs;
    }


    /**
     * Retrieving serial number version of device.
     * @param version the return of show version command
     * @return the serial number of the device
     */
    private ArrayList<String> serialNumber(String version) {
        log.info(version);
        String prueba = version;
        ArrayList<String> list= new ArrayList<String>();
        while (prueba.contains("deviceneighbors")) {
            String serialNumber = StringUtils.substringBetween(prueba, "<deviceneighbors>", "</deviceneighbors>");
            list.add(serialNumber);
            prueba.replaceFirst("<deviceneighbors>.*?</deviceneighbors>", "");
            log.info(prueba);
        }
        return list;
    }

    /**
     * Retrieving serial number version of device.
     * @param version the return of show version command
     * @return the serial number of the device
     */
    private String presenceOfModule(String version) {
        String presence = StringUtils.substringBetween(version, "<Presence>", "</Presence>");
        return presence;
    }
}
