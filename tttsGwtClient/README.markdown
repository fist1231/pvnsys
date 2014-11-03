## PVN Systems TTTS GWT Client Web Appliction

Follow these steps to get started:

1. Have Kafka, TttsFacadeMS, TttsFeedMS (read TttsFacadeMS instructions on how to start them)

2. Have gradle installed

3. Change directory into your project:

        $ cd pvnsys/tttsGwtClient

4. Edit JAVA_HOME and PATH in setEnv.cmd

5. Run setEnv.cmd

6. INstall missing libs, see readme.tst in tttsGwtClient/missinLib directory

7. Start the application:

        $ gradle clean runJettyWar

8. Browse to http://localhost:8080/tttsGwtClient

9. Generate eclipse project files: $ gradle elipse    

