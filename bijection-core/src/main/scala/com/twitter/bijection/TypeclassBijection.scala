package com.twitter.bijection

object TypeclassBijection {
  implicit class RichTypeclass[T[_], A](t: T[A]) {
    def bijectTo[B](implicit tcBij: TypeclassBijection[T], bij: ImplicitBijection[A, B]): T[B] = tcBij(t, bij.bijection)
  }

  object BijectionAndTypeclass {
    implicit def get[T[_], From, To](implicit bij: ImplicitBijection[To, From], typeclass: T[From]) = BijectionAndTypeclass(bij, typeclass)
  }
  case class BijectionAndTypeclass[T[_], From, To](bij: ImplicitBijection[To, From], typeclass: T[From]) {
    def apply(tc: TypeclassBijection[T]): T[To] = TypeclassBijection.typeclassBijection[T, From, To](tc, typeclass, ImplicitBijection.reverse(bij.bijection))
  }

  def typeclassBijection[T[_], A, B](implicit tcBij: TypeclassBijection[T], typeclass: T[A], bij: ImplicitBijection[A, B]): T[B] = tcBij(typeclass, bij.bijection)
  def deriveFor[T[_], To](implicit tcBij: TypeclassBijection[T], batc: BijectionAndTypeclass[T, From, To] forSome { type From }): T[To] = batc(tcBij)
}
trait TypeclassBijection[T[_]] {
  def apply[A, B](tc: T[A], bij: Bijection[A, B]): T[B]
}
