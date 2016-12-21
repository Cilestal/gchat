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

    public String getProperty(String str){
        return properties.getProperty(str);
    }

    public Properties getProperties() {
        return properties;
    }
}