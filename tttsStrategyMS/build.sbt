import AssemblyKeys._ // put this at the top of the file
assemblySettings

name := """ttts-strategy-microservice"""

organization  := "com.pvnsys.ttts"

version       := "1.0"

scalaVersion  := "2.11.2"

crossPaths := false

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
//  "snapshots"           at "http://oss.sonatype.org/content/repositories/snapshots",
//  "releases"            at "http://oss.sonatype.org/content/repositories/releases",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Spray repository" at "http://repo.spray.io/",
  "JBoss repository" at "https://repository.jboss.org/nexus/content/groups/public/"
)

libraryDependencies ++= {
  val akkaVersion = "2.3.6"
  val sprayVersion = "1.3.2"
  Seq(
//    "com.typesafe.akka"   %%  "akka-stream-experimental" % "0.4",
    "com.typesafe.akka"   %%  "akka-stream-experimental" % "0.11",
    "com.typesafe.scala-logging" %%  "scala-logging-slf4j"      % "2.1.2",
    "io.spray"            %%  "spray-json"     % "1.3.1",
    "io.spray"            %%   "spray-can"     % sprayVersion,
    "io.spray"            %%   "spray-routing" % sprayVersion,
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.2",
//	"org.apache.kafka" % "kafka_2.10" % "0.8.1.1"
//	    exclude("javax.jms", "jms")
//	    exclude("com.sun.jdmk", "jmxtools")
//		    exclude("com.sun.jmx", "jmxri"),
    "com.typesafe.akka"   %%  "akka-actor"     % akkaVersion,
    "com.typesafe.akka"   %%  "akka-slf4j"     % akkaVersion,
//=== Kafka dependencies    
    "log4j"   			  %   "log4j"     	   % "1.2.16",
//    "org.slf4j" 		  %   "slf4j-log4j12"  % "1.7.7",
    "com.101tec"   		  %   "zkclient"   	   % "0.3",
    "com.yammer.metrics"  %   "metrics-core"   % "2.2.0",
    "com.typesafe.scala-logging" %%  "scala-logging-slf4j"      % "2.1.2",
//    "org.slf4j" 		  %  "slf4j-simple"    % "1.7.7",
    "ch.qos.logback"      %   "logback-core"             % "1.1.2",
    "ch.qos.logback"      %   "logback-classic"          % "1.1.2",
//    compile 'org.apache.zookeeper:zookeeper:3.4.6'
//    compile 'com.101tec:zkclient:0.3'
//    compile 'com.yammer.metrics:metrics-core:2.2.0'
//    compile 'net.sf.jopt-simple:jopt-simple:3.2'
    "com.typesafe.akka"   %%  "akka-testkit"   % akkaVersion   % "test",
    "io.spray"            %%  "spray-testkit"  % sprayVersion  % "test",
    "org.scalatest"       %%  "scalatest"      % "2.2.0" % "test",
    "junit"               %   "junit"          % "4.11"  % "test",
    "org.specs2"          %%  "specs2"         % "2.3.11" % "test"
  )
}

seq(Revolver.settings: _*)
