package com.twitter.bijection.macros.impl

import scala.language.experimental.macros
import scala.reflect.macros.Context
import scala.reflect.runtime.universe._
import scala.util.{ Try => BasicTry }

case class FieldDescriptor[C <: Context](c: C, indx: Int, name: String, fType: C#Type)

object FieldDescriptor {
  def extractFromTpe[T](c: Context)(implicit T: c.WeakTypeTag[T]): Iterable[FieldDescriptor[Context]] = {
    import c.universe._

    T.tpe
      .declarations
      .collect { case m: MethodSymbol if m.isCaseAccessor => m }
      .zipWithIndex
      .map {
        case (m, idx) =>
          val fieldName = m.name.toTermName.toString
          val fieldType = m.returnType
          FieldDescriptor(c, idx, fieldName, fieldType)
      }
  }
}