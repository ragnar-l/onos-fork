package org.onosproject.drivers.fujitsu.cli;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.DeviceId;
import org.onosproject.net.behaviour.MxpConfig;
import org.onosproject.net.driver.DriverHandler;
import org.onosproject.net.driver.DriverService;

/**
 * Setea vecinos.
 */
@Command(scope = "onos", name = "mxp-set-neighbor",
        description = "Setea el vecino")
public class MxpReplaceOrCreateNeighbor extends AbstractShellCommand {

    @Argument(index = 0, name = "uri", description = "Device ID",
            required = true, multiValued = false)
    String uri = null;

    @Argument(index = 1, name = "puerto_local", description = "Setea vecino con puerto_local",
            required = true, multiValued = false)
    String puerto_local = null;

    @Argument(index = 2, name = "vecino", description = "Setea vecino",
            required = true, multiValued = false)
    String vecino = null;

    @Argument(index = 3, name = "puerto_vecino", description = "Setea vecino con puerto_vecino",
            required = true, multiValued = false)
    String puerto_vecino = null;

    private DeviceId deviceId;

    @Override
    protected void execute() {
        DriverService service = get(DriverService.class);
        deviceId = DeviceId.deviceId(uri);
        DriverHandler h = service.createHandler(deviceId);
        MxpConfig mxp = h.behaviour(MxpConfig.class);
        String reply = mxp.createOrReplaceNeighbor(puerto_local,vecino,puerto_vecino);
        if (reply != null) {
            print("%s", reply);
        } else {
            print("No reply from %s", deviceId.toString());
        }
    }

}