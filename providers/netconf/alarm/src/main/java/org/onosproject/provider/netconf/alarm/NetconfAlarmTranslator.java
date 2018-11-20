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

package org.onosproject.provider.netconf.alarm;

import com.google.common.collect.ImmutableSet;
import org.onosproject.incubator.net.faultmanagement.alarm.Alarm;
import org.onosproject.incubator.net.faultmanagement.alarm.AlarmId;
import org.onosproject.incubator.net.faultmanagement.alarm.AlarmTranslator;
import org.onosproject.incubator.net.faultmanagement.alarm.DefaultAlarm;
import org.onosproject.net.DeviceId;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;

import org.xml.sax.SAXException;

import static org.slf4j.LoggerFactory.getLogger;
import org.apache.commons.lang.StringUtils;

/**
 * Translates NETCONF notification messages to actions on alarms.
 */
public class NetconfAlarmTranslator implements AlarmTranslator {

    private final Logger log = getLogger(getClass());
    private static final String EVENTTIME_TAGNAME = "eventTime";

    private static final String DISALLOW_DTD_FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";

    private static final String DISALLOW_EXTERNAL_DTD =
            "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    @Override
    public Collection<Alarm> translateToAlarm(DeviceId deviceId, InputStream message) {
        try {
            Collection<Alarm> alarms = new ArrayList<>();
            Document doc = createDocFromMessage(message);

            // parse date element value into long
            Node eventTime = doc.getElementsByTagName(EVENTTIME_TAGNAME).item(0);
            String date = eventTime.getTextContent();
            long timeStamp = parseDate(date);

            // event-specific tag names as alarm descriptions
            Node descriptionNode = eventTime.getNextSibling();
            while (descriptionNode != null) {
                if (descriptionNode.getNodeType() == Node.ELEMENT_NODE) {
                    String description = nodeToString(descriptionNode);
                    if (description.contains("mux-notify")) {
                        String identificador = getNotify(description);
                        alarms.add(new DefaultAlarm.Builder(AlarmId.alarmId(deviceId, identificador),
                                deviceId, description,
                                Alarm.SeverityLevel.WARNING,
                                timeStamp).build());
                    } else {
                        alarms.add(new DefaultAlarm.Builder(AlarmId.alarmId(deviceId, Long.toString(timeStamp)),
                                deviceId, description,
                                Alarm.SeverityLevel.WARNING,
                                timeStamp).build());
                    }
                    descriptionNode = null;
                } else {
                    descriptionNode = descriptionNode.getNextSibling();
                }
            }
            return alarms;
        } catch (SAXException | IOException | ParserConfigurationException |
                UnsupportedOperationException | IllegalArgumentException |
                TransformerException e) {
            log.error("Exception thrown translating message from {}.", deviceId, e);
            return ImmutableSet.of();
        }
    }

    private Document createDocFromMessage(InputStream message)
            throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
        //Disabling DTDs in order to avoid XXE xml-based attacks.
        disableFeature(dbfactory, DISALLOW_DTD_FEATURE);
        disableFeature(dbfactory, DISALLOW_EXTERNAL_DTD);
        dbfactory.setXIncludeAware(false);
        dbfactory.setExpandEntityReferences(false);
        DocumentBuilder builder = dbfactory.newDocumentBuilder();
        return builder.parse(new InputSource(message));
    }

    private void disableFeature(DocumentBuilderFactory dbfactory, String feature) {
        try {
            dbfactory.setFeature(feature, true);
        } catch (ParserConfigurationException e) {
            // This should catch a failed setFeature feature
            log.info("ParserConfigurationException was thrown. The feature '" +
                    feature + "' is probably not supported by your XML processor.");
        }
    }

    private long parseDate(String timeStr)
            throws UnsupportedOperationException, IllegalArgumentException {
        return DateTimeFormatter.ISO_DATE_TIME.parse(timeStr, Instant::from).getEpochSecond();
    }

    private static String nodeToString(Node rootNode) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        DOMSource source = new DOMSource(rootNode);
        transformer.transform(source, new StreamResult(writer));
        return writer.getBuffer().toString();
    }

    /**
     * Retrieving manufacturer of device.
     * @param parseame the return of show version command
     * @return the manufacturer of the device
     */
    private static String getNotify(String parseame) {
        String notify = StringUtils.substringBetween(parseame, "<INFO>", "</INFO>");
        String substring;
        if (notify.contains("[ALARM] ")) {
            substring = notify.substring(8, notify.length());
        } else {
            substring = notify.substring(5, notify.length());
        }
        return substring;
    }
}