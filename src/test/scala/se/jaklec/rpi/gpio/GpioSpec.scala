package se.jaklec.rpi.gpio

import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll, ShouldMatchers, WordSpecLike}
import java.io.IOException
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import java.nio.charset.StandardCharsets
import scala.collection.JavaConverters._

trait MockGpioBase extends GpioBase {

  val basePath = "src/test/resources/test"
}

class GpioSpec extends WordSpecLike with ShouldMatchers with BeforeAndAfterEach {

  import java.nio.file.Files._

  val dir = new MockGpioBase {} basePath
  val path = Paths get dir
  val exportPath = Paths get s"$path/export"
  val unexportPath = Paths get s"$path/unexport"
  val gpio10Path = Paths get s"$path/gpio10"
  val gpio10DirectionPath: Path = Paths get s"$gpio10Path/direction"

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
    val gpio = new Gpio with MockGpioBase

    "create file access to a port" in {
      gpio export(Gpid10, In)
      val actual = readAllLines(exportPath, StandardCharsets UTF_8).asScala.mkString

      actual should equal("10")
    }

    "remove file access to a port if it exists" in {
      gpio unexport Gpid10
      val actual = readAllLines(unexportPath, StandardCharsets.UTF_8).asScala.mkString

      actual should equal("10")
    }

    "not remove file access to a port if it doesn't exist" in {
      gpio unexport Gpid17
      val actual = readAllLines(unexportPath, StandardCharsets.UTF_8).asScala

      actual.isEmpty should be(true)
    }

    "set port direction to IN" in {
      gpio export(Gpid10, In)
      val direction = readAllLines(gpio10DirectionPath, StandardCharsets.UTF_8).asScala.mkString

      direction should equal("in")
    }

    "set port direction to OUT" in {
      gpio export(Gpid10, Out)
      val direction = readAllLines(gpio10DirectionPath, StandardCharsets.UTF_8).asScala.mkString

      direction should equal("out")
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
