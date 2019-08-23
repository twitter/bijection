package com.twitter.bijection.macros

import org.scalatest.{Matchers, WordSpec}

import com.twitter.bijection._
import com.twitter.bijection.macros._

import scala.util.{Failure, Success}

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

  def alwaysFail(): Unit = {
    true should be(false)
  }

  "Fast Try" when {
    "creating Try" should {
      "give a Failure with expection" in {
        Macros.fastTry(sys.error("oh no")) match {
          case Failure(e) => e.getMessage.contains("oh no") should be(true)
          case _          => alwaysFail()
        }
        Macros.fastTry {
          val l = List(1, 2, 3)
          val l2 = l.filter(_ < 0)
          l2.head
        }.isFailure should be(true)
      }
      "not fail when there is no exception" in {
        Macros.fastTry(1) should be(Success(1))
        Macros.fastTry(List(1, 2)) should be(Success(List(1, 2)))
        Macros.fastTry {
          val l = List(1, 2, 3)
          val l2 = l.filter(_ > 0)
          l2.last
        } should be(Success(3))
      }
    }
    "attempt Inversion" should {
      "give a InversionFailure on error" in {
        Macros.fastAttempt("12m")("12m".toInt) match {
          case Failure(InversionFailure(d, e)) => d should be("12m")
          case _                               => alwaysFail()
        }
        Macros.fastAttempt("12m")(sys.error("This is lazy")) match {
          case Failure(InversionFailure(d, e)) => d should be("12m")
          case _                               => alwaysFail()
        }
      }
      "give Success when correct" in {
        Macros.fastAttempt("12")("12".toInt) should be(Success(12))
      }
    }
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
