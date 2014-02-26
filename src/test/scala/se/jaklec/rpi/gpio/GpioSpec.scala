package se.jaklec.rpi.gpio

import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll, ShouldMatchers, WordSpecLike}
import java.io.IOException
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import java.nio.charset.StandardCharsets
import scala.collection.JavaConverters._


class GpioSpec extends WordSpecLike with ShouldMatchers with BeforeAndAfterEach {

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

      actual should equal("10")
    }

    "remove file access to a port if it exists" in {
      TestPin10 close
      val actual = readAllLines(unexportPath, StandardCharsets.UTF_8).asScala.mkString

      actual should equal("10")
    }

    "not remove file access to a port if it doesn't exist" in {
      object TestPin17 extends Gpio with MockGpioBase {
        override val pin: String = 17.toString
      }

      TestPin17 close
      val actual = readAllLines(unexportPath, StandardCharsets.UTF_8).asScala

      actual.isEmpty should be(true)
    }

    "set port direction to IN" in {
      TestPin10 open In
      val direction = readAllLines(gpio10DirectionPath, StandardCharsets.UTF_8).asScala.mkString

      direction should equal("in")
    }

    "set port direction to OUT" in {
      TestPin10 open Out
      val direction = readAllLines(gpio10DirectionPath, StandardCharsets.UTF_8).asScala.mkString

      direction should equal("out")
    }

    "write value to pin" in {
      TestPin10 write Analog("foo")
      val value = readAllLines(gpio10ValuePath, StandardCharsets.UTF_8).asScala.mkString

      value should equal("foo")
    }

    "read analog value from pin" in {
      TestPin10 write Analog("foo")
      val value = TestPin10 read

      value should equal("foo")
    }

    "write digital value to pin" in {
      TestPin10 write On
      val on = TestPin10 read

      on should equal("1")

      TestPin10 write Off
      val off = TestPin10 read

      off should equal("0")
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
