package com.twitter.bijection.macros

import com.twitter.bijection._
import org.scalacheck.Arbitrary
import org.scalatest.prop.{ Checkers, PropertyChecks }
import org.scalatest.{ Matchers, PropSpec }

import scala.util.Success
import org.scalacheck.Prop.forAll

trait MacroPropTests extends PropSpec with Checkers with Matchers with MacroTestHelper {
  import com.twitter.bijection.macros.MacroCaseClasses._

  //TODO make a macro to autogenerate arbitraries for case classes
  implicit def arbA: Arbitrary[SampleClassA] = Arbitrary[SampleClassA] {
    for (
      a <- Arbitrary.arbInt.arbitrary;
      b <- Arbitrary.arbString.arbitrary
    ) yield SampleClassA(a, b)
  }

  implicit def arbB: Arbitrary[SampleClassB] = Arbitrary[SampleClassB] {
    for (
      a1 <- arbA.arbitrary;
      a2 <- arbA.arbitrary;
      y <- Arbitrary.arbString.arbitrary
    ) yield SampleClassB(a1, a2, y)
  }

  implicit def arbC: Arbitrary[SampleClassC] = Arbitrary[SampleClassC] {
    for (
      a <- arbA.arbitrary;
      b <- arbB.arbitrary;
      c <- arbA.arbitrary;
      d <- arbB.arbitrary;
      e <- arbB.arbitrary
    ) yield SampleClassC(a, b, c, d, e)
  }
}

trait CaseClassToTuplePropTests extends MacroPropTests {
  def shouldRoundTrip[A, B <: Product](t: A)(implicit proof: IsCaseClass[A], bij: Bijection[A, B]) = {
    bij === a[MacroGenerated] &&
      t === bij.invert(bij(t))
  }

  def shouldRoundTrip[A, B <: Product](t: B)(implicit proof: IsCaseClass[A], bij: Bijection[A, B]) = {
    bij === a[MacroGenerated] &&
      t === bij(bij.invert(t))
  }
}

class CaseClassToTupleRecurisvelyAppliedPropTests extends CaseClassToTuplePropTests {
  import com.twitter.bijection.macros.MacroCaseClasses._
  import com.twitter.bijection.macros.MacroImplicits._

  property("case class A(Int, String) should round trip") {
    forAll { v: SampleClassA => shouldRoundTrip[SampleClassA, Atup](v) }
  }

  property("case class B(A, A, String) should round trip") {
    forAll { v: SampleClassB => shouldRoundTrip[SampleClassB, Btup](v) }
  }

  property("case class C(A, B, A, B, B) should round trip") {
    forAll { v: SampleClassC => shouldRoundTrip[SampleClassC, Ctup](v) }
  }

  property("case class A(Int, String) should round trip in reverse") {
    forAll { v: Atup => shouldRoundTrip[SampleClassA, Atup](v) }
  }

  property("case class B(A, A, String) should round trip in reverse") {
    forAll { v: Btup => shouldRoundTrip[SampleClassB, Btup](v) }
  }

  property("case class C(A, B, A, B, B) should round trip in reverse") {
    forAll { v: Ctup => shouldRoundTrip[SampleClassC, Ctup](v) }
  }
}

class CaseClassToTupleNonRecursivelyAppliedPropTests extends CaseClassToTuplePropTests {
  import com.twitter.bijection.macros.MacroCaseClasses._
  import com.twitter.bijection.macros.MacroImplicits._

  property("case class A(Int, String) should round trip") {
    forAll { v: SampleClassA => shouldRoundTrip[SampleClassA, Atupnr](v) }
  }

  property("case class B(A, A, String) should round trip") {
    forAll { v: SampleClassB => shouldRoundTrip[SampleClassB, Btupnr](v) }
  }

  property("case class C(A, B, A, B, B) should round trip") {
    forAll { v: SampleClassC => shouldRoundTrip[SampleClassC, Ctupnr](v) }
  }

  property("case class A(Int, String) should round trip in reverse") {
    forAll { v: Atupnr => shouldRoundTrip[SampleClassA, Atupnr](v) }
  }

