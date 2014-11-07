## PVN Systems TTTS Facade Service

Follow these steps to get started:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Prerequisites: java, sbt, gradle.

1. Have java 1.7 installed

2. Have sbt installed (http://www.scala-sbt.org/)

3. Have git installed (http://git-scm.com/downloads)

4. Have gradle 2.1 installed (http://www.gradle.org/downloads)

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

5. Build Kafka from sources against Scala 2.11.2. See tttsFacadeMS2/help.me for instructions

6. Git-clone source code.

        $ git clone https://github.com/fist1231/pvnsys.git

7. Change directory into your clone:

        $ cd pvnsys/tttsFacadeMS

8. Edit JAVA_HOME and PATH in setEnv.cmd

9. Run setEnv.cmd

10. Have running Zookeeper on localhost:2181:
	zookeeper-server-start.bat ..\..\config\zookeeper.properties
	(see instructions in help.me for details)

11. Have running Kafka server on localhost:9092:
	kafka-server-start.bat ..\..\config\server.properties
	(see instructions in help.me for details)

12.	Build and run TttsFacadeMS2 with SBT:

13.	Have TttsFeedMS and TttsStrategyMS built and running. See instructions in correspondent projects.

14. Browse to http://localhost:9696/

====================================================================

Misc:
        
15. Generate eclipse project files: $sbt elipse    

16. Packaging: sbt assembly

17. Run packaged jar: tttsFacadeMS2/target/scala-{VERSION}/java -jar ttts-facade-microservice-assembly-1.0.jar        
    
18. Learn more at https://www.pvnsys.com/

