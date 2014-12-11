package com.twitter.bijection.macros.impl

import scala.collection.mutable.{ Map => MMap }
import scala.language.experimental.macros
import scala.reflect.macros.Context
import scala.reflect.runtime.universe._
import scala.util.Try

import com.twitter.bijection._
import com.twitter.bijection.macros.{ IsCaseClass, MacroGenerated }

private[bijection] object CaseClassToMap {
  def caseClassToMapImplWithOption[T](c: Context)(recursivelyApply: c.Expr[Boolean])(proof: c.Expr[IsCaseClass[T]])(implicit T: c.WeakTypeTag[T]): c.Expr[Injection[T, Map[String, Any]]] = {
    import c.universe._
    recursivelyApply.tree match {
      case q"""true""" => caseClassToMapNoProofImpl(c)(T)
      case q"""false""" => caseClassToMapNoProofImplNonRecursive(c)(T)
    }
  }

  def caseClassToMapImpl[T](c: Context)(proof: c.Expr[IsCaseClass[T]])(implicit T: c.WeakTypeTag[T]): c.Expr[Injection[T, Map[String, Any]]] =
    caseClassToMapNoProofImpl(c)(T)

  def caseClassToMapImplNonRecursive[T](c: Context)(proof: c.Expr[IsCaseClass[T]])(implicit T: c.WeakTypeTag[T]): c.Expr[Injection[T, Map[String, Any]]] =
    caseClassToMapNoProofImplNonRecursive(c)(T)

  def caseClassToMapNoProofImplNonRecursive[T](c: Context)(implicit T: c.WeakTypeTag[T]): c.Expr[Injection[T, Map[String, Any]]] = caseClassToMapNoProofImplCommon(c, false)(T)

  // TODO the only diff between this and the above is the case match and the converters. it's easy to gate this on the boolean
  def caseClassToMapNoProofImpl[T](c: Context)(implicit T: c.WeakTypeTag[T]): c.Expr[Injection[T, Map[String, Any]]] = caseClassToMapNoProofImplCommon(c, true)(T)

  def caseClassToMapNoProofImplCommon[T](c: Context, recursivelyApply: Boolean)(implicit T: c.WeakTypeTag[T]): c.Expr[Injection[T, Map[String, Any]]] = {
    import c.universe._
    //TODO can make error handling better?
    val companion = T.tpe.typeSymbol.companionSymbol

    val getPutConv = T.tpe.declarations.collect { case m: MethodSymbol if m.isCaseAccessor => m }.zipWithIndex.map {
      case (m, idx) =>
        val returnType = m.returnType
        val accStr = m.name.toTermName.toString
        returnType match {
          case tpe if recursivelyApply && IsCaseClassImpl.isCaseClassType(c)(tpe) =>
            val conv = newTermName("c2m_" + idx)
            (q"""$conv.invert(m($accStr).asInstanceOf[_root_.scala.collection.immutable.Map[String, Any]]).get""",
              q"""($accStr, $conv(t.$m))""",
              Some(q"""val $conv = implicitly[_root_.com.twitter.bijection.Injection[$tpe, _root_.scala.collection.immutable.Map[String, Any]]]""")) //TODO cache these
          case tpe =>
            (q"""m($accStr).asInstanceOf[$returnType]""",
              q"""($accStr, t.$m)""",
              None)
        }
    }

    val getters = getPutConv.map(_._1)
    val putters = getPutConv.map(_._2)
    val converters = getPutConv.flatMap(_._3)

    c.Expr[Injection[T, Map[String, Any]]](q"""
    new Injection[$T, _root_.scala.collection.immutable.Map[String, Any]] with MacroGenerated {
      override def apply(t: $T) = {
        ..$converters
        _root_.scala.collection.immutable.Map[String, Any](..$putters)
      }
      override def invert(m: _root_.scala.collection.immutable.Map[String, Any]) = {
        ..$converters
        try { _root_.scala.util.Success($companion(..$getters)) } catch { case _root_.scala.util.control.NonFatal(e) => _root_.scala.util.Failure(e) }
      }
    }
    """)

  }
}
