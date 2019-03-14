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
     * @param tipotrafico input data in string
     * @return response string
     */
    String setTipoTrafico(String tipotrafico);


    /**
     * Setea el uso de fec linea.
     *
     * @param tipofeclinea input data in string
     * @return response string
     */
    String setTipoFecLinea(String tipofeclinea);


    /**
     * Setea el uso de fec cliente.
     *
     * @param tipofeccliente input data in string
     * @return response string
     */
    String setTipoFecCliente(String tipofeccliente);


    /**
     * Setea el uso de fec cliente.
     *
     * @param edfaoutputpower input data in string
     * @return response string
     */
    String setEdfaOutPower(String edfaoutputpower);


    /**
     * Setea la frecuencia de las notificaciones del dispositivo.
     *
     * @param timenotifyconfig time data in string
     * @return response string
     */
    String setTimeToNotify(String timenotifyconfig);


    /**
     * Setea el valor del EDFA a notificar.
     *
     * @param valuenotifyconfig time data in string
     * @return response string
     */
    String setValueEdfaNotify(String valuenotifyconfig);


    /**
     * Setea el valor del rx_power a notificar.
     *
     * @param valuerxpowernotifyconfig time data in string
     * @return response string
     */
    String setValueRxPowerNotify(String valuerxpowernotifyconfig);

    /**
     * Setea el valor del rx_power a notificar.
     *
     * @param deviceneighbors time data in string
     * @return response string
     */
    String setDeviceNeighbors(String deviceneighbors);

    /**
     * Setea el valor del rx_power a notificar.
     *
     * @param local_port puerto local con quien formar vecino
     * @param neighbor numero de serie del dispositivo vecino
     * @param remote_port puerto remoto con quien se forma el vecino
     * @return response string
     */
    String createOrReplaceNeighbor(String local_port, String neighbor, String remote_port);


    /**
     * Setea el valor del rx_power a notificar.
     *
     * @param puerto puerto del dispositivo local que eliminar
     * @return response string
     */
    String removeNeighbor(String puerto);

    /**
     * Rpc para aplicar configuracion en el MXP.
     *
     * @return response string
     */
    String rpcApplyConfig();

    /**
     * Rpc settings MXP.
     *
     * @return response string
     */
    String rpcSettingsConfig();

}
