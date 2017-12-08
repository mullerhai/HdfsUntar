import java.io.{InputStream, OutputStream}
import java.util.zip.{GZIPInputStream, ZipEntry, ZipInputStream}

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FSDataOutputStream, Path, FileSystem => HDFSFileSystem}
import org.apache.hadoop.io.IOUtils
import org.apache.hadoop.io.compress.{CompressionCodec, CompressionCodecFactory}

import scala.collection.mutable.ArrayBuffer
import scala.util.control._
//import org.apache.tools.tar.{TarEntry, TarInputStream}
import org.kamranzafar.jtar.{TarEntry, TarInputStream}


/**
  * Created by linkedmemuller on 06/12/2017.
  */
class HdfsTars {

}

object HdfsTars {

  def uncompressForHadoop(srcDir: String, outputDir: String, fs: HDFSFileSystem): Unit = {
    var in: InputStream = null
    var out: OutputStream = null
    println("uncompressForHadoop")
    try {
      val p1: Path = new Path(srcDir)
      val factory: CompressionCodecFactory = new CompressionCodecFactory(fs.getConf)
      val codec: CompressionCodec = factory.getCodec(p1)
      if (codec == null) {
        return
      }
      println(" un hadoop codec")
      var targetFileName = CompressionCodecFactory.removeSuffix(srcDir, codec.getDefaultExtension)
      targetFileName = targetFileName.substring(targetFileName.lastIndexOf("/") + 1)
      val p2: Path = new Path(outputDir + targetFileName)
      in = codec.createInputStream(fs.open(p1))
      out = fs.create(p2)
      IOUtils.copyBytes(in, out, fs.getConf)
    } finally {
      IOUtils.closeStream(out)
      IOUtils.closeStream(in)
      fs.close()
    }
  }

  def main(args: Array[String]): Unit = {

    if (args.length < 2) {

      sys.exit(0)
      return
    }
    println(" begin untar ing")
    val tarFile = args(0)
    val hdfsDest = args(1)
    //    val hdfsURI = args(2)
    //    val hadoopUser = args(3)
    val FS = "hdfs://192.168.255.161:9000"
    //  System.setProperty("HADOOP_USER_NAME",hadoopUser)
    // val path=new Path(hdfsURI)
    val conf = new Configuration()
    conf.set("fs.defaultFS", FS)
    val fs = HDFSFileSystem.get(conf)
    println("tarfile " + tarFile + " desrt " + hdfsDest + " fs " + fs)
    umcompresstar(tarFile, hdfsDest, fs)
    //uncompressForHadoop(tarFile,hdfsDest,fs)
    //    val inStream = unTars(tarFile)
    //    val outStream = setUpHDFSDest(hadoopUser, hdfsURI, hdfsDest)
    //    readAndWrite(inStream, outStream)
  }

