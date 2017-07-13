# gchat
Simple Java NIO client-server chat<br>
## Build projects
To build the entire Gradle project, you should run the following in the root of the checkout.<br>
```./gradlew build```<br>
<b>Build server</b><br>
```./gradlew server:build```<br>
<b>Build client</b><br>
```./gradlew client:build```<br>
## Quick Start Instructions
### Server
``` 
java -cp ./server-1.0-SNAPSHOT.jar dp.ua.michaellang.gchat.ConsoleServer
2017-07-13 21:44:02 DEBUG Server.java:74  Method: init       - Selector open: true
2017-07-13 21:44:02 DEBUG Server.java:77  Method: init       - Register port: 6868
2017-07-13 21:44:02 DEBUG Server.java:77  Method: init       - Register port: 5454
List of console commands:
-addUser
-removeUser
-exit
```
## Client
![alt text](https://raw.githubusercontent.com/Cilestal/gchat/master/screenshots/1.png)
![alt text](https://raw.githubusercontent.com/Cilestal/gchat/master/screenshots/2.png)
