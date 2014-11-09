## PVN Systems TTTS EngineF Service

This is an engine that runs simulations and real operations based on an input of some StrategyMS


~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Prerequisites: TttsFacadeMS, TttsFeedMS and tttsStrategyMS must be running.

See 
	- tttsFacadeMS2/README.markdown 
	- tttsFeedMS/README.markdown 
	- tttsStrategyMS/README.markdown 
for instructions.


~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

1. Change directory into your clone:

        $ cd pvnsys/tttsEngineFMS

2. Edit JAVA_HOME and PATH in setEnv.cmd

3. Run environment setup: 

		$ setEnv.cmd

4. Build and run TttsSEngineFMS with SBT (or see MISC to how build and run an assembly jar):

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

		$ tttsEngineFMS/target/scala-{VERSION}/java -jar ttts-engineF-microservice-assembly-1.0.jar        
    
4. Learn more at https://www.pvnsys.com/ (TBD)

