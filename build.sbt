import com.gu.riffraff.artifact.BuildInfo

name := "google-search-index-checker"

organization := "com.gu"

description:= "Checking whether Guardian content is available in google search"

version := "1.0"

scalaVersion := "3.3.7"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-release:11"
)

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.2.3",
  "com.amazonaws" % "aws-lambda-java-events" % "3.11.4",
  "net.logstash.logback" % "logstash-logback-encoder" % "8.1",
  "org.slf4j" % "log4j-over-slf4j" % "2.0.12", //  log4j-over-slf4j provides `org.apache.log4j.MDC`, which is dynamically loaded by the Lambda runtime
  "ch.qos.logback" % "logback-classic" % "1.5.19",
  "com.lihaoyi" %% "upickle" % "3.3.1",
  "com.madgag" %% "scala-collection-plus" % "1.0.0",
  "org.scanamo" %% "scanamo" % "1.0.0",
  ("com.gu" %% "content-api-client-default" % "25.0.1").cross(CrossVersion.for3Use2_13),
  "org.scalatest" %% "scalatest" % "3.2.19" % Test

) ++ Seq("ssm", "url-connection-client").map(artifact => "software.amazon.awssdk" % artifact % "2.25.29")

dependencyOverrides += "io.netty" % "netty-handler" % "4.1.128.Final"

enablePlugins(RiffRaffArtifact, BuildInfoPlugin)

assemblyJarName := s"${name.value}.jar"
riffRaffPackageType := assembly.value
riffRaffArtifactResources := Seq(
  (assembly/assemblyOutputPath).value -> s"${name.value}/${name.value}.jar",
  file("cdk/cdk.out/GoogleSearchIndexChecker-PROD.template.json") -> s"cdk.out/GoogleSearchIndexChecker-PROD.template.json",
  file("cdk/cdk.out/riff-raff.yaml") -> s"riff-raff.yaml"
)

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case _ => MergeStrategy.first
}

buildInfoPackage := "ophan.google.index.checker"
buildInfoKeys := {
  lazy val buildInfo = BuildInfo(baseDirectory.value)
  Seq[BuildInfoKey](
    "buildNumber" -> buildInfo.buildIdentifier,
    "gitCommitId" -> buildInfo.revision,
    "buildTime" -> System.currentTimeMillis
  )
}
