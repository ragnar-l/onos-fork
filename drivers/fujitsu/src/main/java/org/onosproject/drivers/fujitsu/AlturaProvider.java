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
import com.google.common.collect.Maps;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onosproject.incubator.net.faultmanagement.alarm.*;
import org.onosproject.net.DeviceId;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.netconf.*;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Provider which uses an Alarm Manager to keep track of device notifications.
**/

@Component(immediate = true)
public class AlturaProvider extends AbstractProvider implements AlarmProvider {

    public static final String ACTIVE = "active";
    private final Logger log = getLogger(getClass());


    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected AlarmProviderRegistry providerRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected NetconfController controller;

    protected AlarmProviderService providerService;


    public AlturaProvider() {
        super(new ProviderId("netconff", "org.onosproject.netconff"));
    }



    private Map<DeviceId, InternalNotificationListener> idNotificationListenerMap = Maps.newHashMap();

    private NetconfDeviceListener deviceListener = new InnerDeviceListener();


    @Activate
    public void activate() {
        providerService = providerRegistry.register(this);
        controller.getNetconfDevices().forEach(id -> {
            NetconfDevice device = controller.getNetconfDevice(id);
            NetconfSession session = device.getSession();
            InternalNotificationListener listener = new InternalNotificationListener(device.getDeviceInfo());
            session.addDeviceOutputListener(listener);
            idNotificationListenerMap.put(id, listener);
        });
        controller.addDeviceListener(deviceListener);
        log.info("AlturaProvider Started");
    }

    @Deactivate
    public void deactivate() {
        providerRegistry.unregister(this);
        idNotificationListenerMap.forEach((id, listener) -> {
            controller.getNetconfDevice(id)
                    .getSession()
                    .removeDeviceOutputListener(listener);
        });
        controller.removeDeviceListener(deviceListener);
        providerService = null;
        log.info("AlturaProvider Stopped");
    }

    @Override
    public void triggerProbe(DeviceId deviceId) {
        log.debug("Alarm probe triggered with {}", deviceId);
    }

    private void triggerProbe(DeviceId deviceId, Collection<Alarm> alarms) {

        providerService.updateAlarmList(deviceId, alarms);

    }


    public void pushEvent(NetconfDeviceOutputEvent e){
        idNotificationListenerMap.forEach((id, listener) -> {
            if (e.getDeviceInfo().getDeviceId() == id) {
                listener.event(e);
            }
        });
    }

    private class InternalNotificationListener
            extends FilteringNetconfDeviceOutputEventListener
            implements NetconfDeviceOutputEventListener {

        InternalNotificationListener(NetconfDeviceInfo deviceInfo) {
            super(deviceInfo);
        }

        @Override
        public void event(NetconfDeviceOutputEvent event) {
            if (event.type() == NetconfDeviceOutputEvent.Type.DEVICE_NOTIFICATION) {
                DeviceId deviceId = event.getDeviceInfo().getDeviceId();
                String message = event.getMessagePayload();
                InputStream in = new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
                Collection<Alarm> newAlarms = new ArrayList<>();
                newAlarms.add(new DefaultAlarm.Builder(AlarmId.alarmId(deviceId, "WARNING CONFIG"),
                        deviceId, "[ALARM] mux-notify xmlnnnnnnns; Inconsistent config with neighbor",
                        Alarm.SeverityLevel.MINOR,
                        System.currentTimeMillis()).build());
                triggerProbe(deviceId, newAlarms);
            }
        }
    }

    private class InnerDeviceListener implements NetconfDeviceListener {

        @Override
        public void deviceAdded(DeviceId deviceId) {
            NetconfDevice device = controller.getNetconfDevice(deviceId);
            NetconfSession session = device.getSession();
            InternalNotificationListener listener = new InternalNotificationListener(device.getDeviceInfo());
            session.addDeviceOutputListener(listener);
            idNotificationListenerMap.put(deviceId, listener);
        }

        @Override
        public void deviceRemoved(DeviceId deviceId) {
            idNotificationListenerMap.remove(deviceId);
        }
    }

        }



