package org.onosproject.drivers.fujitsu;

import org.onosproject.net.behaviour.LinkDiscovery;
import java.util.HashSet;
import java.util.Set;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DefaultAnnotations;
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
import java.util.concurrent.TimeUnit;
import org.onosproject.net.link.LinkAdminService;

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

        Set<LinkDescription> descs = new HashSet<>();

        if ( !localdevice.type().toString().equals("OTN") ) {
            log.info("NO SON IGUALES");
            try {
                TimeUnit.SECONDS.sleep(5);
            }
            catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            return null;
        }

        log.info("LinkDiscovery -- Descubriendo links para dispositivo {}", ncDeviceId.toString());

        if ( localdevice.swVersion().equals("1.0") || localdevice.swVersion().equals("2.0")  ) {

            /**
             * Pregunto al dispositivo local por sus puertos.
             */

            try {
                StringBuilder request = new StringBuilder("<mux-config xmlns=\"http://fulgor.com/ns/cli-mxp\">");
                request.append("<ports/>");
                request.append("</mux-config>");

                reply = controller
                        .getDevicesMap()
                        .get(ncDeviceId)
                        .getSession()
                        .get(request.toString(), REPORT_ALL);
            } catch (NetconfException e) {
                log.error("Cannot communicate to device {} exception {}", ncDeviceId, e);
            }



            ArrayList<AlturaMxpPuertos> lista_puertos = getPuertos(reply);
            DeviceId localDeviceId = this.handler().data().deviceId();

            if ( !lista_puertos.isEmpty() ) {

                int len = lista_puertos.size();

                aLoopName: for (int i = 0; i < len; i++) {

                    AlturaMxpPuertos p = lista_puertos.get(i);


                    if ( p.getPuerto() == 0 ) {

                        if (p.getPuertoVecino() != 1) {
                            log.info("Se esperaba puerto de Receptor");
                            continue aLoopName;
                        }
                    }

                    else if ( p.getPuerto() == 1 ) {

                        if (p.getPuertoVecino() != 0) {
                            log.info("Se esperaba puerto de Transmisor");
                            continue aLoopName;
                        }
                    }

                    else if (p.getPuerto() > 1) {

                        com.google.common.base.Optional<Device> dev = Iterables.tryFind(
                                deviceService.getAvailableDevices(),
                                input -> input.id().toString().equals(p.getVecino()));

                        if (!dev.isPresent()) {
                            log.info("no esta el of:"+p.getVecino());
                            continue aLoopName;
                        }

                        else {

                            try {
                                String XFP = "XFP"+Integer.toString(p.getPuerto()-1);
                                StringBuilder request = new StringBuilder("<mux-state-"+XFP);
                                request.append(" xmlns=\"http://fulgor.com/ns/cli-mxp\">");
                                request.append("<Presence/>");
                                request.append("</mux-state-"+XFP+">");


                                reply = controller
                                        .getDevicesMap()
                                        .get(ncDeviceId)
                                        .getSession()
                                        .get(request.toString(), REPORT_ALL);
                            } catch (NetconfException e) {
                                log.error("Cannot communicate to device {} exception {}", ncDeviceId, e);
                            }

                            if ( presenceOfModule(reply).equals("Yes") ) {


                                Port localPort = deviceService.getPorts(localDeviceId).get(p.getPuerto());
                                ConnectPoint local = new ConnectPoint(localDeviceId, localPort.number());

                                Device remoteDevice = dev.get();
                                Port remotePort = deviceService.getPorts(remoteDevice.id()).get(1);
                                ConnectPoint remote = new ConnectPoint(remoteDevice.id(), remotePort.number());

                                DefaultAnnotations annotations = DefaultAnnotations.builder().set("layer", "IP").build();
                                descs.add(new DefaultLinkDescription(
                                        local, remote, Link.Type.OPTICAL, false, annotations));
                                descs.add(new DefaultLinkDescription(
                                        remote, local, Link.Type.OPTICAL, false, annotations));
                            }

                            else {

                                LinkAdminService linkService  = handler.get(LinkAdminService.class);

                                //no armo nada

                                Port localPort = deviceService.getPorts(localDeviceId).get(p.getPuerto());
                                ConnectPoint local = new ConnectPoint(localDeviceId, localPort.number());

                                Device remoteDevice = dev.get();
                                Port remotePort = deviceService.getPorts(remoteDevice.id()).get(1);
                                ConnectPoint remote = new ConnectPoint(remoteDevice.id(), remotePort.number());


                                log.info("borro link");
                                //linkService.removeLink(local,remote);
                                linkService.removeLink(remote,local);


                                /**
                                 * DefaultAnnotations annotations = DefaultAnnotations.builder().set("layer", "IP").build();
                                descs.add(new DefaultLinkDescription(
                                        remote, local, Link.Type.OPTICAL, false, annotations));
                                 **/
                            }
                        }
                        continue aLoopName;
                    }

                    if ( (p.getPuerto()==0) || (p.getPuerto()==1) ) {

                        /**
                         * Se busca en los dispositivos actualmente conectados si hay alguno con un numero de serie que coincida con el indicado por el dispositivo como vecino.
                         */
                        com.google.common.base.Optional<Device> dev = Iterables.tryFind(
                                deviceService.getAvailableDevices(),
                                input -> input.serialNumber().equals(  p.getVecino() ) );

                        if (!dev.isPresent()) {
                            log.info( "Device with chassis ID {} does not exist", p.getVecino() );
                            continue aLoopName;
                        }

                        /**
                         * Tengo que ver si el dispositivo local tiene alarmas referidas al enlace.
                         * Si las tiene, no armo enlace.
                         */
                        AlarmService alarmService = this.handler().get(AlarmService.class);
                        try {
                            for ( Alarm a : alarmService.getAlarms(localDeviceId)) {
                                if ( (a.id().toString().contains("RXS")) || (a.id().toString().contains("Rx LOCK ERR")) ) {
                                    log.info("-- No se pudo formar el enlace, el dispositivo local {} contiene alarmas --", localDeviceId.toString());
                                    continue aLoopName;
                                }
                            }
                        } catch (Exception e){
                            log.info("LinkDiscovery ERROR - alarms");
                        }

                        Device remoteDevice = dev.get();
                        Port localPort = deviceService.getPorts(localDeviceId).get(p.getPuerto()); // el puerto del local con el que formo el enlace.
                        Port remotePort = deviceService.getPorts(remoteDevice.id()).get(p.getPuertoVecino()); // el puerto del vecino con el que formo el enlace.

                        ConnectPoint local = new ConnectPoint(localDeviceId, localPort.number());
                        ConnectPoint remote = new ConnectPoint(remoteDevice.id(), remotePort.number());

                        DefaultAnnotations annotations = DefaultAnnotations.builder().set("layer", "IP").build();

                        descs.add(new DefaultLinkDescription(
                                local, remote, Link.Type.OPTICAL, false, annotations));
                    }
                }
            }

            else {
                log.info("Sin vecinos..");
            }

        }


        return descs;
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

}
