import java.io.{File, FileInputStream, InputStream}
import java.util.Properties
import java.util.zip.{GZIPInputStream, ZipEntry, ZipInputStream}

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import org.apache.hadoop.fs.{FSDataInputStream, FSDataOutputStream, Path, FileSystem => HDFSFileSystem}
import org.apache.hadoop.io.IOUtils
import org.apache.tools.tar.{TarEntry, TarInputStream}
//import org.kamranzafar.jtar.{TarEntry, TarInputStream}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks

object CompressUtils {

  def main(args: Array[String]): Unit = {

    //val propertiesPath="prefixfile.properties"
    //val proSeq=CompressUtils.converInnerPropertiesToSeq(filepath)("&")
    //proSeq.foreach(println(_))

    //    val propertiesPath="/usr/local/readme.properties"
    //    val proSeq=CompressUtils.converOuterPropertiesToSeq(propertiesPath)(",")
    //    proSeq.foreach(println(_))
  }


  /** *
    * 根据 tar包 后缀 选择解压数据io 流对象
    *
    * @param srcDir
    * @param fs
    * @return
    */
  def tarfileStreamBySuffix(srcDir: String, fs: HDFSFileSystem): TarInputStream = {
    var tarln: TarInputStream = null
    val srcTemp: String = srcDir.toLowerCase()
    try {
      val fsData: FSDataInputStream = fs.open(new Path(srcDir))
      if (srcTemp.endsWith(".tar.gz")) {
        println("srctemp tar.gz uncompressing" + fs.getUri + srcDir + "fs" + fs)
        tarln = new TarInputStream(new GZIPInputStream(fsData))
        println("tarball  umcompress successfully")
      } else if (srcTemp.endsWith(".tar.bz2")) {
        tarln = new TarInputStream(new BZip2CompressorInputStream(fsData))
      } else if (srcTemp.endsWith(".tgz")) {
        tarln = new TarInputStream(new GZIPInputStream(fsData))
      } else if (srcTemp.endsWith(".tar")) {
        tarln = new TarInputStream(fsData)
      }
    } catch {
      case e: Exception => throw e
    }
    return tarln
  }

  /**
    * 根据 压缩文件后缀 选择解压数据流方式
    *
    * @param srcDir
    * @param fs
    * @return
    */
  def zipBz2gzipFileStreamBySuffix(srcDir: String, fs: HDFSFileSystem): InputStream = {
    var is: InputStream = null
    val srcTemp: String = srcDir.toLowerCase()
    try {
      if (srcTemp.endsWith(".bz2")) {
        is = new BZip2CompressorInputStream(fs.open(new Path(srcDir)))
      } else if (srcTemp.endsWith(".gz")) {
        is = new GZIPInputStream(fs.open(new Path(srcDir)))
      } else if (srcTemp.endsWith(".zip")) {
        is = new ZipInputStream(fs.open(new Path(srcDir)))
      }
    } catch {
      case e: Exception => throw e
    }
    return is
  }

  /**
    * jar包内的资源文件目录读取,按key  读取字段，逗号分隔,没有输入字段就分key 读取
    *
    * @param filePath          jar包内的资源文件目录路径,
    * @param propertiesKeyname 属性文件的 key，为null 时，则读取全部的key，逗号分隔
    * @param   delimiter       分隔符 默认逗号
    * @return 属性文件 value 序列
    */
  def converInnerPropertiesToSeq(filePath: String, propertiesKeyname: String*)(delimiter: String = ","): ArrayBuffer[String] = {
    var seqPro: ArrayBuffer[String] = mutable.ArrayBuffer()
    val pro: Properties = new Properties()
    try {
      val pertiesPath = CompressUtils.getClass.getClassLoader.getResourceAsStream(filePath)
      pro.load(pertiesPath)
    } catch {
      case e: NullPointerException => throw e
    }
    if (propertiesKeyname.length < 1) {
      val proEmu = pro.propertyNames()
      while (proEmu.hasMoreElements) {
        val fliePrefix: String = pro.getProperty(proEmu.nextElement().toString)
        if (!fliePrefix.contains(delimiter)) {
          seqPro.+=(fliePrefix)
        } else {
          fliePrefix.split(delimiter).foreach(x => {
            seqPro.+=(x)
          })
        }

      }
    } else {
      propertiesKeyname.foreach(key => {
        val proVals = pro.getProperty(key)
        proVals.split(delimiter).foreach(vas => {
          seqPro.+=(vas)
        })
      })
    }
    return seqPro
  }

