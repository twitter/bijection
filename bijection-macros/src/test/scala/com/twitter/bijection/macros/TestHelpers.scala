package com.twitter.bijection.macros

import org.scalatest.Matchers
import com.twitter.chill.Externalizer

object MacroCaseClasses extends java.io.Serializable {
  type Atup = (Int, String)
  type Btup = (Atup, Atup, String)
  type Ctup = (Atup, Btup, Atup, Btup, Btup)

  // These are s single level unpacking into tuples
  // of the case classes below
  type Atupnr = (Int, String)
  type Btupnr = (SampleClassA, SampleClassA, String)
  type Ctupnr = (SampleClassA, SampleClassB, SampleClassA, SampleClassB, SampleClassB)

  case class SampleClassA(x: Int, y: String)
  case class SampleClassB(a1: SampleClassA, a2: SampleClassA, y: String)
  case class SampleClassC(a: SampleClassA, b: SampleClassB, c: SampleClassA, d: SampleClassB, e: SampleClassB)
  class SampleClassD // Non-case class
}

trait MacroTestHelper extends Matchers {
  def canExternalize(t: AnyRef) { Externalizer(t).javaWorks shouldBe true }
}
