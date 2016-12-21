package org.ml.gchat.engine;

import org.ml.gchat.managers.ConfigurationManager;

/**
 * Date: 21.10.2016
 *
 * @author Michael Lang
 * @version 1.0
 */
public class ConfigServerBuilder extends ServerBuilder {
    private ConfigurationManager cfm;

    public ConfigServerBuilder(ConfigurationManager cfm) {
        super();
        this.cfm = cfm;
    }

    public ConfigServerBuilder(LoginLogic loginLogic, ConfigurationManager cfm) {
        super(loginLogic);
        this.cfm = cfm;
    }

    @Override
    public void buildServerTitle() {
        String serverTitle = cfm.getProperty("server-title");
        if (!isValid(serverTitle)) {
            super.server.setServerTitle(serverTitle);
        }
    }

    @Override
    public void buildPorts() {
        String portsStr = cfm.getProperty("ports");

        if (!isValid(portsStr)) {
            String[] split = portsStr.split("[,]");
            int[] ports = new int[split.length];
            for (int i = 0; i < split.length; i++) {
                ports[i] = Integer.parseInt(split[i]);
            }
            this.ports = ports;
        }
    }

    @Override
    public void buildWelcomeMessage() {
        String welcomeMessage = cfm.getProperty("welcome-message");

        if (!isValid(welcomeMessage)) {
            super.server.setWelcomeMessageText(welcomeMessage);
        }
    }

    @Override
    public void buildShowWelcomeMessage() {
        String showWelcomeMessage = cfm.getProperty("show-welcome-message");

        if (!isValid(showWelcomeMessage)) {
            boolean swm = Boolean.parseBoolean(showWelcomeMessage);
            super.server.setShowWelcomeMessage(swm);
        }
    }

    private boolean isValid(String serverTitle) {
        return serverTitle == null || serverTitle.length() == 0;
    }
}
