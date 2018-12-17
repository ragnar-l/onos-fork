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

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.incubator.net.faultmanagement.alarm.*;
import org.onosproject.mastership.MastershipEvent;
import org.onosproject.mastership.MastershipListener;
import org.onosproject.mastership.MastershipService;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Dictionary;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.concurrent.Executors.newScheduledThreadPool;
import static org.onlab.util.Tools.get;
import static org.onlab.util.Tools.groupedThreads;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Alarm provider capable of polling the environment using the device driver
 * {@link AlarmConsumer} behaviour.
 */
@Component(immediate = true)
public class ProviderAl extends AbstractProvider implements AlarmProvider {

    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected MastershipService mastershipService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected AlarmProviderRegistry providerRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ComponentConfigService cfgService;

    protected AlarmProviderService providerService;










    public ProviderAl() {
        super(new ProviderId("default", "org.onosproject.core"));
    }

    @Activate
    public void activate(ComponentContext context) {
        cfgService.registerProperties(getClass());


        providerService = providerRegistry.register(this);


        log.info("Started");
    }






    @Override
    public void triggerProbe(DeviceId deviceId) {
        log.debug("Alarm probe triggered with {}", deviceId);
    }


    public void triggerProbes(DeviceId deviceId, Collection<Alarm> alarms) {
        providerService.updateAlarmList(deviceId, alarms);
        triggerProbe(deviceId);
    }




}
