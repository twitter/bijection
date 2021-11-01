package com.twitter.bijection.macros.impl

import scala.collection.mutable.{Map => MMap}
import scala.reflect.macros.Context

import com.twitter.bijection._
import com.twitter.bijection.macros.IsCaseClass

private[bijection] object CaseClassToTuple {
  def caseClassToTupleImplWithOption[T, Tup](c: Context)(
      recursivelyApply: c.Expr[Boolean]
  )(
      proof: c.Expr[IsCaseClass[T]]
  )(implicit T: c.WeakTypeTag[T], Tup: c.WeakTypeTag[Tup]): c.Expr[Bijection[T, Tup]] = {
    import c.universe._
    recursivelyApply match {
      case q"""true"""  => caseClassToTupleNoProofImpl(c)(T, Tup)
      case q"""false""" => caseClassToTupleNoProofImplNonRecursive(c)(T, Tup)
      case _            => caseClassToTupleNoProofImpl(c)(T, Tup)
    }
  }

  // Entry point
  def caseClassToTupleImpl[T, Tup](
      c: Context
  )(
      proof: c.Expr[IsCaseClass[T]]
  )(implicit T: c.WeakTypeTag[T], Tup: c.WeakTypeTag[Tup]): c.Expr[Bijection[T, Tup]] =
    caseClassToTupleNoProofImpl(c)(T, Tup)

  def caseClassToTupleImplNonRecursive[T, Tup](
      c: Context
  )(
      proof: c.Expr[IsCaseClass[T]]
  )(implicit T: c.WeakTypeTag[T], Tup: c.WeakTypeTag[Tup]): c.Expr[Bijection[T, Tup]] =
    caseClassToTupleNoProofImplNonRecursive(c)(T, Tup)

  def caseClassToTupleNoProofImplNonRecursive[T, Tup](
      c: Context
  )(implicit T: c.WeakTypeTag[T], Tup: c.WeakTypeTag[Tup]): c.Expr[Bijection[T, Tup]] =
    caseClassToTupleNoProofImplCommon(c, false)(T, Tup)

  def caseClassToTupleNoProofImpl[T, Tup](
      c: Context
  )(implicit T: c.WeakTypeTag[T], Tup: c.WeakTypeTag[Tup]): c.Expr[Bijection[T, Tup]] =
    caseClassToTupleNoProofImplCommon(c, true)(T, Tup)

  def caseClassToTupleNoProofImplCommon[T, Tup](
      c: Context,
      recursivelyApply: Boolean
  )(implicit T: c.WeakTypeTag[T], Tup: c.WeakTypeTag[Tup]): c.Expr[Bijection[T, Tup]] = {
    import c.universe._
    val tupUtils = new TupleUtils[c.type](c)
    val convCache = MMap.empty[Type, TermName]

    // TODO can make error handling better
    val companion = T.tpe.typeSymbol.companionSymbol
    val tuple = Tup.tpe.typeSymbol.companionSymbol

    val getPutConv = T.tpe.declarations
      .collect { case m: MethodSymbol if m.isCaseAccessor => m }
      .zip(tupUtils.tupleCaseClassEquivalent(T.tpe))
      .zip(Tup.tpe.declarations.collect { case m: MethodSymbol if m.isCaseAccessor => m })
      .zipWithIndex
      .map { case (((tM, treeEquiv), tupM), idx) =>
        tM.returnType match {
          case tpe if recursivelyApply && IsCaseClassImpl.isCaseClassType(c)(tpe) =>
            val needDeclaration = !convCache.contains(tpe)
            val conv = convCache.getOrElseUpdate(tpe, newTermName("c2t_" + idx))
            (
              q"""$conv.invert(tup.$tupM)""",
              q"""$conv(t.$tM)""",
              if (needDeclaration)
                Some(
                  q"""val $conv = implicitly[_root_.com.twitter.bijection.Bijection[${tM.returnType}, $treeEquiv]]"""
                )
              else None
            ) // cache these
          case tpe =>
            (q"""tup.$tupM""", q"""t.$tM""", None)
        }
      }

    val getters = getPutConv.map(_._1)
    val putters = getPutConv.map(_._2)
    val converters = getPutConv.flatMap(_._3)

    c.Expr[Bijection[T, Tup]](q"""
    new Bijection[$T,$Tup] with MacroGenerated {
      override def apply(t: $T): $Tup = {
        ..$converters
        $tuple(..$putters)
      }
      override def invert(tup: $Tup): $T = {
        ..$converters
        $companion(..$getters)
      }
    }
    """)
  }
}
//TODO test serialization of them
