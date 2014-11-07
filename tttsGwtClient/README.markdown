## PVN Systems TTTS GWT Client Web Appliction

Follow these steps to get started:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Prerequisites: TttsFacadeMS, TttsFeedMS and TttsStrategyMS must be running.

See:
	- tttsFacadeMS2/README.markdown
	- tttsFeedMS/README.markdown
	- tttsStrategyMS/README.markdown
for instructions. 


~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


1. Have gradle installed

2. Change directory into your project:

        $ cd pvnsys/tttsGwtClient

3. Edit JAVA_HOME and PATH in setEnv.cmd

4. Run setEnv.cmd

5. Install missing libs, see readme.tst in tttsGwtClient/missinLib directory

6. Start the application:

        $ gradle clean runJettyWar

7. Browse to http://localhost:8080/tttsGwtClient

8. Generate eclipse project files: $ gradle elipse

Or to use existing .classpath add USERPROFILE_HOME=c:\Users\{username} to Eclipse variables. 
c:\Userc\{username} is a USERPROFILE windows variable, or correspondent Linux variable

