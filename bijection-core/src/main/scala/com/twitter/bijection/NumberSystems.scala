/**
 * Convert between popular number systems
 * Author: Krishnan Raman, kraman@twitter.com
 */

package com.twitter.bijection

object NumberSystems {
  case class BinaryString(get: String) extends AnyVal
  case class HexString(get: String) extends AnyVal
  case class OctalString(get: String) extends AnyVal
  case class ArbitBaseString(get: String, base: Int)

  implicit val binary: Bijection[Int, BinaryString] = new AbstractBijection[Int, BinaryString] {
    def apply(num: Int) = BinaryString(Integer.toString(num, 2))
    override def invert(s: BinaryString) = Integer.parseInt(s.get, 2)
  }
  implicit val hexadecimal: Bijection[Int, HexString] = new AbstractBijection[Int, HexString] {
    def apply(num: Int) = HexString(Integer.toString(num, 16))
    override def invert(s: HexString) = Integer.parseInt(s.get, 16)
  }
  implicit val octal: Bijection[Int, OctalString] = new AbstractBijection[Int, OctalString] {
    def apply(num: Int) = OctalString(Integer.toString(num, 8))
    override def invert(s: OctalString) = Integer.parseInt(s.get, 8)
  }
  // Can't be implicit due to the base parameter:
  def arbitbase(base: Int): Bijection[Int, ArbitBaseString] = new AbstractBijection[Int, ArbitBaseString] {
    def apply(num: Int) = ArbitBaseString(Integer.toString(num, base), base)
    override def invert(s: ArbitBaseString) = Integer.parseInt(s.get, s.base)
  }
}
