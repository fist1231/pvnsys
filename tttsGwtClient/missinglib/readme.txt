mvn install:install-file -DgroupId=highcharts -DartifactId=highcharts -Dversion=1.5.0 -Dpackaging=jar -Dfile=missinglib/highcharts-1.5.0.jar
mvn install:install-file -DgroupId=smartgwt -DartifactId=smartgwt -Dversion=4.1 -Dpackaging=jar -Dfile=missinglib/smartgwt.jar
mvn install:install-file -DgroupId=smartgwt-skins -DartifactId=smartgwt-skins -Dversion=4.1 -Dpackaging=jar -Dfile=missinglib/smartgwt-skins.jar

