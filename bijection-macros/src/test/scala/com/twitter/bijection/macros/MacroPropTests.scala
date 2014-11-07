package com.twitter.bijection.macros

import scala.util.Success

import org.scalacheck.Arbitrary
import org.scalatest.{ Matchers, PropSpec }
import org.scalatest.prop.PropertyChecks

import com.twitter.bijection._
import com.twitter.bijection.macros._
import com.twitter.bijection.macros.common.{ MacroImplicits => CommonMacroImplicits, _ }
import com.twitter.chill.Externalizer

trait MacroPropTests extends PropSpec with PropertyChecks with Matchers with MacroTestHelper {
  import MacroImplicits._
  import CommonMacroImplicits._
  import MacroCaseClasses._

  //TODO make a macro to autogenerate arbitraries for case classes
  implicit def arbA: Arbitrary[A] = Arbitrary[A] {
    for (
      a <- Arbitrary.arbInt.arbitrary;
      b <- Arbitrary.arbString.arbitrary
    ) yield A(a, b)
  }

  implicit def arbB: Arbitrary[B] = Arbitrary[B] {
    for (
      a1 <- arbA.arbitrary;
      a2 <- arbA.arbitrary;
      y <- Arbitrary.arbString.arbitrary
    ) yield B(a1, a2, y)
  }

  implicit def arbC: Arbitrary[C] = Arbitrary[C] {
    for (
      a <- arbA.arbitrary;
      b <- arbB.arbitrary;
      c <- arbA.arbitrary;
      d <- arbB.arbitrary;
      e <- arbB.arbitrary
    ) yield C(a, b, c, d, e)
  }
}

trait CaseClassToTuplePropTests extends MacroPropTests {
  def shouldRoundTrip[A, B <: Product](t: A)(implicit proof: IsCaseClass[A], bij: Bijection[A, B]) {
    val mgbij = isMg(bij)
    t shouldBe mgbij.invert(mgbij(t))
  }

  def shouldRoundTrip[A, B <: Product](t: B)(implicit proof: IsCaseClass[A], bij: Bijection[A, B]) {
    val mgbij = isMg(bij)
    t shouldBe mgbij(mgbij.invert(t))
  }
}

class CaseClassToTupleRecurisvelyAppliedPropTests extends CaseClassToTuplePropTests {
  import MacroImplicits._
  import CommonMacroImplicits._
  import MacroCaseClasses._

  property("case class A(Int, String) should round trip") {
    forAll { v: A => shouldRoundTrip[A, Atup](v) }
  }

  property("case class B(A, A, String) should round trip") {
    forAll { v: B => shouldRoundTrip[B, Btup](v) }
  }

  property("case class C(A, B, A, B, B) should round trip") {
    forAll { v: C => shouldRoundTrip[C, Ctup](v) }
  }

  property("case class A(Int, String) should round trip in reverse") {
    forAll { v: Atup => shouldRoundTrip[A, Atup](v) }
  }

  property("case class B(A, A, String) should round trip in reverse") {
    forAll { v: Btup => shouldRoundTrip[B, Btup](v) }
  }

  property("case class C(A, B, A, B, B) should round trip in reverse") {
    forAll { v: Ctup => shouldRoundTrip[C, Ctup](v) }
  }
}

class CaseClassToTupleNonRecursivelyAppliedPropTests extends CaseClassToTuplePropTests {
  import LowerPriorityMacroImplicits._
  import CommonMacroImplicits._
  import MacroCaseClasses._

  property("case class A(Int, String) should round trip") {
    forAll { v: A => shouldRoundTrip[A, Atupnr](v) }
  }

  property("case class B(A, A, String) should round trip") {
    forAll { v: B => shouldRoundTrip[B, Btupnr](v) }
  }

  property("case class C(A, B, A, B, B) should round trip") {
    forAll { v: C => shouldRoundTrip[C, Ctupnr](v) }
  }

  property("case class A(Int, String) should round trip in reverse") {
    forAll { v: Atupnr => shouldRoundTrip[A, Atupnr](v) }
  }

  property("case class B(A, A, String) should round trip in reverse") {
    forAll { v: Btupnr => shouldRoundTrip[B, Btupnr](v) }
  }

  property("case class C(A, B, A, B, B) should round trip in reverse") {
    forAll { v: Ctupnr => shouldRoundTrip[C, Ctupnr](v) }
  }
}