  property("case class B(A, A, String) should round trip in reverse") {
    forAll { v: Btupnr => shouldRoundTrip[SampleClassB, Btupnr](v) }
  }

  property("case class C(A, B, A, B, B) should round trip in reverse") {
    forAll { v: Ctupnr => shouldRoundTrip[SampleClassC, Ctupnr](v) }
  }
}

trait CaseClassToMapPropTests extends MacroPropTests {
  def shouldRoundTrip[A](t: A)(implicit proof: IsCaseClass[A], inj: Injection[A, Map[String, Any]]) = {
    inj === a[MacroGenerated] &&
      Success(t) === inj.invert(inj(t))
  }

  def shouldRoundTrip[A](t: Map[String, Any])(implicit proof: IsCaseClass[A], inj: Injection[A, Map[String, Any]]) = {
    inj.invert(t).get
    inj === a[MacroGenerated] &&
      Success(t) === inj.invert(t).map { inj(_) }
  }
}

class CaseClassToMapRecursivelyAppliedPropTests extends CaseClassToMapPropTests {
  import com.twitter.bijection.macros.MacroCaseClasses._
  import com.twitter.bijection.macros.MacroImplicits._

  property("case class A(Int, String) should round trip") {
    forAll { v: SampleClassA => shouldRoundTrip[SampleClassA](v) }
  }

  property("case class B(A, A, String) should round trip") {
    forAll { v: SampleClassB => shouldRoundTrip[SampleClassB](v) }
  }

  property("case class C(A, B, A, B, B) should round trip") {
    forAll { v: SampleClassC => shouldRoundTrip[SampleClassC](v) }
  }

  property("case class A(Int, String) should round trip in reverse") {
    forAll { v: Atup => shouldRoundTrip[SampleClassA](Map[String, Any]("x" -> v._1, "y" -> v._2)) }
  }

  property("case class B(A, A, String) should round trip in reverse") {
    forAll { v: Btup =>
      shouldRoundTrip[SampleClassB](
        Map[String, Any](
          "a1" -> Map[String, Any]("x" -> v._1._1, "y" -> v._1._2),
          "a2" -> Map[String, Any]("x" -> v._2._1, "y" -> v._2._2),
          "y" -> v._3))
    }
  }

  property("case class C(A, B, A, B, B) should round trip in reverse") {
    forAll { v: Ctup =>
      shouldRoundTrip[SampleClassC](
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
  import com.twitter.bijection.macros.MacroCaseClasses._
  import com.twitter.bijection.macros.MacroImplicits._

  property("case class A(Int, String) should round trip") {
    forAll { v: SampleClassA => shouldRoundTrip[SampleClassA](v) }
  }

  property("case class B(A, A, String) should round trip") {
    forAll { v: SampleClassB => shouldRoundTrip[SampleClassB](v) }
  }

  property("case class C(A, B, A, B, B) should round trip") {
    forAll { v: SampleClassC => shouldRoundTrip[SampleClassC](v) }
  }

  property("case class A(Int, String) should round trip in reverse") {
    forAll { v: Atupnr => shouldRoundTrip[SampleClassA](Map[String, Any]("x" -> v._1, "y" -> v._2))(implicitly, Macros.caseClassToMap[SampleClassA](false)) }
  }

  property("case class B(A, A, String) should round trip in reverse") {
    forAll { v: Btupnr =>
      shouldRoundTrip[SampleClassB](
        Map[String, Any](
          "a1" -> v._1,
          "a2" -> v._2,
          "y" -> v._3))(implicitly, Macros.caseClassToMap[SampleClassB](false))
    }
  }

  property("case class C(A, B, A, B, B) should round trip in reverse") {
    forAll { v: Ctupnr =>
      shouldRoundTrip[SampleClassC](Map[String, Any]("a" -> v._1, "b" -> v._2, "c" -> v._3, "d" -> v._4, "e" -> v._5))(implicitly, Macros.caseClassToMap[SampleClassC](false))
    }
  }
}
