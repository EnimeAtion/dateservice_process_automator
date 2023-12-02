package za.co.rmb.copp_clark_uat_uploader.constants_reader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import za.co.rmb.copp_clark_uat_uploader.file_manager.FileManager;
import za.co.rmb.copp_clark_uat_uploader.utilities.Utilities;


public class ConstantsReader {
    public static Document document;
    private static final Logger LOGGER = LogManager.getLogger(FileManager.class);

    /**
     * @throws Exception
     */
    public ConstantsReader() throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        document = dBuilder.parse(Utilities.getXMLFilePath().toString());
    }

    private String getValue(String key, String elementName) {

        LOGGER.info("Getting value for key: " + key + ", element: " + elementName);  // Print the key and element
        NodeList nodeList = document.getElementsByTagName("constant");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element el = (Element) nodeList.item(i);
            LOGGER.info("Checking element with key: " + el.getAttribute("key"));  // Print the key of the current element
            if (el.getAttribute("key").equals(key)) {
                NodeList childNodes = el.getElementsByTagName(elementName);
                if (childNodes.getLength() > 0) {
                    return childNodes.item(0).getTextContent();
                }
            }
        }
        LOGGER.error("Key not found: " + key);
        return null;
    }
    public String getUrl(String key) {
        return getValue(key, "url");
    }
    public String getUsername(String key) {
        return getValue(key, "username");
    }
    public String getHostname(String key) {
        return getValue(key, "hostname");
    }
    public String getPassword(String key) {
        return getValue(key, "password");
    }

}

