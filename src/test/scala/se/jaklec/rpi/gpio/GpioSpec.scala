package se.jaklec.rpi.gpio

import org.scalatest._
import java.io.IOException
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import java.nio.charset.StandardCharsets
import scala.collection.JavaConverters._
import scala.language.postfixOps
import java.security.DigestInputStream
import scala.util.{Success, Failure}

class GpioSpec extends WordSpecLike with MustMatchers with BeforeAndAfterEach {

  import java.nio.file.Files._

  trait MockGpioBase extends GpioBase {
  
    val basePath = "src/test/resources/test"
  }

  object TestPin10 extends Gpio with MockGpioBase {
    override val pin: String = 10.toString
  }
  
  val dir = new MockGpioBase {} basePath
  val path = Paths get dir
  val exportPath = Paths get s"$path/export"
  val unexportPath = Paths get s"$path/unexport"
  val gpio10Path = Paths get s"$path/gpio10"
  val gpio10DirectionPath: Path = Paths get s"$gpio10Path/direction"
  val gpio10ValuePath: Path = Paths get s"$gpio10Path/value"

  override def beforeEach() {
    createDirectories(path)
    createDirectories(gpio10Path)
    createFile(exportPath)
    createFile(unexportPath)
    createFile(gpio10DirectionPath)
  }

  override def afterEach() {
    deleteAllTestResources
  }

  "A Gpio" should {

    "create file access to a port" in {
      TestPin10 open In
      val actual = readAllLines(exportPath, StandardCharsets UTF_8).asScala.mkString

      actual must equal("10")
    }

    "remove file access to a port if it exists" in {
      TestPin10 close
      val actual = readAllLines(unexportPath, StandardCharsets.UTF_8).asScala.mkString

      actual must equal("10")
    }

    "not remove file access to a port if it doesn't exist" in {
      object TestPin17 extends Gpio with MockGpioBase {
        override val pin: String = 17.toString
      }

      TestPin17 close
      val actual = readAllLines(unexportPath, StandardCharsets.UTF_8).asScala

      actual.isEmpty must be(true)
    }

    "set port direction to IN" in {
      TestPin10 open In
      val direction = readAllLines(gpio10DirectionPath, StandardCharsets.UTF_8).asScala.mkString

      direction must equal("in")
    }

    "set port direction to OUT" in {
      TestPin10 open Out
      val direction = readAllLines(gpio10DirectionPath, StandardCharsets.UTF_8).asScala.mkString

      direction must equal("out")
    }

    "write analog value to pin" in {
      TestPin10 write Analog("foo")
      val value = readAllLines(gpio10ValuePath, StandardCharsets.UTF_8).asScala.mkString

      value must equal("foo")
    }

    "read analog value from pin" in {
      TestPin10 write Analog("foo")
      val result = TestPin10 read

      result.value must equal("foo")
    }

    "write digital value to pin" in {
      TestPin10 write On
      val on = readAllLines(gpio10ValuePath, StandardCharsets.UTF_8).asScala.mkString

      on must equal("1")

      TestPin10 write Off
      val off = readAllLines(gpio10ValuePath, StandardCharsets.UTF_8).asScala.mkString

      off must equal("0")
    }

    "read digital value from pin" in {
      TestPin10 write On
      val r1 = TestPin10 read

      r1 must equal(On)

      TestPin10 write Off
      val r2 = TestPin10 read

      r2 must equal(Off)
    }

    "explicitly read analog value from pin" in {
      TestPin10 write Analog("1")
      val result = TestPin10 readAnalog

      result match {
        case Analog(_) => result.value must equal("1")
        case _ => fail("Not parsed as an analog value")
      }
    }

    "explicitly read digital value from pin" in {
      TestPin10 write Off
      val result = TestPin10 readDigital

      result match {
        case d: Success[Digital] => result.get must equal(Off)
        case _ => fail("Not parsed as a digital value")
      }
    }

    "fail if trying to read analog value as digital" in {
      TestPin10 write Analog("1.23")
      TestPin10 readDigital match {
        case Success(d) => fail("Not returning a failure")
        case Failure(e) => assertException(e)
      }

      def assertException(e: Throwable): Unit = e match {
        case e: ReadException =>
          e.getMessage must equal("Not a digital value")
        case _ => fail("Unexpected exception")
      }
    }
  }

  def deleteAllTestResources {
    import java.nio.file.FileVisitResult._
    walkFileTree(path, new SimpleFileVisitor[Path]() {

      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        delete(file)
        CONTINUE
      }

      override def postVisitDirectory(dir: Path, e: IOException): FileVisitResult = {
        delete(dir)
        CONTINUE
      }
    })
  }
}
