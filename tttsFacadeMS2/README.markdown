## PVN Systems TTTS Facade Service

Follow these steps to get started:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Prerequisites: java, sbt, gradle.

1. Have java 1.7 installed

2. Have sbt installed (http://www.scala-sbt.org/)

3. Have git installed (http://git-scm.com/downloads)

4. Have gradle 2.1 installed (http://www.gradle.org/downloads)

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

1. Build Kafka from sources against Scala 2.11.2. See tttsFacadeMS2/help.me for instructions

2. Git-clone source code.

        $ git clone https://github.com/fist1231/pvnsys.git

3. Change directory into your clone:

        $ cd pvnsys/tttsFacadeMS2

4. Edit JAVA_HOME and PATH in setEnv.cmd

5. Run environment setup: 

		$ setEnv.cmd

6. Have running Zookeeper on localhost:2181 (see instructions in help.me for details):
	
		$ zookeeper-server-start.bat ..\..\config\zookeeper.properties
	
7. Have running Kafka server on localhost:9092 (see instructions in help.me for details):
	
		$ kafka-server-start.bat ..\..\config\server.properties

8.	Build and run TttsFacadeMS2 with SBT (or see MISC to how build and run an assembly jar):

		$ sbt clean compile run

9.	Have TttsFeedMS and TttsStrategyMS built and running. See instructions in correspondent projects.

10. Browse to http://localhost:9696/

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

MISC:
        
1. Generate eclipse project files: 

		$ sbt elipse    
		
Or to use existing .classpath add USERPROFILE_HOME=c:\Users\{username} to Eclipse variables. 
c:\Users\{username} is a USERPROFILE windows variable, or correspondent Linux variable
		

2. Packaging: 

		$ sbt assembly

3. Run packaged jar: 

		$ tttsFacadeMS2/target/scala-{VERSION}/java -jar ttts-facade-microservice-assembly-1.0.jar        
    
4. Learn more at https://www.pvnsys.com/ (TBD)

