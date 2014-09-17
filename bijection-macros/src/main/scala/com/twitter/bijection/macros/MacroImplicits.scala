package com.twitter.bijection.macros

import scala.language.experimental.macros

import com.twitter.bijection._
import com.twitter.bijection.macros.common.IsCaseClass
import com.twitter.bijection.macros.impl.MacroImpl

trait LowerPriorityMacroImplicits {
  implicit def materializeCaseClassToTupleNonRecursive[T: IsCaseClass, Tup]: Bijection[T, Tup] = macro MacroImpl.caseClassToTupleImplNonRecursive[T, Tup]
  implicit def materializeCaseClassToMapNonRecursive[T: IsCaseClass]: Injection[T, Map[String, Any]] = macro MacroImpl.caseClassToMapImplNonRecursive[T]
}
object LowerPriorityMacroImplicits extends LowerPriorityMacroImplicits
object MacroImplicits extends LowerPriorityMacroImplicits {
  implicit def materializeCaseClassToTuple[T: IsCaseClass, Tup]: Bijection[T, Tup] = macro MacroImpl.caseClassToTupleImpl[T, Tup]
  implicit def materializeCaseClassToMap[T: IsCaseClass]: Injection[T, Map[String, Any]] = macro MacroImpl.caseClassToMapImpl[T]
}
