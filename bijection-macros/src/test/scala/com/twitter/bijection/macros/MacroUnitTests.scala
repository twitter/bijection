package com.twitter.bijection.macros

import org.scalatest.{ Matchers, WordSpec }

import com.twitter.bijection._
import com.twitter.bijection.macros._
import com.twitter.bijection.macros.common.{ MacroImplicits => CommonMacroImplicits, _ }
import com.twitter.chill.Externalizer

object MacroCaseClasses extends java.io.Serializable {
  type Atup = (Int, String)
  type Btup = (Atup, Atup, String)
  type Ctup = (Atup, Btup, Atup, Btup, Btup)

  type Atupnr = (Int, String)
  type Btupnr = (A, A, String)
  type Ctupnr = (A, B, A, B, B)

  case class A(x: Int, y: String)
  case class B(a1: A, a2: A, y: String)
  case class C(a: A, b: B, c: A, d: B, e: B)
}

trait MacroTestHelper extends Matchers {
  def isMg[T](t: T): T = {
    t shouldBe a[MacroGenerated]
    t
  }

  def canExternalize(t: AnyRef) { Externalizer(t).javaWorks shouldBe true }
}

class MacroUnitTests extends WordSpec with Matchers with MacroTestHelper {
  import CommonMacroImplicits._
  import MacroCaseClasses._

  def doesJavaWork[A, B](implicit bij: Bijection[A, B]) { canExternalize(isMg(bij)) }
  def doesJavaWork[A](implicit bij: Injection[A, Map[String, Any]]) { canExternalize(isMg(bij)) }

  "Recursively applied" when {
    import MacroImplicits._

    "MacroGenerated Bijection to tuple" should {
      "be serializable for case class A" in { doesJavaWork[A, Atup] }
      "be serializable for case class B" in { doesJavaWork[B, Btup] }
      "be serializable for case class C" in { doesJavaWork[C, Ctup] }
    }

    "MacroGenerated Injection to map" should {
      "be serializable for case class A" in { doesJavaWork[A] }
      "be serializable for case class B" in { doesJavaWork[B] }
      "be serializable for case class C" in { doesJavaWork[C] }
    }
  }

  "Non-recursively applied" when {
    import LowerPriorityMacroImplicits._

    "MacroGenerated Bijection to tuple" should {
      "be serializable for case class A" in { doesJavaWork[A, Atupnr] }
      "be serializable for case class B" in { doesJavaWork[B, Btupnr] }
      "be serializable for case class C" in { doesJavaWork[C, Ctupnr] }
    }

    "MacroGenerated Injection to map" should {
      "be serializable for case class A" in { doesJavaWork[A] }
      "be serializable for case class B" in { doesJavaWork[B] }
      "be serializable for case class C" in { doesJavaWork[C] }
    }
  }
}
