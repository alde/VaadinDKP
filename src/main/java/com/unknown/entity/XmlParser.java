/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author alde
 */
public class XmlParser {

        private String startString = "http://www.wowhead.com/item=";
        private String endString = "&xml";
        private String wowIdString;
        private Element root;

        public XmlParser(String wowidstring) {
                this.wowIdString = wowidstring;
        }
        public String parseXmlTooltip() {

                String output = "";
                try {
                        if (!wowIdString.isEmpty()) {
                                rootXmlElement(startString + wowIdString + endString);
                                if (!isErrorXml(root).equalsIgnoreCase("Item not found!")) {
                                        output = findElement(root, "htmlTooltip");
                                } else {
                                        output = "Item not found!";
                                }
                        }
                } catch (JDOMException ex) {
                        Logger.getLogger(XmlParser.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                        Logger.getLogger(XmlParser.class.getName()).log(Level.SEVERE, null, ex);
                }
                return output;
        }

        private void rootXmlElement(String idString) throws JDOMException, IOException {
                SAXBuilder builder = new SAXBuilder();
                Document doc = builder.build(idString);
                root = doc.getRootElement();
        }

        public String findElement(Element root, String targetElement) {
                final String tooltip = root.getChild("item").getChild(targetElement).getText();
                // System.out.println(tooltip);

                return tooltip;

        }

        private String isErrorXml(Element root) {
                String error = root.getChildText("error");
                if (error != null) {
                        return error;
                } else {
                        return "";
                }
        }
}
