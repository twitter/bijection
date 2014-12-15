package com.twitter.bijection.macros.impl

import scala.language.experimental.macros
import scala.reflect.macros.Context
import scala.reflect.runtime.universe._
import scala.util.Try

import com.twitter.bijection._

private[bijection] object TryMacros {
  def fastAttempt[A, B](c: Context)(b: c.Expr[B])(inv: c.Expr[A])(implicit A: c.WeakTypeTag[A]): c.Expr[Try[A]] = {
    import c.universe._
    c.Expr[scala.util.Try[A]](
      q"""(try { _root_.scala.util.Success($inv) }
        catch { case _root_.scala.util.control.NonFatal(e) =>
          _root_.scala.util.Failure(new _root_.com.twitter.bijection.InversionFailure($b, e)) }): _root_.scala.util.Try[$A]""")
  }

  def fastTry[T](c: Context)(toEval: c.Expr[T])(implicit T: c.WeakTypeTag[T]): c.Expr[Try[T]] = {
    import c.universe._
    c.Expr[scala.util.Try[T]](
      q"""(try { _root_.scala.util.Success($toEval) }
        catch { case _root_.scala.util.control.NonFatal(e) => _root_.scala.util.Failure(e) }): _root_.scala.util.Try[$T]""")
  }
}
