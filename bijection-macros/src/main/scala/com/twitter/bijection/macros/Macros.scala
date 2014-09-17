package com.twitter.bijection.macros

import scala.language.experimental.macros

import com.twitter.bijection._
import com.twitter.bijection.macros.common.IsCaseClass
import com.twitter.bijection.macros.impl.MacroImpl

object Macros {
  def caseClassToTuple[T: IsCaseClass, Tup](recursivelyApply: Boolean = true): Bijection[T, Tup] = macro MacroImpl.caseClassToTupleImplWithOption[T, Tup]
  def caseClassToMap[T: IsCaseClass](recursivelyApply: Boolean = true): Injection[T, Map[String, Any]] = macro MacroImpl.caseClassToMapImplWithOption[T]
}
