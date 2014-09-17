package com.twitter.bijection.macros.common.impl

import scala.language.experimental.macros
import scala.reflect.macros.Context
import scala.reflect.runtime.universe._
import scala.util.{ Try => BasicTry }

import com.twitter.bijection.macros.common.{ Derived, IsCaseClass, MacroGenerated, NotDerived, TypesNotEqual }

object MacroImpl {
  def isCaseClassImpl[T](c: Context)(implicit T: c.WeakTypeTag[T]): c.Expr[IsCaseClass[T]] = {
    import c.universe._
    if (isCaseClassType(c)(T.tpe)) {
      //TOOD we should support this, just need to make sure it is concrete
      if (T.tpe.typeConstructor.takesTypeArgs) {
        c.abort(c.enclosingPosition, "Case class with type parameters currently not supported")
      } else {
        c.Expr[IsCaseClass[T]](q"""_root_.com.twitter.bijection.macros.common.impl.MacroGeneratedIsCaseClass[$T]()""")
      }
    } else {
      c.abort(c.enclosingPosition, "Type parameter is not a case class")
    }
  }

  def isCaseClassType(c: Context)(tpe: c.universe.Type): Boolean =
    BasicTry { tpe.typeSymbol.asClass.isCaseClass }.toOption.getOrElse(false)

  def typesNotEqualImpl[A, B](c: Context)(implicit A: c.WeakTypeTag[A], B: c.WeakTypeTag[B]): c.Expr[TypesNotEqual[A, B]] = {
    import c.universe._
    val a = A.tpe
    val b = B.tpe
    if (a =:= b) c.abort(c.enclosingPosition, s"Types A[$a] and B[$b] are equal")
    c.Expr[TypesNotEqual[A, B]](q"""_root_.com.twitter.bijection.macros.common.impl.MacroGeneratedTypesNotEqual[$A, $B]()""")
  }

  def notDerivedImpl[T](c: Context)(implicit T: c.WeakTypeTag[T]): c.Expr[NotDerived[T]] = {
    import c.universe._
    val t = T.tpe
    if (t <:< typeOf[Derived[_]]) c.abort(c.enclosingPosition, s"Type T[$t] is Derived")
    c.Expr[NotDerived[T]](q"""_root_.com.twitter.bijection.macros.common.impl.MacroGeneratedNotDerived[$T]()""")
  }
}

case class MacroGeneratedIsCaseClass[T]() extends IsCaseClass[T] with MacroGenerated
case class MacroGeneratedTypesNotEqual[A, B]() extends TypesNotEqual[A, B] with MacroGenerated
case class MacroGeneratedNotDerived[T]() extends NotDerived[T] with MacroGenerated
