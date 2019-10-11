package com.twitter.bijection

import org.scalatest.{PropSpec, MustMatchers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class TypeclassBijectionLaws
    extends PropSpec
    with ScalaCheckPropertyChecks
    with MustMatchers
    with BaseProperties {
  import FakeAlgebird._
  import TypeclassBijection._

  case class A(x: Int, y: String)
  implicit val abij: Bijection[A, (Int, String)] = Bijection.build[A, (Int, String)] {
    A.unapply(_).get
  } { x =>
    A(x._1, x._2)
  }

  implicit val orderingTypeclassBijection: TypeclassBijection[Ordering] =
    new TypeclassBijection[Ordering] {
      def apply[A, B](tc: Ordering[A], bij: Bijection[A, B]) = tc.on { bij.invert(_) }
    }

  case class Wrapper(get: Int)
  implicit val wrapperbij: Bijection[Wrapper, Int] = Bijection.build[Wrapper, Int] { _.get } {
    Wrapper(_)
  }

  property("basic") {
    implicitly[Semigroup[Int]]
    implicitly[Semigroup[String]]
    implicitly[Semigroup[(Int, String)]]
    typeclassBijection[Semigroup, (Int, String), A]
    implicitly[Semigroup[(Int, String)]].bijectTo[A]
    deriveFor[Semigroup, A]
    deriveFor[Ordering, A]
    deriveFor[Ordering, Wrapper]
  }
}

object FakeAlgebird {
  object Semigroup {
    implicit val intSemigroup: Semigroup[Int] =
      new Semigroup[Int] {
        override def plus(left: Int, right: Int) = left + right
      }

    implicit val strSemigroup: Semigroup[String] =
      new Semigroup[String] {
        override def plus(left: String, right: String) = left + right
      }

    implicit def tup2Semigroup[A, B](
        implicit s1: Semigroup[A],
        s2: Semigroup[B]
    ): Semigroup[(A, B)] =
      new Semigroup[(A, B)] {
        override def plus(l: (A, B), r: (A, B)) = (s1.plus(l._1, r._1), s2.plus(l._2, r._2))
      }

    implicit val semigroupTypeclassBijection: TypeclassBijection[Semigroup] =
      new TypeclassBijection[Semigroup] {
        def apply[A, B](tc: Semigroup[A], bij: Bijection[A, B]) =
          new Semigroup[B] {
            override def plus(left: B, right: B) =
              bij(tc.plus(bij.invert(left), bij.invert(right)))
          }
      }
  }
  trait Semigroup[T] {
    def plus(left: T, right: T): T
  }
}
