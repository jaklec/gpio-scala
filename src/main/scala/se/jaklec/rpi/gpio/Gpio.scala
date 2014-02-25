package se.jaklec.rpi.gpio

import java.nio.file.{StandardOpenOption, Files, Path, Paths}
import java.nio.charset.StandardCharsets
import java.io.File

sealed abstract class Gpid(val number: String)
case object Gpid0 extends Gpid(0.toString)
case object Gpid1 extends Gpid(1.toString)
case object Gpid4 extends Gpid(4.toString)
case object Gpid9 extends Gpid(9.toString)
case object Gpid10 extends Gpid(10.toString)
case object Gpid17 extends Gpid(17.toString)
case object Gpid21 extends Gpid(21.toString)
case object Gpid22 extends Gpid(22.toString)

sealed abstract class Io(val direction: String)
case object In extends Io("in")
case object Out extends Io("out")

trait GpioBase {

  val basePath: String
}

trait DefaultGpioBase extends GpioBase {

  val basePath = "/sys/class/gpio"
}

class Gpio { this: GpioBase =>

  def export(port: Gpid, io: Io): Unit = {
    write(port.number, Paths get s"$basePath/export")
    write(io.direction, Paths get s"$basePath/gpio${port.number}/direction")
  }

  def unexport(port: Gpid): Unit = {
    val portAccessFile = new File(s"$basePath/gpio${port.number}")
    if (portAccessFile.exists())
      write(port.number, Paths get s"$basePath/unexport")
  }

  def write(port: Gpid, value: String): Unit = {
    write(value, Paths get s"$basePath/gpio${port.number}/value")
  }

  private def write(value: String, path: Path): Unit = {
    Files write(path, value.getBytes(StandardCharsets.UTF_8), StandardOpenOption CREATE)
  }
}