name := "ElasticSearch-Scala"

version := "0.1"

scalaVersion := "2.13.5"

libraryDependencies ++= {

  val akkaVersion       = "2.5.23"
  val akkaHttpVersion   = "10.1.8"
  val elastic4sVersion = "7.10.2"

  Seq(
    // recommended client for beginners
    "com.sksamuel.elastic4s"          %% "elastic4s-client-esjava"        % elastic4sVersion,
    // test kit
    "com.sksamuel.elastic4s"          %% "elastic4s-testkit"              % elastic4sVersion % "test",
    "com.typesafe.akka"               %% "akka-actor"                     % akkaVersion,
    "com.typesafe.akka"               %% "akka-stream"                    % akkaVersion,
    "com.typesafe.akka"               %% "akka-slf4j"                     % akkaVersion,
    "io.spray"                        %% "spray-json"                     % "1.3.5",
    "com.typesafe.akka"               %% "akka-stream-kafka"              % "1.0.4",
    //"com.lightbend.akka"              %% "akka-stream-alpakka-cassandra"  % "1.0-M1",
    "com.typesafe.akka"               %% "akka-http"                      % akkaHttpVersion,
    "com.typesafe.akka"               %% "akka-http-spray-json"           % akkaHttpVersion,
    "org.json4s"                      %% "json4s-jackson"                 % "3.6.6",
    "org.elasticsearch.client"        % "transport"                       % "7.10.2",
    "org.apache.logging.log4j"        % "log4j-api"                       % "2.6.2",
    "org.apache.logging.log4j"        % "log4j-to-slf4j"                  % "2.6.2",
    "ch.qos.logback"                  % "logback-classic"                 % "1.0.9",
    "org.scalatest"                   % "scalatest_2.13"                  % "3.0.8"               % "test"
  )
}

resolvers ++= Seq(
  "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
)
