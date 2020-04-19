package org.cauldron.game;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Holds and handles persistent settings for the game
 */
public class Settings {
    public static int volume = 100;
    public static boolean barsFollow = true;
    public static String powerIndicator = "both";
    public static boolean powerUpsOnBars = true;
    public static String resolution = "1280x720";
    public static int[] resInt = new int[]{1280, 720};
    public static boolean fullscreen = false;
    public static boolean vsync = true;
    public static boolean fps = false;
    public static final String[] fullResOptions = new String[]{
            "1024x576", "1280x720", "1366x768", "1600x900",
            "1920x1080", "2560x1400", "3480x2160"};
    public static String[] resOptions;
    public static String maxSupportedRes;
    public static String configPath = System.getProperty("user.home") + "/Games/Blast/config.xml";
    public static String configDir = System.getProperty("user.home") + "/Games/Blast";

    /**
     * @return The resolution options available on the current display, based on the maximum supported resolution. 16:9 only.
     */
    public static String[] getResOptions() {
        int i = 0;
        for (i = 0; i < fullResOptions.length; i++) {
            if (fullResOptions[i].equals(maxSupportedRes))
                break;
        }
        resOptions = new String[i];
        for (i = 0; i < resOptions.length; i++) {
            resOptions[i] = fullResOptions[i];
        }
        return resOptions;
    }

    /**
     * Parses settings from the XML file (created if not existing) into the variables held in this object.
     *
     * @return success
     */
    public static boolean read() {
        boolean success = true;

        if (!Files.exists(Paths.get(configPath))) {
            if (!copy(Thread.currentThread().getContextClassLoader().getResourceAsStream("config.xml"),
                    configPath)) {
                System.out.println("Copy failed");
                return false;
            }
        }

        try {
            File inputFile = new File(configPath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            Element settings = doc.getDocumentElement();
            if (settings.getTagName() != "settings")
                return false;
            try {
                volume = Integer.parseInt(settings.getElementsByTagName("volume").item(0).getTextContent());
                if (volume > 100)
                    volume = 100;
                else if (volume < 0)
                    volume = 0;
            } catch (NumberFormatException | NullPointerException | DOMException e) {
                success = false;
                System.out.println("Error getting volume from config!");
            }

            try {
                barsFollow = Boolean.parseBoolean(settings.getElementsByTagName("barsfollow").item(0).getTextContent());
            } catch (DOMException | NullPointerException e) {
                success = false;
                System.out.println("Error getting barsfollow from config!");
            }

            try {
                String temp = settings.getElementsByTagName("powerindicator").item(0).getTextContent();
                if (!(temp.equals("both") || temp.equals("arrow") || temp.equals("bar"))) {
                    System.out.println("Invalid powerindicator from config! Must be 'both', 'bar' or 'arrow'");
                } else {
                    powerIndicator = temp;
                }
            } catch (DOMException | NullPointerException e) {
                success = false;
                System.out.println("Error getting powerindicator from config!");
            }

            try {
                powerUpsOnBars = Boolean.parseBoolean(settings.getElementsByTagName("powerupsonbars").item(0).getTextContent());
            } catch (DOMException | NullPointerException e) {
                success = false;
                System.out.println("Error getting powerupsonbars from config!");
            }

            try {
                String temp = settings.getElementsByTagName("resolution").item(0).getTextContent();
                String found = "";
                for (String res : getResOptions()) {
                    if (temp.equals(res))
                        found = temp;
                }
                if (!found.equals("")) {
                    resolution = found;
                    String[] split = resolution.split("x");
                    resInt[0] = Integer.parseInt(split[0]);
                    resInt[1] = Integer.parseInt(split[1]);
                } else {
                    System.out.println("Invalid resolution from config! Must be one of:\n" + resOptions.toString());
                }
            } catch (DOMException | NullPointerException e) {
                success = false;
                System.out.println("Error getting resolution from config!");
            }

            try {
                fullscreen = Boolean.parseBoolean(settings.getElementsByTagName("fullscreen").item(0).getTextContent());
            } catch (DOMException | NullPointerException e) {
                success = false;
                System.out.println("Error getting fullscreen from config!");
            }

            try {
                vsync = Boolean.parseBoolean(settings.getElementsByTagName("vsync").item(0).getTextContent());
            } catch (DOMException | NullPointerException e) {
                success = false;
                System.out.println("Error getting vsync from config!");
            }

            try {
                fps = Boolean.parseBoolean(settings.getElementsByTagName("fps").item(0).getTextContent());
            } catch (DOMException | NullPointerException e) {
                success = false;
                System.out.println("Error getting fps from config!");
            }
            return success;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Writes the currently held settings variables to the XML file.
     *
     * @return success
     */
    public static boolean write() {
        if (!Files.exists(Paths.get(configPath))) {
            copy(Thread.currentThread().getContextClassLoader().getResourceAsStream("config.xml"),
                    configPath);
        }

        try {
            File inputFile = new File(configPath);
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(inputFile);
            Node settings = doc.getFirstChild();

            NodeList list = settings.getChildNodes();

            for (int temp = 0; temp < list.getLength(); temp++) {
                Node node = list.item(temp);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) node;
                    if ("volume".equals(e.getNodeName())) {
                        e.setTextContent(String.valueOf(volume));
                    }
                    if ("barsfollow".equals(e.getNodeName())) {
                        e.setTextContent(String.valueOf(barsFollow));
                    }
                    if ("powerindicator".equals(e.getNodeName())) {
                        e.setTextContent(powerIndicator);
                    }
                    if ("powerupsonbars".equals(e.getNodeName())) {
                        e.setTextContent(String.valueOf(powerUpsOnBars));
                    }
                    if ("resolution".equals(e.getNodeName())) {
                        e.setTextContent(resolution);
                    }
                    if ("fullscreen".equals(e.getNodeName())) {
                        e.setTextContent(String.valueOf(fullscreen));
                    }
                    if ("vsync".equals(e.getNodeName())) {
                        e.setTextContent(String.valueOf(vsync));
                    }
                    if ("fps".equals(e.getNodeName())) {
                        System.out.println("test");
                        e.setTextContent(String.valueOf(fps));
                    }
                }
            }

            //write the updated document to file or console
            doc.getDocumentElement().normalize();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(configPath));
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);
            System.out.println("XML file updated successfully");

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Copy the default settings XML out of the (potential) jar and to Documents/Game/Blast/config.xml
     *
     * @param source
     * @param destination
     * @return success
     */
    private static boolean copy(InputStream source, String destination) {
        boolean success = true;

        System.out.println("Copying ->" + source + "\n\tto ->" + destination);

        try {
            Files.createDirectories(Paths.get(configDir));
            Files.copy(source, Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            success = false;
        }

        return success;

    }
}
