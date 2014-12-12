package com.twitter.bijection.macros

import org.scalatest.{ Matchers, WordSpec }

import com.twitter.bijection._
import com.twitter.bijection.macros._
import com.twitter.chill.Externalizer

class MacroUnitTests extends WordSpec with Matchers with MacroTestHelper {
  import MacroImplicits._
  import MacroCaseClasses._

  def doesJavaWork[A, B](implicit bij: Bijection[A, B]) {
    bij shouldBe a[MacroGenerated]
    canExternalize(bij)
  }
  def doesJavaWork[A](implicit bij: Injection[A, Map[String, Any]]) {
    bij shouldBe a[MacroGenerated]
    canExternalize(bij)
  }

  "Recursively applied" when {

    "MacroGenerated Bijection to tuple" should {
      "be serializable for case class A" in { doesJavaWork[SampleClassA, Atup] }
      "be serializable for case class B" in { doesJavaWork[SampleClassB, Btup] }
      "be serializable for case class C" in { doesJavaWork[SampleClassC, Ctup] }
    }

    "MacroGenerated Injection to map" should {
      "be serializable for case class A" in { doesJavaWork[SampleClassA] }
      "be serializable for case class B" in { doesJavaWork[SampleClassB] }
      "be serializable for case class C" in { doesJavaWork[SampleClassC] }
    }
  }

  "Non-recursively applied" when {
    "MacroGenerated Bijection to tuple" should {
      "be serializable for case class A" in { doesJavaWork[SampleClassA, Atupnr] }
      "be serializable for case class B" in { doesJavaWork[SampleClassB, Btupnr] }
      "be serializable for case class C" in { doesJavaWork[SampleClassC, Ctupnr] }
    }

    "MacroGenerated Injection to map" should {
      "be serializable for case class A" in { doesJavaWork[SampleClassA] }
      "be serializable for case class B" in { doesJavaWork[SampleClassB] }
      "be serializable for case class C" in { doesJavaWork[SampleClassC] }
    }
  }
}
