import play.core.PlayVersion
import play.sbt.PlayImport.{cacheApi, guice}

name := """quote-order"""

version := "1.0"
lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)
scalaVersion := "2.13.8"
sources in(Compile, doc) := Seq.empty
publishArtifact in(Compile, packageDoc) := false
resolvers += "Local Maven Repository" at "file:///" + Path.userHome.absolutePath + "/.ivy2/cache"
resolvers += "ali-maven" at "https://maven.aliyun.com/nexus/content/groups/public"
resolvers += "Typesafe Simple Repository" at "https://repo.typesafe.com/typesafe/simple/maven-releases/"
resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/maven-releases/"
resolvers += "maven-central" at "https://repo1.maven.org/maven2/"
resolvers += "jitpack" at "https://jitpack.io"
libraryDependencies ++= Seq(
  javaJdbc,
  guice,
  filters,
  caffeine,
  cacheApi,
  "com.google.inject" % "guice" % "5.1.0",
  "com.google.inject.extensions" % "guice-assistedinject" % "5.1.0",
  "mysql" % "mysql-connector-java" % "8.0.17",
  "commons-io" % "commons-io" % "2.5",
  "commons-validator" % "commons-validator" % "1.5.1",
  "org.bouncycastle" % "bcprov-jdk15on" % "1.55",
  "com.google.code.gson" % "gson" % "2.6.2",
  "com.aliyun" % "aliyun-java-sdk-core" % "3.0.9",
  "com.aliyun" % "aliyun-java-sdk-sms" % "3.0.0-rc1",
  "com.github.martinwithaar" % "encryptor4j" % "0.1",
  "com.github.karelcemus" %% "play-redis" % "2.7.0",
  "commons-httpclient" % "commons-httpclient" % "3.0.1",
  "org.apache.httpcomponents" % "httpclient" % "4.5.3",
  "dom4j" % "dom4j" % "1.6.1",
  "com.aventrix.jnanoid" % "jnanoid" % "2.0.0",
  "com.github.wechatpay-apiv3" % "wechatpay-apache-httpclient" % "0.2.2",
  "com.aliyun.oss" % "aliyun-sdk-oss" % "3.4.2",
  "com.stripe" % "stripe-java" % "21.11.0",
  javaWs
)
val akkaVersion = PlayVersion.akkaVersion
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
  "com.typesafe.play" %% "play-mailer" % "8.0.1",
  "com.typesafe.play" %% "play-mailer-guice" % "8.0.1"
)
libraryDependencies += filters
libraryDependencies += "org.awaitility" % "awaitility" % "2.0.0" % Test
libraryDependencies += "org.assertj" % "assertj-core" % "3.6.2" % Test
libraryDependencies += "org.mockito" % "mockito-core" % "2.1.0" % Test
testOptions in Test += Tests.Argument(TestFrameworks.JUnit, "-a", "-v")

javacOptions ++= Seq("-encoding", "UTF-8")
