name := "HdfsUntars"

version := "1.0"

scalaVersion := "2.12.1"

mainClass := Some("HdfsTars")
unmanagedResourceDirectories in Compile += baseDirectory.value /"src/main/resource"
resourceDirectory in Compile := baseDirectory.value / "src/webapp"
resourceDirectory in Compile := baseDirectory.value / "src/main/resource"

//unmanagedSourceDirectories in Compile += baseDirectory.value /"src"
//resourceDirectory in Compile <<= baseDirectory(_ / "src/main/webapp")



resourceDirectory in assembly := baseDirectory.value / "src/main/resource"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
libraryDependencies ++= Seq(
  "org.apache.hadoop" % "hadoop-common" % "2.8.1",
  "org.apache.hadoop" % "hadoop-hdfs" % "2.8.1",
  "org.apache.hadoop" % "hadoop-client" % "2.8.1",
  "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "2.8.1",
  "org.apache.hadoop" % "hadoop-mapreduce-client-common" % "2.8.1",
  // https://mvnrepository.com/artifact/org.kamranzafar/jtar
 // "org.kamranzafar" % "jtar" % "2.3",
    // https://mvnrepository.com/artifact/com.jsuereth/scala-arm
  "com.jsuereth" %% "scala-arm" % "2.0"
)
// https://mvnrepository.com/artifact/org.apache.ant/ant
   libraryDependencies += "org.apache.ant" % "ant" % "1.10.1"

//resolvers += "bintray-sbt-plugins" at "http://dl.bintray.com/sbt/sbt-plugin-releases"
//addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.11.2")
