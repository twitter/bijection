package com.twitter.bijection.macros

import scala.language.experimental.macros
import com.twitter.bijection.macros.impl._

/**
 * This trait is meant to be used exclusively to allow the type system to prove that a class is or is not a case class.
 */
object IsCaseClass {
  implicit def isCaseClass[T]: IsCaseClass[T] = macro IsCaseClassImpl.isCaseClassImpl[T]
}

trait IsCaseClass[T]

object IsPrimitiveCaseClass {
  implicit def isPrimitiveCaseClass[T]: IsPrimitiveCaseClass[T] = macro IsPrimitiveCaseClassImpl.isPrimitiveCaseClassImpl[T]
}

trait IsPrimitiveCaseClass[T] extends IsCaseClass[T]

/**
 * This is a tag trait to allow macros to signal, in a uniform way, that a piece of code was generated.
 */
trait MacroGenerated