  /** *
    * jar包外 的配置文件 读取
    *
    * @param filePath          jar包外 的配置文件 读取目录路径,
    * @param propertieskeyname 属性文件的 key，为null 时，则读取全部的key，逗号分隔
    * @param   delimiter       分隔符 默认逗号
    * @return
    */
  def converOuterPropertiesToSeq(filePath: String, propertieskeyname: String*)(delimiter: String = ","): ArrayBuffer[String] = {

    val pro: Properties = new Properties()
    var seqPro: ArrayBuffer[String] = mutable.ArrayBuffer()
    try {
      val ins: InputStream = new FileInputStream(new File(filePath))
      pro.load(ins)
    } catch {
      case e: Exception => throw e
    }
    if (propertieskeyname.length < 1) {
      val proEmu = pro.propertyNames()
      while (proEmu.hasMoreElements) {
        val fliePrefix: String = pro.getProperty(proEmu.nextElement().toString)
        if (!fliePrefix.contains(delimiter)) {
          seqPro.+=(fliePrefix)
        } else {
          fliePrefix.split(delimiter).foreach(x => {
            seqPro.+=(x)
          })
        }

      }
    } else {
      propertieskeyname.foreach(key => {
        val seqvals = pro.getProperty(key)
        seqvals.split(",").foreach(v => {
          seqPro.+=(v)
        })
      })
    }

    return seqPro
  }

  /**
    * 获取文件名称 通过路径
    *
    * @param path
    * @return
    */
  def getFileName(path: String): String = {
    val tmps: Array[String] = path.split("/")
    val tmsize = tmps.length
    var fileName: String = tmps(tmsize - 1)
    fileName = fileName.substring(0, fileName.lastIndexOf("."))
    return fileName
  }

  /**
    * 判断文件前缀
    *
    * @param tarEntryName
    * @return
    */
  def boolFilePrefixContains(tarEntryName: String,propertiesPath:String): Boolean = {
    var flag:Boolean=false
    val loop: Breaks = new Breaks
    val proSeq: ArrayBuffer[String] = CompressUtils.converOuterPropertiesToSeq(propertiesPath)(",")
    loop.breakable {
      proSeq.foreach(fliePrefix => {
        val suffixContain = tarEntryName.contains(fliePrefix)
        println("bool fliePrefix || " + fliePrefix + " res  || " + suffixContain)
        if (suffixContain == true) {
          println(" suffixContain   && " + suffixContain)
          flag = true
          loop.break()
        }
      })
    }
    return flag
  }


