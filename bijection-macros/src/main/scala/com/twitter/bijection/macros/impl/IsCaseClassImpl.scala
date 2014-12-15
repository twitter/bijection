package com.twitter.bijection.macros.impl

import scala.language.experimental.macros
import scala.reflect.macros.Context
import scala.reflect.runtime.universe._
import scala.util.{ Try => BasicTry }

import com.twitter.bijection.macros.{ IsCaseClass, MacroGenerated }

object IsCaseClassImpl {
  def isCaseClassImpl[T](c: Context)(implicit T: c.WeakTypeTag[T]): c.Expr[IsCaseClass[T]] = {
    import c.universe._
    if (isCaseClassType(c)(T.tpe)) {
      //TOOD we should support this, just need to make sure it is concrete
      if (T.tpe.typeConstructor.takesTypeArgs) {
        c.abort(c.enclosingPosition, "Case class with type parameters currently not supported")
      } else {
        c.Expr[IsCaseClass[T]](q"""_root_.com.twitter.bijection.macros.impl.MacroGeneratedIsCaseClass[$T]()""")
      }
    } else {
      c.abort(c.enclosingPosition, "Type parameter is not a case class")
    }
  }

  def isCaseClassType(c: Context)(tpe: c.universe.Type): Boolean =
    BasicTry { tpe.typeSymbol.asClass.isCaseClass }.toOption.getOrElse(false)
}

case class MacroGeneratedIsCaseClass[T]() extends IsCaseClass[T] with MacroGenerated
