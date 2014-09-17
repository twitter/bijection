package com.twitter.bijection.macros.common

/**
 * This trait is meant to be used exclusively to allow the type system to prove that a class is or is not a case class.
 */
trait IsCaseClass[T]

/**
 * This is a tag trait to allow macros to signal, in a uniform way, that a piece of code was generated.
 */
trait MacroGenerated

trait TypesNotEqual[A, B]

trait NotDerived[T]

object Derived {
  implicit def toDerived[T](implicit notDerived: NotDerived[T], t: T): Derived[T] = Derived(t)
  def derive[T](implicit t: Derived[T]): T = t.get
}
case class Derived[T](get: T)
