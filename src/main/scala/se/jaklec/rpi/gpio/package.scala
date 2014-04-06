package se.jaklec.rpi

package object gpio {

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

  sealed abstract class Value(val value: String)
  case class Analog(override val value: String) extends Value(value)
  abstract class Digital(val digit: Int) extends Value(digit.toString)
  case object On extends Digital(1)
  case object Off extends Digital(0)
}
