package dp.ua.michaellang.gchat;

import dp.ua.michaellang.gchat.db.XMLUserBD;
import dp.ua.michaellang.gchat.db.model.User;
import dp.ua.michaellang.gchat.engine.CommonLoginLogic;
import dp.ua.michaellang.gchat.engine.ConfigServerBuilder;
import dp.ua.michaellang.gchat.engine.Server;
import dp.ua.michaellang.gchat.engine.ServerBuilder;
import dp.ua.michaellang.gchat.managers.ConfigurationManager;

import java.util.Scanner;

/**
 * Date: 10.10.2016
 *
 * @author Michael Lang
 * @version 1.0
 */
public class ConsoleServer {
    public static final String DEFAULT_SERVER_CONFIG = "./config/server_config.xml";
    public static final String DEFAULT_SERVER_BD = "./xmldb/user_list.xml";
    public static final String DEFAULT_SERVER_BD_XSD = "./xmldb/user_list.xsd";

    private ConfigurationManager cfm;
    private Server server;
    private XMLUserBD xmlUserBD;
    private Scanner scanner;

    private boolean status;

    public ConsoleServer() {
        this.cfm = new ConfigurationManager(DEFAULT_SERVER_CONFIG);
        cfm.load();
    }

    public void startServer(){
        try {
            this.xmlUserBD = new XMLUserBD(DEFAULT_SERVER_BD, DEFAULT_SERVER_BD_XSD);
            xmlUserBD.loadFromXML();
            CommonLoginLogic loginLogic = new CommonLoginLogic(xmlUserBD);

            ServerBuilder csb = new ConfigServerBuilder(loginLogic, cfm);
            this.server = csb.buildServer();

            Thread thr = new Thread(this.server);
            thr.start();
            this.status = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void readCommands(){
        this.scanner = new Scanner(System.in);
        String mes;
        while (status) {
            mes = scanner.next();
            try {
                command(mes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void command(String mes) throws Exception {
        switch (mes){
            case "-addUser":
                addUser();
                break;
            case "-removeUser":
                removeUser();
                break;
            case "-exit":
                server.close();
                this.status = false;
                xmlUserBD.storeToXML();
                break;
            default:
                printCommands();
        }
    }

    private void removeUser() {
        System.out.println("Enter login: ");
        String login = scanner.next();
        User user = xmlUserBD.getUser(login);
        if(user != null) {
            boolean b = xmlUserBD.removeUser(user);
            if(b) System.out.println("User " + login + " removed.");
        }
    }

    private void addUser() throws Exception {
        System.out.println("Enter login:");
        String login = scanner.next();
        System.out.println("Enter password:");
        String password = scanner.next();

        User user = new User(login);
        user.createPassword(password, false);

        xmlUserBD.addUser(user);
    }

    private void printCommands() {
        System.out.println("List of console commands:");
        System.out.println("-addUser");
        System.out.println("-removeUser");
        System.out.println("-exit");
    }

    public static void main(String[] args) {
        ConsoleServer consoleServer = new ConsoleServer();
        consoleServer.startServer();
        consoleServer.readCommands();
    }
}
