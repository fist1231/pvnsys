## PVN Systems TTTS GWT Client Web Appliction

Follow these steps to get started:

1. Have Kafka, TttsFacadeMS, TttsFeedMS (read TttsFacadeMS instructions on how to start them)

2. Have gradle installed

3. Change directory into your project:

        $ cd pvnsys/tttsGwtClient

4. Edit JAVA_HOME and PATH in setEnv.cmd

5. Run setEnv.cmd

6. Start the application:

        $ gradle clean runJettyWar

7. Browse to http://localhost:8080/tttsGwtClient

8. Generate eclipse project files: $ gradle elipse    

