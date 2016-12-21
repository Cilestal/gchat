package org.ml.gchat.managers;

import java.io.*;
import java.util.Properties;

/**
 * Date: 14.10.2016
 *
 * @author Michael Lang
 * @version 1.0
 */
public class ConfigurationManager {
    public static final String DEFAULT_CLIENT_CONFIG = "./res/config/client_config.xml";
    public static final String DEFAULT_SERVER_CONFIG = "./res/config/server_config.xml";

    private String path;
    private Properties properties;

    public ConfigurationManager(String path) {
        this.path = path;
        this.properties = new Properties();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Properties load(){
        try(InputStream is = new FileInputStream(this.path)){
            this.properties.loadFromXML(is);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return this.properties;
    }

    public void save(){
        try(OutputStream os = new FileOutputStream(this.path)){
            properties.storeToXML(os, "Client config");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Properties getProperties() {
        return properties;
    }
}