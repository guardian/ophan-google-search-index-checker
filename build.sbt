import com.gu.riffraff.artifact.BuildInfo

name := "google-search-index-checker"

organization := "com.gu"

description:= "Checking whether Guardian content is available in google search"

version := "1.0"

scalaVersion := "3.2.2"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-release:11"
)

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.2.2",
  "com.amazonaws" % "aws-lambda-java-events" % "3.11.2",
  "net.logstash.logback" % "logstash-logback-encoder" % "7.3",
  "org.slf4j" % "log4j-over-slf4j" % "2.0.7", //  log4j-over-slf4j provides `org.apache.log4j.MDC`, which is dynamically loaded by the Lambda runtime
  "ch.qos.logback" % "logback-classic" % "1.4.8",
  "com.lihaoyi" %% "upickle" % "3.1.0",

  "com.madgag" %% "scala-collection-plus" % "0.11",
  "com.google.http-client" % "google-http-client-gson" % "1.43.3",
  "com.google.apis" % "google-api-services-customsearch" % "v1-rev20230319-2.0.0",
  "org.scanamo" %% "scanamo" % "1.0.0-M26",
  ("com.gu" %% "content-api-client-default" % "19.0.5").cross(CrossVersion.for3Use2_13),
  "org.scalatest" %% "scalatest" % "3.2.16" % Test

) ++ Seq("ssm", "url-connection-client").map(artifact => "software.amazon.awssdk" % artifact % "2.17.295")

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