trait CaseClassToMapPropTests extends MacroPropTests {
  def shouldRoundTrip[A](t: A)(implicit proof: IsCaseClass[A], inj: Injection[A, Map[String, Any]]) {
    val mginj = isMg(inj)
    Success(t) shouldBe mginj.invert(mginj(t))
  }

  def shouldRoundTrip[A](t: Map[String, Any])(implicit proof: IsCaseClass[A], inj: Injection[A, Map[String, Any]]) {
    val mginj = isMg(inj)
    Success(t) shouldBe mginj.invert(t).map { mginj(_) }
  }
}

class CaseClassToMapRecursivelyAppliedPropTests extends CaseClassToMapPropTests {
  import MacroImplicits._
  import CommonMacroImplicits._
  import MacroCaseClasses._

  property("case class A(Int, String) should round trip") {
    forAll { v: A => shouldRoundTrip[A](v) }
  }

  property("case class B(A, A, String) should round trip") {
    forAll { v: B => shouldRoundTrip[B](v) }
  }

  property("case class C(A, B, A, B, B) should round trip") {
    forAll { v: C => shouldRoundTrip[C](v) }
  }

  property("case class A(Int, String) should round trip in reverse") {
    forAll { v: Atup => shouldRoundTrip[A](Map[String, Any]("x" -> v._1, "y" -> v._2)) }
  }

  property("case class B(A, A, String) should round trip in reverse") {
    forAll { v: Btup =>
      shouldRoundTrip[B](
        Map[String, Any](
          "a1" -> Map[String, Any]("x" -> v._1._1, "y" -> v._1._2),
          "a2" -> Map[String, Any]("x" -> v._2._1, "y" -> v._2._2),
          "y" -> v._3))
    }
  }

  property("case class C(A, B, A, B, B) should round trip in reverse") {
    forAll { v: Ctup =>
      shouldRoundTrip[C](
        Map[String, Any](
          "a" -> Map[String, Any]("x" -> v._1._1, "y" -> v._1._2),
          "b" -> Map[String, Any](
            "a1" -> Map[String, Any]("x" -> v._2._1._1, "y" -> v._2._1._2),
            "a2" -> Map[String, Any]("x" -> v._2._2._1, "y" -> v._2._2._2),
            "y" -> v._2._3),
          "c" -> Map[String, Any]("x" -> v._3._1, "y" -> v._3._2),
          "d" -> Map[String, Any](
            "a1" -> Map[String, Any]("x" -> v._4._1._1, "y" -> v._4._1._2),
            "a2" -> Map[String, Any]("x" -> v._4._2._1, "y" -> v._4._2._2),
            "y" -> v._4._3),
          "e" -> Map[String, Any](
            "a1" -> Map[String, Any]("x" -> v._5._1._1, "y" -> v._5._1._2),
            "a2" -> Map[String, Any]("x" -> v._5._2._1, "y" -> v._5._2._2),
            "y" -> v._5._3)))
    }
  }
}

class CaseClassToMapNonRecursivelyAppliedPropTests extends CaseClassToMapPropTests {
  import LowerPriorityMacroImplicits._
  import CommonMacroImplicits._
  import MacroCaseClasses._

  property("case class A(Int, String) should round trip") {
    forAll { v: A => shouldRoundTrip[A](v) }
  }

  property("case class B(A, A, String) should round trip") {
    forAll { v: B => shouldRoundTrip[B](v) }
  }

  property("case class C(A, B, A, B, B) should round trip") {
    forAll { v: C => shouldRoundTrip[C](v) }
  }

  property("case class A(Int, String) should round trip in reverse") {
    forAll { v: Atupnr => shouldRoundTrip[A](Map[String, Any]("x" -> v._1, "y" -> v._2)) }
  }

  property("case class B(A, A, String) should round trip in reverse") {
    forAll { v: Btupnr =>
      shouldRoundTrip[B](
        Map[String, Any](
          "a1" -> v._1,
          "a2" -> v._2,
          "y" -> v._3))
    }
  }

  property("case class C(A, B, A, B, B) should round trip in reverse") {
    forAll { v: Ctupnr =>
      shouldRoundTrip[C](Map[String, Any]("a" -> v._1, "b" -> v._2, "c" -> v._3, "d" -> v._4, "e" -> v._5))
    }
  }
}
