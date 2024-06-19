# Distributed-Text-Mining
Distributed system for text mining in books using Java, tf-idf and load balancing.

## Stack and libraries 
- Java
  - `java`
  - `version 21`
- Books
  - `txt files`

## How to Start
Verify that you have the required java and javac version installed.
```shell
java -version
javac -version
```
In case you do not have it, please make the process of indicated in their respective official pages.

In the folder where the project is located, add a folder named LIBROS_TXT with several txt files.

Compile the necessary files for the Processing servers and execute it.
```shell
javac Book.java Answers.java ServerProcess.java
java ServerProcess [port]
```

Compile the necessary files for the main server.
```shell
javac Book.java Answers.java ServerMain.java
```

Define the port on which the main server will be located and run. The processing servers will have to be in the addresses localhost:8091, localhost:8092 and localhost:8093.
```shell
java ServerMain [port] 
```

Define the addresses of each of the processing servers along with their ports and run. The default port will be 80.
```shell
java ServerMain [address1:port] [address2:port] [address3:port]
```

Compile the necessary files for the Client and run it. The default address of the main server is localhost:80, but you can define another address along with its port.
```shell
javac Book.java Answers.java GUI_Cliente.java Cliente.java
java Cliente.java [addressM:port]
```

Compile the necessary files for Resource Monitoring and run it. The default addresses along with their ports for the main server and processing servers are localhost:8090, localhost:8091, localhost:8092 and localhost:8093, but addresses and ports can be defined anyway.
```shell
javac WebClient.java Aggregator.java GUI_Monitor.java
java GUI_Monitor.java [addressM:port] [address1:port] [address2:port] [address3:port]
```