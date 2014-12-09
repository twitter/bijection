package com.twitter.bijection.macros

import scala.language.experimental.macros

import com.twitter.bijection._
import com.twitter.bijection.macros.impl.{ CaseClassToTuple, CaseClassToMap }

trait LowerPriorityMacroImplicits {
  implicit def materializeCaseClassToTupleNonRecursive[T: IsCaseClass, Tup]: Bijection[T, Tup] = macro CaseClassToTuple.caseClassToTupleImplNonRecursive[T, Tup]
  implicit def materializeCaseClassToMapNonRecursive[T: IsCaseClass]: Injection[T, Map[String, Any]] = macro CaseClassToMap.caseClassToMapImplNonRecursive[T]
}
object LowerPriorityMacroImplicits extends LowerPriorityMacroImplicits
object MacroImplicits extends LowerPriorityMacroImplicits {
  implicit def materializeCaseClassToTuple[T: IsCaseClass, Tup]: Bijection[T, Tup] = macro CaseClassToTuple.caseClassToTupleImpl[T, Tup]
  implicit def materializeCaseClassToMap[T: IsCaseClass]: Injection[T, Map[String, Any]] = macro CaseClassToMap.caseClassToMapImpl[T]
}
