package se.jaklec.rpi.gpio

import java.nio.file.{StandardOpenOption, Files, Path, Paths}
import java.nio.charset.StandardCharsets
import java.io.File
import scala.collection.JavaConverters._

sealed abstract class Pid(val number: String)
case object Pid0 extends Pid(0.toString)
case object Pid1 extends Pid(1.toString)
case object Pid4 extends Pid(4.toString)
case object Pid9 extends Pid(9.toString)
case object Pid10 extends Pid(10.toString)
case object Pid17 extends Pid(17.toString)
case object Pid21 extends Pid(21.toString)
case object Pid22 extends Pid(22.toString)

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

class Gpio { this: GpioBase =>

  def open(port: Pid, io: Io): Unit = {
    write(Analog(port.number), Paths get s"$basePath/export")
    write(Analog(io.direction), Paths get s"$basePath/gpio${port.number}/direction")
  }

  def close(port: Pid): Unit = {
    val portAccessFile = new File(s"$basePath/gpio${port.number}")
    if (portAccessFile.exists())
      write(Analog(port.number), Paths get s"$basePath/unexport")
  }

  def write(port: Pid, value: Value): Unit = {
    write(value, Paths get s"$basePath/gpio${port.number}/value")
  }

  def read(port: Pid): String = {
    Files.readAllLines(Paths get s"$basePath/gpio${port.number}/value", StandardCharsets.UTF_8).asScala.mkString
  }

  private def write(value: Value, path: Path): Unit = {
    Files write(path, value.v.getBytes(StandardCharsets.UTF_8), StandardOpenOption CREATE)
  }
}