package se.jaklec.rpi.gpio

import java.nio.file.{StandardOpenOption, Files, Path, Paths}
import java.nio.charset.StandardCharsets
import java.io.File
import scala.collection.JavaConverters._

sealed abstract class Pin(val p: Int) extends Gpio with DefaultGpio {
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

sealed abstract class Io(val direction: String)
case object In extends Io("in")
case object Out extends Io("out")

sealed abstract class Value(val v: String)
case class Analog(override val v: String) extends Value(v)
case object On extends Value("1")
case object Off extends Value("0")

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

  def read: String = {
    Files.readAllLines(Paths get s"$basePath/gpio$pin/value", StandardCharsets.UTF_8).asScala.mkString
  }

  private def write(value: Value, path: Path): Unit = {
    Files write(path, value.v.getBytes(StandardCharsets.UTF_8), StandardOpenOption CREATE)
  }
}
