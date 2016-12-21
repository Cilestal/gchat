package org.ml.gchat.managers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

/**
 * Date: 17.10.2016
 *
 * @author Michael Lang
 * @version 1.0
 */
public class ClientConfigManagerTest {
    ConfigurationManager cfm;

    @Before
    public void setUp() {
        cfm = new ConfigurationManager("./src/Test/resources/client_config.xml");
    }

    @Test
    public void testLoadAndSave() {
        Properties properties = cfm.getProperties();
        properties.put("host", "127.0.0.1");
        properties.put("port", "5454");
        cfm.save();

        cfm.load();
        Assert.assertTrue(properties.size() == 2);
    }

}