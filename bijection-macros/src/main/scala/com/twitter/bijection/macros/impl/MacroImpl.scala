package com.twitter.bijection.macros.impl

import scala.collection.mutable.{ Map => MMap }
import scala.language.experimental.macros
import scala.reflect.macros.Context
import scala.reflect.runtime.universe._
import scala.util.Try

import com.twitter.bijection._
import com.twitter.bijection.macros.common.{ IsCaseClass, MacroGenerated }
import com.twitter.bijection.macros.common.impl.{ MacroImpl => CommonMacroImpl }

object MacroImpl {
  def caseClassToTupleImplWithOption[T, Tup](c: Context)(recursivelyApply: c.Expr[Boolean])(proof: c.Expr[IsCaseClass[T]])(implicit T: c.WeakTypeTag[T], Tup: c.WeakTypeTag[Tup]): c.Expr[Bijection[T, Tup]] = {
    import c.universe._
    recursivelyApply match {
      case q"""true""" => caseClassToTupleNoProofImpl(c)(T, Tup)
      case q"""false""" => caseClassToTupleNoProofImplNonRecursive(c)(T, Tup)
    }
  }

  def caseClassToTupleImpl[T, Tup](c: Context)(proof: c.Expr[IsCaseClass[T]])(implicit T: c.WeakTypeTag[T], Tup: c.WeakTypeTag[Tup]): c.Expr[Bijection[T, Tup]] =
    caseClassToTupleNoProofImpl(c)(T, Tup)

  def caseClassToTupleImplNonRecursive[T, Tup](c: Context)(proof: c.Expr[IsCaseClass[T]])(implicit T: c.WeakTypeTag[T], Tup: c.WeakTypeTag[Tup]): c.Expr[Bijection[T, Tup]] =
    caseClassToTupleNoProofImplNonRecursive(c)(T, Tup)

  def caseClassToTupleNoProofImplNonRecursive[T, Tup](c: Context)(implicit T: c.WeakTypeTag[T], Tup: c.WeakTypeTag[Tup]): c.Expr[Bijection[T, Tup]] = caseClassToTupleNoProofImplCommon(c, false)(T, Tup)

  def caseClassToTupleNoProofImpl[T, Tup](c: Context)(implicit T: c.WeakTypeTag[T], Tup: c.WeakTypeTag[Tup]): c.Expr[Bijection[T, Tup]] = caseClassToTupleNoProofImplCommon(c, true)(T, Tup)

  def caseClassToTupleNoProofImplCommon[T, Tup](c: Context, recursivelyApply: Boolean)(implicit T: c.WeakTypeTag[T], Tup: c.WeakTypeTag[Tup]): c.Expr[Bijection[T, Tup]] = {
    import c.universe._

    val tupleCaseClassCache = MMap.empty[Type, Tree]

    //TODO pull out?
    def tupleCaseClassEquivalent(tpe: Type): Seq[Tree] =
      tpe.declarations.collect {
        case m: MethodSymbol if m.isCaseAccessor =>
          m.returnType match {
            case tpe if CommonMacroImpl.isCaseClassType(c)(tpe) =>
              tupleCaseClassCache.getOrElseUpdate(tpe, {
                val equiv = tupleCaseClassEquivalent(tpe)
                AppliedTypeTree(Ident(newTypeName("Tuple" + equiv.size)), equiv.toList)
              })
            case tpe => Ident(tpe.typeSymbol.name.toTypeName)
          }
      }.toSeq

    val convCache = MMap.empty[Type, TermName]

    //TODO can make error handling better
    val companion = T.tpe.typeSymbol.companionSymbol
    val getPutConv = T.tpe
      .declarations
      .collect { case m: MethodSymbol if m.isCaseAccessor => m }
      .zip(tupleCaseClassEquivalent(T.tpe))
      .zip(Tup.tpe.declarations.collect { case m: MethodSymbol if m.isCaseAccessor => m })
      .zipWithIndex
      .map {
        case (((tM, treeEquiv), tupM), idx) =>
          tM.returnType match {
            case tpe if recursivelyApply && CommonMacroImpl.isCaseClassType(c)(tpe) =>
              val needDeclaration = !convCache.contains(tpe)
              val conv = convCache.getOrElseUpdate(tpe, newTermName("c2t_" + idx))
              (q"""$conv.invert(tup.$tupM)""",
                q"""$conv(t.$tM)""",
                if (needDeclaration) Some(q"""val $conv = implicitly[_root_.com.twitter.bijection.Bijection[${tM.returnType}, $treeEquiv]]""") else None) // cache these
            case tpe =>
              (q"""tup.$tupM""",
                q"""t.$tM""",
                None)
          }
      }

    val getters = getPutConv.map(_._1)
    val putters = getPutConv.map(_._2)
    val converters = getPutConv.flatMap(_._3)

    c.Expr[Bijection[T, Tup]](q"""
    _root_.com.twitter.bijection.macros.impl.MacroGeneratedBijection[$T,$Tup](
      { t: $T =>
        ..$converters
        (..$putters)
      },
      { tup: $Tup =>
        ..$converters
        $companion(..$getters)
      }
    )
    """)
  }

  def caseClassToMapImplWithOption[T](c: Context)(recursivelyApply: c.Expr[Boolean])(proof: c.Expr[IsCaseClass[T]])(implicit T: c.WeakTypeTag[T]): c.Expr[Injection[T, Map[String, Any]]] = {
    import c.universe._
    recursivelyApply match {
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
          case tpe if recursivelyApply && CommonMacroImpl.isCaseClassType(c)(tpe) =>
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
    _root_.com.twitter.bijection.macros.impl.MacroGeneratedInjection[$T, _root_.scala.collection.immutable.Map[String, Any]](
      { t: $T =>
        ..$converters
        _root_.scala.collection.immutable.Map[String, Any](..$putters)
      },
      { m: _root_.scala.collection.immutable.Map[String, Any] =>
        ..$converters
        try { _root_.scala.util.Success($companion(..$getters)) } catch { case _root_.scala.util.control.NonFatal(e) => _root_.scala.util.Failure(e) }
      }
    )
    """)
  }
}

case class MacroGeneratedBijection[A, B](fn: A => B, inv: B => A) extends Bijection[A, B] with MacroGenerated {
  override def apply(a: A) = fn(a)
  override def invert(b: B) = inv(b)
}
case class MacroGeneratedInjection[A, B](fn: A => B, inv: B => Try[A]) extends Injection[A, B] with MacroGenerated {
  override def apply(a: A) = fn(a)
  override def invert(b: B) = inv(b)
}

//TODO test serialization of them
