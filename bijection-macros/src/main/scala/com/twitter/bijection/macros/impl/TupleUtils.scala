package com.twitter.bijection.macros.impl

import scala.collection.mutable.{Map => MMap}
import scala.language.experimental.macros
import scala.reflect.macros.Context
import scala.reflect.runtime.universe._

class TupleUtils[C <: Context](val c: C) {
  import c.universe._
  private[this] val tupleCaseClassCache = MMap.empty[Type, Tree]

  // Takes a case class and generates the equiv tuple to it
  def tupleCaseClassEquivalent(tpe: Type): Seq[Tree] =
    tpe.declarations.collect {
      case m: MethodSymbol if m.isCaseAccessor =>
        m.returnType match {
          case tpe if IsCaseClassImpl.isCaseClassType(c)(tpe) =>
            tupleCaseClassCache.getOrElseUpdate(tpe, {
              val equiv = tupleCaseClassEquivalent(tpe)
              AppliedTypeTree(Ident(newTypeName("Tuple" + equiv.size)), equiv.toList)
            })
          case tpe => Ident(tpe.typeSymbol.name.toTypeName)
        }
    }.toSeq
}
