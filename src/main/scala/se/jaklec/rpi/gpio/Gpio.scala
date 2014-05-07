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

object Gpio {
  abstract class Pin(val p: Int) extends Gpio with DefaultGpio {
    override val pin: String = p.toString
  }
  case object Pin0 extends Pin(0)
  case object Pin1 extends Pin(1)
  case object Pin4 extends Pin(4)
  case object Pin9 extends Pin(9)
  case object Pin10 extends Pin(10)
  case object Pin17 extends Pin(17)
  case object Pin21 extends Pin(21)
  case object Pin22 extends Pin(22)

  abstract class Io(val direction: String)
  case object In extends Io("in")
  case object Out extends Io("out")

  abstract class Value(val value: String)
  case class Analog(override val value: String) extends Value(value)
  abstract class Digital(val digit: Int) extends Value(digit.toString)
  case object On extends Digital(1)
  case object Off extends Digital(0)
}

trait Gpio {
  this: GpioBase =>
  import se.jaklec.rpi.gpio.Gpio._

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