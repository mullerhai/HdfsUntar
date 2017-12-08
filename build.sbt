name := "HdfsUntars"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "org.apache.hadoop" % "hadoop-common" % "2.8.1",
  "org.apache.hadoop" % "hadoop-hdfs" % "2.8.1",
  "org.apache.hadoop" % "hadoop-client" % "2.8.1",
  "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "2.8.1",
  "org.apache.hadoop" % "hadoop-mapreduce-client-common" % "2.8.1",
  // https://mvnrepository.com/artifact/org.kamranzafar/jtar
  "org.kamranzafar" % "jtar" % "2.3"
)
// https://mvnrepository.com/artifact/org.apache.ant/ant
//libraryDependencies += "org.apache.ant" % "ant" % "1.10.1"
