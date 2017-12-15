import java.io.FileInputStream
import java.util.Properties

import scala.util.control.Breaks
import scala.util.control.Breaks._

/**
  * Created by linkedmemuller on 11/12/2017.
  */
object protest {


  def test(args:Array[String]):Unit= {

    var a = 0;
    var b = 0;
    var sum = 0
    val numList1 = List(1,2,3,4,5);
    val numList2 = List(11,12,13,34);

    val outer = new Breaks;
    val inner = new Breaks;

    outer.breakable {
      for( a <- numList1){
        println( "Value of a: " + a );
        inner.breakable {
          for( b <- numList2){
            println( "Value of b: " + b );
            sum +=b
            println(sum)
            if(sum >= 35){
              inner.break;
            }
          }
        } // 内嵌循环中断
      }
    } // 外部
  }
  def main(args:Array[String]):Unit={
    var sum = 0
    var ks=10
    val numList1 = List(1,2,6,4,5)
    val numList2 = List(11,12,13,34)
    var is=0
    val inner = new Breaks
    val outer = new Breaks
//    outer.breakable{
    while (ks>2){
      println("ks"+ks)

      inner.breakable {
        while(is < 20) {
        println("inner"+is)
        sum += is
        if (sum >= 35){
          sum=0
          is =0
          inner.break()
        }
          is+=1
      } }
      ks=ks-1
    }
//    }

    println(sum)



    val pro:Properties=new Properties()


    pro.load(new FileInputStream("src/main/resource/prefixfile.properties"))

    println(pro.getProperty("files2"))
    val proNames=pro.propertyNames
//    while (proNames.hasMoreElements){
//     // println(proNames.nextElement()+" ele ")
//      println(getProperties(proNames.nextElement().toString).get + " values")
//    }
   val path=Thread.currentThread().getContextClassLoader.getResource("src/main/resource/prefixfile.properties")

    val pat=protest.getClass.getResource("/")


      println( pat)
    val tarname="biz.losgs"
    val bb=boolFilePrefixContains(tarname)
    println(bb+"  ||tarnaem")
    val pertiesPath=HdfsTars.getClass.getClassLoader.getResource("./prefixfile.properties").getFile
    println(pertiesPath)
    val pros:Properties=new Properties()
    pros.load(new  FileInputStream(pertiesPath))
    // println(HdfsTars.getClass.getResource("/"))
    //   val pertiesPath=HdfsTars.getClass.getClassLoader.getResource("./prefixfile.properties").getPath
    //    println(pertiesPath)
    //    val pro:Properties=new Properties()
    //    pro.load(new  FileInputStream(pertiesPath))
    //pro.load(pertiesPath.toString)

  }

  def  getProperties(keys:String):Option[String]={
    val pro:Properties=new Properties()
    pro.load(new  FileInputStream("src/main/resource/prefixfile.properties"))
    val proval= pro.getProperty(keys)
    return Some(proval)
  }


  def  boolFilePrefixContains(tarEntryName:String):Boolean={

    var res=false
    val loop =new Breaks
    try{
      val pro:Properties=new Properties()
      pro.load(new  FileInputStream("src/main/resource/prefixfile.properties"))
      val  proEmu=pro.propertyNames()
      loop.breakable{
      while (proEmu.hasMoreElements){
        val fliePrefix=pro.getProperty(proEmu.nextElement().toString)
        res= tarEntryName.contains(fliePrefix)

          if(res){
            loop.break()
          }

        }

      }

    }catch{

      case e:Exception => res=false
    }
    return res
  }
}
