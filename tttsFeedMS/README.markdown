## PVN Systems TTTS Feed Service

Follow these steps to get started:

1. Have java 1.7 installed

2. Have sbt installed (http://www.scala-sbt.org/)

3. Have git installed (http://git-scm.com/downloads)

4. !!! Do p.9 instead (Have Kafka installed (tested version bundled with kafka_2.10-0.8.1.1). Comes with Zookeeper.)

5. Git-clone this repository.

        $ git clone https://github.com/fist1231/pvnsys.git

6. Change directory into your clone:

        $ cd pvnsys/tttsFeedMS

7. Edit JAVA_HOME and PATH in setEnv.cmd

8. Run setEnv.cmd

9. Build Kafka from sources against Scala 2.11.2. See tttsFacadeMS2/read.me for instructions

10. Start Zookeeper on localhost:2181, or adjust resources/application.conf accordingly :
	zookeeper-server-start.bat ..\..\config\zookeeper.properties
	(see instructions in help.me for details)

11. Start Kafka server on localhost:9092, or adjust resources/application.conf accordingly :
	kafka-server-start.bat ..\..\config\server.properties
	(see instructions in help.me for details)

12.	Launch SBT:

        $ sbt clean compile

13. Start the application:

        $ sbt run

14. Stop the application:

        $ Ctrl + C
        
15. Generate eclipse project files: $sbt elipse

16. Packaging: sbt assembly

17. Run packaged jar: tttsFeedMS/target/scala-{VERSION}/java -jar ttts-feed-microservice-assembly-1.0.jar        

18. Learn more at https://www.pvnsys.com/

