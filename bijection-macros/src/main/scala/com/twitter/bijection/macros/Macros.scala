package com.twitter.bijection.macros

import scala.language.experimental.macros

import com.twitter.bijection._
import com.twitter.bijection.macros.impl.{CaseClassToTuple, CaseClassToMap, TryMacros}

import scala.util.Try

object Macros {
  def caseClassToTuple[T: IsCaseClass, Tup](recursivelyApply: Boolean): Bijection[T, Tup] =
    macro CaseClassToTuple.caseClassToTupleImplWithOption[T, Tup]
  def caseClassToMap[T: IsCaseClass](recursivelyApply: Boolean): Injection[T, Map[String, Any]] =
    macro CaseClassToMap.caseClassToMapImplWithOption[T]

  /**
    * This can be used like Inversion.attempt. For example:
    * fastAttempt(intString)(intString.toInt)
    * The reason we don't take a B => A like attempt does is
    * that would still require calling apply on that function.
    * In contrast, here, we can inline it directly.
    */
  def fastAttempt[A, B](b: B)(inv: A): Try[A] = macro TryMacros.fastAttempt[A, B]

  /**
    * This macro expands out to a try block so it is only slower
    * than a try block in that it allocates Success/Failure wrappers
    */
  def fastTry[T](toEval: T): Try[T] = macro TryMacros.fastTry[T]
}
