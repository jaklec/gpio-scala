package se.jaklec.rpi.gpio

import java.nio.file.{StandardOpenOption, Files, Path, Paths}
import java.nio.charset.StandardCharsets
import java.io.File
import scala.collection.JavaConverters._
import scala.language.postfixOps
import scala.util.Try

trait GpioBase {

  val basePath: String
}

trait DefaultGpio extends GpioBase {

  val basePath = "/sys/class/gpio"
}

trait Gpio { this: GpioBase =>

  val pin: String

  def open(io: Io): Unit = {
    write(Analog(pin), Paths get s"$basePath/export")
    write(Analog(io.direction), Paths get s"$basePath/gpio$pin/direction")
  }

  def close: Unit = {
    val portAccessFile = new File(s"$basePath/gpio$pin")
    if (portAccessFile.exists())
      write(Analog(pin), Paths get s"$basePath/unexport")
  }

  def write(value: Value): Unit = {
    write(value, Paths get s"$basePath/gpio$pin/value")
  }

  def read: Value = {
    val asAnalog: PartialFunction[String, Analog] = { case v @ _ => Analog(v) }
    val readValue = asDigital orElse asAnalog
    readValue(readFile)
  }

  def readAnalog: Analog = Analog(readFile)

  def readDigital: Try[Digital] = Try {
    val failWithReadException: PartialFunction[String, Digital] = { case _ => throw new ReadException("Not a digital value") }
    val readAsDigitalOrFail = asDigital orElse failWithReadException
    readAsDigitalOrFail(readFile)
  }

  private val asDigital: PartialFunction[String, Digital] = {
    case "0" => Off
    case "1" => On
  }

  private def readFile: String = Files.readAllLines(Paths get s"$basePath/gpio$pin/value", StandardCharsets.UTF_8).asScala.mkString

  private def write(value: Value, path: Path): Unit = {
    Files write(path, value.value.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE)
  }
}

class ReadException(msg: String) extends RuntimeException(msg)