## PVN Systems TTTS Strategy Service

Do not add real strategy code to this project.
This is a template project, make a copy and rename first.

Or to use existing .classpath add USERPROFILE_HOME=c:\Users\{username} to Eclipse variables. 
c:\Users\{username} is a USERPROFILE windows variable, or correspondent Linux variable

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Prerequisites: TttsFacadeMS and TttsFeedMS must be running.

See 
	- tttsFacadeMS2/README.markdown 
	- tttsFeedMS/README.markdown 
for instructions.


~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

1. Change directory into your clone:

        $ cd pvnsys/tttsStrategyMS

2. Edit JAVA_HOME and PATH in setEnv.cmd

3. Run environment setup: 

		$ setEnv.cmd

4. Build and run TttsStrategyMS with SBT (or see MISC to how build and run an assembly jar):

		$ sbt clean compile run


~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

MISC:
        
1. Generate eclipse project files: 

		$ sbt elipse    
		
Or to use existing .classpath add USERPROFILE_HOME=c:\Users\{username} to Eclipse variables. 
c:\Users\{username} is a USERPROFILE windows variable, or correspondent Linux variable
		

2. Packaging: 

		$ sbt assembly

3. Run packaged jar: 

		$ tttsStrategyMS/target/scala-{VERSION}/java -jar ttts-facade-microservice-assembly-1.0.jar        
    
4. Learn more at https://www.pvnsys.com/ (TBD)

