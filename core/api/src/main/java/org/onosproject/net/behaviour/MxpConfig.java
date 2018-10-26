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
 * Device behaviour to obtain and set parameters of ONUs in vOLT.
 */
@Beta
public interface MxpConfig extends HandlerBehaviour {

    /**
     * Setea un tipo de trafico en el dispositivo.
     *
     * @param tipo_trafico input data in string
     * @return response string
     */
    String setTipoTrafico(String tipo_trafico);


    /**
     * Setea el uso de fec linea.
     *
     * @param tipo_fec_linea input data in string
     * @return response string
     */
    String setTipoFecLinea(String tipo_fec_linea);


    /**
     * Setea el uso de fec cliente.
     *
     * @param tipo_fec_cliente input data in string
     * @return response string
     */
    String setTipoFecCliente(String tipo_fec_cliente);


    /**
     * Setea el uso de fec cliente.
     *
     * @param edfa_output_power input data in string
     * @return response string
     */
    String setEdfaOutPower(String edfa_output_power);


    /**
     * Setea la frecuencia de las notificaciones del dispositivo.
     *
     * @param time_notify_config time data in string
     * @return response string
     */
    String setTimeToNotify(String time_notify_config);


    /**
     * Setea el valor del EDFA a notificar. (umbral)
     *
     * @param value_notify_config time data in string
     * @return response string
     */
    String setValueEdfaNotify(String value_notify_config);


    /**
     * Setea el valor del rx_power a notificar. (umbral)
     *
     * @param value_rx_power_notify_config time data in string
     * @return response string
     */
    String setValueRxPowerNotify(String value_rx_power_notify_config);


    /**
     * Rpc para aplicar configuracion en el MXP
     *
     * @return response string
     */
    String rpcApplyConfig();

}
