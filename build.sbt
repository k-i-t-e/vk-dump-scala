name := "vk-fetch"
 
version := "1.0" 
      
lazy val `vk-fetch` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.2"

libraryDependencies ++= Seq( ehcache , ws , specs2 % Test , guice )

libraryDependencies += "net.codingwell" %% "scala-guice" % "4.1.0"
libraryDependencies += "com.iheart" %% "ficus" % "1.4.3" // config lib, used by Silhouette,

// Database
libraryDependencies += "org.postgresql" % "postgresql" % "42.1.1"
libraryDependencies ++= Seq(
  "com.h2database" % "h2" % "1.4.196" % Test,
  "com.typesafe.play" %% "play-slick" % "3.0.3",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.3")

// Silhouette config
val silhouetteVer = "5.0.0"
lazy val silhouetteLib = Seq(
  "com.mohiva" %% "play-silhouette" % silhouetteVer,
  "com.mohiva" %% "play-silhouette-password-bcrypt" % silhouetteVer,
  "com.mohiva" %% "play-silhouette-crypto-jca" % silhouetteVer,
  "com.mohiva" %% "play-silhouette-persistence" % silhouetteVer,
  "com.mohiva" %% "play-silhouette-testkit" % silhouetteVer % "test"
)

// VK
libraryDependencies += "com.vk.api" % "sdk" % "0.5.12"

libraryDependencies ++= silhouetteLib

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

      