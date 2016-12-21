package org.ml.gchat.engine;

/**
 * Date: 21.10.2016
 *
 * @author Michael Lang
 * @version 1.0
 */
public abstract class ServerBuilder {
    protected int[] ports;
    protected LoginLogic loginLogic;

    protected Server server;

    public ServerBuilder() {
    }

    public ServerBuilder(LoginLogic loginLogic) {
        this.loginLogic = loginLogic;
    }

    public void createServer() {
        server = new Server(ports, loginLogic);
    }

    abstract void buildServerTitle();

    abstract void buildPorts();

    abstract void buildWelcomeMessage();

    abstract void buildShowWelcomeMessage();

    /**
     * Инициализирует порты {@link Server#ports}
     * создает сервер с заданными портами {@link Server#ports}
     * устанавливает значение {@link Server#serverTitle},
     * устанавливает значение {@link Server#welcomeMessageText}
     * устанавливает значение {@link Server#welcomeMessage}
     * устанавливает значение {@link Server#bufferSize}
     * и инициализирует {@link Server#serverBuffer}
     * @return Собранный сервер.
     */
    public Server buildServer(){
        buildPorts();
        createServer();
        buildServerTitle();
        buildWelcomeMessage();
        buildShowWelcomeMessage();
        return this.server;
    }

    public Server getServer() {
        return server;
    }
}
