/**
Convert between popular number systems
Author: Krishnan Raman, kraman@twitter.com
*/

package com.twitter.bijection

object NumberSystems {
  case class BinaryString(get:String)
  case class HexString(get:String)
  case class OctalString(get:String)
  case class ArbitBaseString(get:String,base:Int)

  implicit val binary: Bijection[Int, BinaryString] = new Bijection[Int, BinaryString] {
      def apply(num: Int) = BinaryString(Integer.toString(num,2))
      override def invert(s: BinaryString) = Integer.parseInt(s.get,2)
  }
  implicit val hexadecimal: Bijection[Int, HexString] = new Bijection[Int, HexString] {
      def apply(num: Int) = HexString(Integer.toString(num,16))
      override def invert(s: HexString)  = Integer.parseInt(s.get,16)
  }
  implicit val octal: Bijection[Int, OctalString] = new Bijection[Int, OctalString] {
      def apply(num: Int) = OctalString(Integer.toString(num,8))
      override def invert(s: OctalString) = Integer.parseInt(s.get,8)
  }
  implicit def arbitbase(base:Int): Bijection[Int, ArbitBaseString] = new Bijection[Int, ArbitBaseString] {
      def apply(num: Int) = ArbitBaseString(Integer.toString(num,base),base)
      override def invert(s: ArbitBaseString) = Integer.parseInt(s.get,s.base)
  }
}
