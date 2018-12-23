package org.onosproject.drivers.fujitsu.cli;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.DeviceId;
import org.onosproject.net.behaviour.MxpConfig;
import org.onosproject.net.driver.DriverHandler;
import org.onosproject.net.driver.DriverService;



/**
 * Setea el edfa out power.
 */
@Command(scope = "onos", name = "mxp-remove-neighbor",
        description = "Elimina el vecino")
public class MxpRemoveNeighbor extends AbstractShellCommand {

    @Argument(index = 0, name = "uri", description = "Device ID",
            required = true, multiValued = false)
    String uri = null;

    @Argument(index = 1, name = "puerto_local", description = "Elimina informacion de vecino con el puerto local dado",
            required = true, multiValued = false)
    String puerto_local = null;


    private DeviceId deviceId;

    @Override
    protected void execute() {
        DriverService service = get(DriverService.class);
        deviceId = DeviceId.deviceId(uri);
        DriverHandler h = service.createHandler(deviceId);
        MxpConfig mxp = h.behaviour(MxpConfig.class);
        String reply = mxp.removeNeighbor(puerto_local);
        if (reply != null) {
            print("%s", reply);
        } else {
            print("No reply from %s", deviceId.toString());
        }
    }

}