  /**
    * tar 包类型压缩文件解压
    *
    * @param srcDir
    * @param outputDir
    * @param propertiesPath
    * @param fs
    */
  def tarFileUnCompress(srcDir: String, outputDir: String, fs: HDFSFileSystem, propertiesPath: String): Unit = {
    val tarln: TarInputStream =CompressUtils.tarfileStreamBySuffix(srcDir,fs)
    println("tarFileUnCompress uncompressing" + fs.getUri + srcDir + "fs" + fs)
    println("begin uncompress log data tar gz")
    var entry: TarEntry = null
    while ( {
        entry = tarln.getNextEntry; entry != null
    }) {
      try {
        if (entry.isDirectory) {
          println("tar entry.getName  " + entry.getName + "   || outputDir:  " + outputDir)
          val dirPath=outputDir + "/" + entry.getName
          println("dirPath"+dirPath)
          val ps: Path = new Path(dirPath)
          println("hdfs path name ||"+ps.getName)
         val mkflag=fs.mkdirs(ps)
          println("hdfs create dir suceess"+mkflag)
        } else {
          println("tar OutputStream entry.getName  " + entry.getName + "  || outputDir:  " + outputDir)
          val tarEntryName: String = entry.getName
          val flag: Boolean =boolFilePrefixContains(tarEntryName,propertiesPath)

          println("flag || " + flag)
          if (flag == true) {
            val pss: Path = new Path(outputDir + "/" + entry.getName)
            val out: FSDataOutputStream = fs.create(pss)
            try {
              var length = 0
              val arrayBuffer: Array[Byte] = new Array[Byte](8192)
              val loop: Breaks = new Breaks
              //              import loop.{break,breakable}
              //              breakable { }
              while ( {
                try {
                  length = tarln.read(arrayBuffer);
                  length != -1
                } catch {
                  case e: IndexOutOfBoundsException => {
                    println("read length" + length)
                    false
                  }
                }

              }) {
                val ale = arrayBuffer.length
                //println(" arrayBuffer "+ale+" length "+ length)
                if (arrayBuffer != null && ale > 0 && length >= 0 && ale >= length) {
                  out.write(arrayBuffer, 0, length)
                } else {
                  loop.break()
                }
              }

            } catch {
              case e: Exception => e.printStackTrace()
            } finally {
              IOUtils.closeStream(out)

            }
          }
        }
      } catch {
        case e: Exception => e.printStackTrace()
      } finally {

      }
    }
    IOUtils.closeStream(tarln)
    fs.close()

  }

  /**
    * 解压  zip Bz2 gzip 类压缩文件
    *
    * @param srcDir
    * @param outputDir
    * @param fs
    */
  def zipBz2gzipFileUnCompress(srcDir: String, outputDir: String, fs: HDFSFileSystem): Unit = {
    val srcTemp: String = srcDir.toLowerCase()
    val is: InputStream = CompressUtils.zipBz2gzipFileStreamBySuffix(srcDir, fs)
    if (srcTemp.endsWith(".zip")) {
      while (is.asInstanceOf[ZipInputStream].getNextEntry != null) {
        try {
          val entry: ZipEntry = is.asInstanceOf[ZipInputStream].getNextEntry
          if (entry.isDirectory) {
            println("zip stream entry.getName " + entry.getName + "outputDir" + outputDir)
            val ps: Path = new Path(outputDir + "/" + entry.getName)
            fs.mkdirs(ps)
          } else {
            val out: FSDataOutputStream = fs.create(new Path(outputDir + "/" + entry.getName))
            try {
              var length: Int = 0
              val buff: Array[Byte] = new Array[Byte](2048)
              while ( {
                length = is.read(buff);
                length != -1
              }) {
                val ale = buff.length
                if (buff != null && ale > 0 && length >= 0 && ale >= length) {
                  out.write(buff, 0, length)
                }
              }
            } catch {
              case e: Exception => e.printStackTrace()
            } finally {
              IOUtils.closeStream(out)
            }
          }
        } catch {
          case e: Exception => e.printStackTrace()
        } finally {
          IOUtils.closeStream(is)
          fs.close()
        }
      }
    } else {
      val fileName: String = CompressUtils.getFileName(srcDir)
      val pass: Path = new Path(outputDir + "/" + fileName)
      val out: FSDataOutputStream = fs.create(pass)
      try {
        var length: Int = 0
        val arrayBuffer: Array[Byte] = new Array[Byte](8192)
        while ( {
          length = is.read(arrayBuffer);
          length != -1
        }) {
          val ale = arrayBuffer.length
          if (arrayBuffer != null && ale > 0 && length >= 0 && ale >= length) {
            out.write(arrayBuffer, 0, length)
          }
        }
      } catch {
        case e: Exception => e.printStackTrace()
      } finally {

        IOUtils.closeStream(out)

      }
    }

    IOUtils.closeStream(is)
    fs.close()
  }


}
