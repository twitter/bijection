package com.twitter.bijection.macros

import com.twitter.bijection._
import com.twitter.bijection.macros.common.Derived

object TypeclassBijection {
  implicit def typeclassBijection[T[_], A, B](implicit tcBij: TypeclassBijection[T], typeclass: T[A], bij: ImplicitBijection[A, B]): Derived[T[B]] = Derived(tcBij(typeclass, bij.bijection))
}
trait TypeclassBijection[T[_]] {
  def apply[A, B](tc: T[A], bij: Bijection[A, B]): T[B]
}