  def umcompresstar(srcDir: String, outputDir: String, fs: HDFSFileSystem): Unit = {

    var tarln: TarInputStream = null
    var is: InputStream = null
    println("umcompresstar method execing")
    try {
      val srcTemp: String = srcDir.toLowerCase()
      if (srcTemp.endsWith(".tar.gz")) {
        println("srctemp tar.gz uncompressing")
        tarln = new TarInputStream(new GZIPInputStream(fs.open(new Path(srcDir))))
        println("tarball  umcompress successfully")
      } else if (srcTemp.endsWith(".tar.bz2")) {
        tarln = new TarInputStream(new BZip2CompressorInputStream(fs.open(new Path(srcDir))))
      } else if (srcTemp.endsWith(".tgz")) {
        tarln = new TarInputStream(new GZIPInputStream(fs.open(new Path(srcDir))))
      } else if (srcTemp.endsWith(".tar")) {
        tarln = new TarInputStream(fs.open(new Path(srcDir)))
      } else if (srcTemp.endsWith(".bz2")) {
        is = new BZip2CompressorInputStream(fs.open(new Path(srcDir)))
      } else if (srcTemp.endsWith(".gz")) {
        is = new GZIPInputStream(fs.open(new Path(srcDir)))
      } else if (srcTemp.endsWith(".zip")) {
        is = new ZipInputStream(fs.open(new Path(srcDir)))
      }
      if (srcTemp.endsWith(".tar.gz") || srcTemp.endsWith(".tar.bz2") || srcTemp.endsWith(".tgz") || srcTemp.endsWith(".tar")) {
        println("begin uncompress log data tar gz")
        var entry: TarEntry = null
        while ( {
          entry = tarln.getNextEntry; entry != null
        }) {
          try {

            if (entry.isDirectory) {
              println("tar entry.getName  " + entry.getName + "   || outputDir:  " + outputDir)
              val ps: Path = new Path(outputDir + "/" + entry.getName)
              fs.mkdirs(ps)
            } else {
              println("tar OutputStream entry.getName  " + entry.getName + "  || outputDir:  " + outputDir)
              val in: String = entry.getName
              if (in.contains("biz.log") || in.contains("info.log") || in.contains("ad_status") || in.contains("ad_behavior")) {
                val pss: Path = new Path(outputDir + "/" + entry.getName)
                val out: FSDataOutputStream = fs.create(pss)
                try {
                  var length = 0
                  val arrayBuffer: Array[Byte] = new Array[Byte](8192)
                  val loop: Breaks = new Breaks
                  //              import loop.{break,breakable}
                  //              breakable { }
                  while ( {
                    length = tarln.read(arrayBuffer);
                    length != -1
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
          }

        }
      } else if (srcTemp.endsWith(".zip")) {

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
                  length = is.read(buff); length != -1
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
          }


        }
      } else {
        val fileName: String = getFileName(srcDir)
        val pass: Path = new Path(outputDir + "/" + fileName)
        val out: OutputStream = fs.create(pass)
        try {
          var length: Int = 0
          val arrayBuffer: Array[Byte] = new Array[Byte](8192)
          while ( {
            length = is.read(arrayBuffer); length != -1
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

    } finally {
      IOUtils.closeStream(tarln)
      IOUtils.closeStream(is)
      fs.close()
    }
  }

  def getFileName(path: String): String = {
    val tmps: Array[String] = path.split("/")
    val tmsize = tmps.length
    var fileName: String = tmps(tmsize - 1)
    fileName = fileName.substring(0, fileName.lastIndexOf("."))
    return fileName
  }


  //class  readHandler(dataQueue:ConcurrentLinkedQueue[Array[Byte]],tis:TarInputStream)extends Runnable{
  //  val BUFFSIZE:Int=100*1024*1024
  //  override def run(): Unit ={
  //    var entry:TarEntry=tis.getNextEntry
  //    if(entry!=null){
  //      var count :Int=0
  //      val data:Array[Byte]=new Array[Byte](BUFFSIZE)
  //      count=tis.read(data,0,BUFFSIZE)
  //      while (count != -1){
  //        dataQueue.add(data.slice(0,count))
  //        count=tis.read(data,0,BUFFSIZE)
  //      }
  //    }
  //    tis.close()
  //
  //  }
  //}
  //class writeHandler(dataQueue:ConcurrentLinkedQueue[Array[Byte]],
  //                   dest:FSDataOutputStream) extends Runnable{
  //  override def run(): Unit = {
  //
  //    while (true){
  //      val data=dataQueue.poll()
  //      if(data !=null){
  //        dest.write(data)
  //      }
  //      if(Thread.interrupted()){
  //        dest.flush()
  //        dest.close()
  //        return
  //      }
  //    }
  //  }
  //}

  //val BUFFSIZE:Int=8*1024*1024
  //val dataQueue=new ConcurrentLinkedQueue[Array[Byte]]()
  //
  //def unTars(tarFile:String):TarInputStream={
  //  println("unTars methods ")
  //  val tis:TarInputStream=
  //    if(tarFile=="-"){
  //      new TarInputStream(new BufferedInputStream(System.in))
  //    }else if (tarFile.endsWith(".gz")){
  //      new TarInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(tarFile))))
  //    }else {
  //      new TarInputStream(new BufferedInputStream(new FileInputStream(tarFile)))
  //    }
  //  return tis
  //}

  //def setUpHDFSDest(user:String,uri:String,filePath:String):FSDataOutputStream={
  //  System.setProperty("HADOOP_USER_NAME",user)
  //  val path=new Path(filePath)
  //  val conf=new Configuration()
  //  conf.set("fs.defaultFS",uri)
  //  val fs =HDFSFileSystem.get(conf)
  //  println("setUpHDFSDest")
  //  return fs.create(path)
  //}
  //def readAndWrite(tis:TarInputStream,dest:FSDataOutputStream):Unit={
  //  var entry:TarEntry=tis.getNextEntry
  //  println(entry.toString+" tar file entry ")
  //  if(entry !=null){
  //    var count:Int=0
  //    val data:Array[Byte]=new Array[Byte](BUFFSIZE)
  //    count=tis.read(data,0,BUFFSIZE)
  //    while (count != -1){
  //      dest.write(data,0,count)
  //      count=tis.read(data ,0,BUFFSIZE)
  //    }
  //    dest.flush()
  //    dest.close()
  //  }
  //  tis.close()
  //}
  //def keepWrite(dataQueue:ConcurrentLinkedQueue[Array[Byte]],dest:FSDataOutputStream,rWorker:Thread)={
  //  while (rWorker.getState !=Thread.State.TERMINATED){
  //    val data =dataQueue.poll()
  //    if(data !=null){
  //      dest.write(data)
  //    }
  //  }
  //}
}


