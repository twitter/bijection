package com.twitter.bijection.macros.impl

import scala.language.experimental.macros
import scala.reflect.macros.Context
import scala.reflect.runtime.universe._
import scala.util.{ Try => BasicTry }

import com.twitter.bijection.macros.{ IsPrimitiveCaseClass, MacroGenerated }

object IsPrimitiveCaseClassImpl {

  private[this] val primitiveTypes = List(
    "Boolean",
    "Double",
    "Float",
    "Long",
    "Int",
    "Short",
    "Byte",
    "Boolean",
    "Char")

  def isPrimitiveCaseClassImpl[T](c: Context)(implicit T: c.WeakTypeTag[T]): c.Expr[IsPrimitiveCaseClass[T]] = {
    import c.universe._
    if (IsCaseClassImpl.isCaseClassType(c)(T.tpe)) {
      if (T.tpe.typeConstructor.takesTypeArgs) {
        c.abort(c.enclosingPosition, "Case class with type parameters currently not supported")
      } else if (!hasOnlyPrimitiveTypes[T](c)) {
        c.abort(c.enclosingPosition, "Non primitive case class")
      } else {
        c.Expr[IsPrimitiveCaseClass[T]](q"""_root_.com.twitter.bijection.macros.impl.MacroGeneratedIsPrimitiveCaseClass[$T]()""")
      }
    } else {
      c.abort(c.enclosingPosition, "Type parameter is not a case class")
    }
  }

  def hasOnlyPrimitiveTypes[T](c: Context)(implicit T: c.WeakTypeTag[T]): Boolean =
    FieldDescriptor.extractFromTpe[T](c).forall { t =>
      primitiveTypes.contains(t.fType.toString)
    }

}

case class MacroGeneratedIsPrimitiveCaseClass[T]() extends IsPrimitiveCaseClass[T] with MacroGenerated
