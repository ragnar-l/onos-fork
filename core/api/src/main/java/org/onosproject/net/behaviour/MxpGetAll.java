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
package org.onosproject.net.behaviour;

import com.google.common.annotations.Beta;
import org.onosproject.net.driver.HandlerBehaviour;

/**
 * Device behaviour to get all available data in vOLT.
 */
@Beta
public interface MxpGetAll extends HandlerBehaviour {

    /**
     * Obtain all available data in the device.
     *
     * @return response string
     */
    String getAll();

    /**
     * Obtain config container available in mxp device.
     *
     * @return response string
     */
    String getConfigContainer();

    /**
     * Obtain state container available in mxp device.
     *
     * @return response string
     */
    String getStateContainer();

    /**
     * Obtain state misc container available in mxp device.
     *
     * @return response string
     */
    String getStateMiscContainer();

    /**
     * Obtain state tx/rx alarm container available in mxp device.
     *
     * @return response string
     */
    String getStateTxRxAlarmContainer();

    /**
     * Obtain state power container available in mxp device.
     *
     * @return response string
     */
    String getStatePowerContainer();

    /**
     * Obtain state dsp container available in mxp device.
     *
     * @return response string
     */
    String getDspContainer();

    /**
     * Obtain state edfa container available in mxp device.
     *
     * @return response string
     */
    String getEdfaContainer();

    /**
     * Obtain state temp/hum container available in mxp device.
     *
     * @return response string
     */
    String getTempHumContainer();

    /**
     * Obtain state XFP1 container available in mxp device.
     *
     * @return response string
     */
    String getXFPOneContainer();

    /**
     * Obtain state XFP2 container available in mxp device.
     *
     * @return response string
     */
    String getXFPTwoContainer();

    /**
     * Obtain state XFP3 container available in mxp device.
     *
     * @return response string
     */
    String getXFPThreeContainer();

    /**
     * Obtain state XFP4 container available in mxp device.
     *
     * @return response string
     */
    String getXFPFourContainer();

}
