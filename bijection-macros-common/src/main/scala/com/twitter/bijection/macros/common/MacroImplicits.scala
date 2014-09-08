package com.twitter.bijection.macros.common

import scala.language.experimental.macros

import com.twitter.bijection.macros.common.impl.MacroImpl

object MacroImplicits {
  /**
   * This method provides proof that the given type is a case class.
   */
  implicit def isCaseClass[T]: IsCaseClass[T] = macro MacroImpl.isCaseClassImpl[T]
}
