name := """ttts-facade-microservice"""

organization  := "com.pvnsys.ttts"

version       := "1.0"

scalaVersion  := "2.10.3"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Spray repository" at "http://repo.spray.io/",
  "JBoss repository" at "https://repository.jboss.org/nexus/content/groups/public/"
)

libraryDependencies ++= {
  val akkaV = "2.2.3"
  val sprayV = "1.2.0"
  Seq(
    "io.spray"            %%  "spray-json"     % "1.2.5",
    "io.spray"            %   "spray-can"      % sprayV,
    "io.spray"            %   "spray-routing"  % sprayV,
	"org.apache.kafka" % "kafka_2.10" % "0.8.1"
	    exclude("javax.jms", "jms")
	    exclude("com.sun.jdmk", "jmxtools")
	    exclude("com.sun.jmx", "jmxri"),
    "com.typesafe.akka"   %%  "akka-actor"     % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"   % akkaV   % "test",
    "io.spray"            %   "spray-testkit"  % sprayV  % "test",
    "org.scalatest"       %%  "scalatest"      % "2.0"   % "test",
    "junit"               %   "junit"          % "4.11"  % "test",
    "org.specs2"          %%  "specs2"         % "2.2.3" % "test"
  )
}

seq(Revolver.settings: _*)
