package org.onosproject.drivers.fujitsu.cli;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.behaviour.MxpConfig;
import org.onosproject.net.DeviceId;
import org.onosproject.net.driver.DriverHandler;
import org.onosproject.net.driver.DriverService;

/**
 * Setea tipo fec cliente.
 */
@Command(scope = "onos", name = "mxp-neighbor-discovery",
        description = "Descubre vecinos")

public class MxpNeighborDiscoveryCommand extends AbstractShellCommand {

    @Argument(index = 0, name = "uri", description = "Device ID",
            required = true, multiValued = false)
    String uri = null;

    private DeviceId deviceId;

    @Override
    protected void execute() {
        DriverService service = get(DriverService.class);
        deviceId = DeviceId.deviceId(uri);
        DriverHandler h = service.createHandler(deviceId);
        MxpConfig mxp = h.behaviour(MxpConfig.class);
        //String reply = mxp.neighborDiscovery();
        String reply = null;
        if (reply != null) {
            print("%s", reply);
        } else {
            print("No reply from %s", deviceId.toString());
        }
    }

}