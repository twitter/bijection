package com.twitter.bijection.macros.common

import org.scalatest.{ Matchers, WordSpec }
import org.scalatest.exceptions.TestFailedException
import com.twitter.bijection.macros.common.{ _ => _ }

/**
 * This test is intended to ensure that the macros do not require any imported code in scope. This is why all
 * references are via absolute paths.
 */
class MacroDepHygiene extends WordSpec with Matchers {
  import com.twitter.bijection.macros.common.MacroImplicits.isCaseClass

  case class A(x: Int, y: String)
  case class B(x: A, y: String, z: A)
  class C

  def isMg(t: AnyRef) {
    t shouldBe a[com.twitter.bijection.macros.common.MacroGenerated]
    canExternalize(t)
  }

  def canExternalize(t: AnyRef) { com.twitter.chill.Externalizer(t).javaWorks shouldBe true }

  "IsCaseClass macro" should {
    val dummy = new com.twitter.bijection.macros.common.IsCaseClass[Nothing] {}
    def isCaseClassAvailable[T](implicit proof: com.twitter.bijection.macros.common.IsCaseClass[T] = dummy.asInstanceOf[com.twitter.bijection.macros.common.IsCaseClass[T]]) { isMg(proof) }

    "work fine without any imports" in {
      isCaseClassAvailable[A]
      isCaseClassAvailable[B]
    }

    "fail if not available" in {
      a[TestFailedException] should be thrownBy isCaseClassAvailable[C]
    }
  }
}
