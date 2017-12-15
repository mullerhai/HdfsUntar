object testOne {

  def main(args: Array[String]): Unit = {

    val filepath="prefixfile.properties"
//    val pertiesPath=CompressUtils.getClass.getClassLoader.getResourceAsStream(filepath)
//    val fsd=CompressUtils.getClass.getClassLoader.getResource(filepath).getPath
//    println(fsd)
    //val propertiesPath="prefixfile.properties"
    val propertiesPath="/usr/local/readme.properties"
    val proSeq=CompressUtils.converOuterPropertiesToSeq(propertiesPath)(",")
   // val proSeq=CompressUtils.converInnerPropertiesToSeq(filepath)("&")
    proSeq.foreach(println(_))
//    //val propertiesPath="prefixfile.properties"
//    val propertiesPath="/usr/local/readme.properties"
//    //val proSeq=CompressUtils.converOuterPropertiesToSeq(propertiesPath)(",")
//    val proSeq=CompressUtils.converInnerPropertiesToSeq(propertiesPath)(",")
//    proSeq.foreach(println(_))
  }
}
