package com.twitter.bijection.macros

import scala.language.experimental.macros

import com.twitter.bijection._
import com.twitter.bijection.macros.impl.{CaseClassToTuple, CaseClassToMap, IsCaseClassImpl}

trait LowerPriorityMacroImplicits {
  // Since implicit macro's aborting makes them just appear to never having existed
  // its valid for this to be lower priority for the one in MacroImplicits.
  // We will attempt this one if we don't see the other
  implicit def materializeCaseClassToTupleNonRecursive[T: IsCaseClass, Tup]: Bijection[T, Tup] =
    macro CaseClassToTuple.caseClassToTupleImplNonRecursive[T, Tup]
}

object MacroImplicits extends LowerPriorityMacroImplicits {
  implicit def materializeCaseClassToTuple[T: IsCaseClass, Tup]: Bijection[T, Tup] =
    macro CaseClassToTuple.caseClassToTupleImpl[T, Tup]
  implicit def materializeCaseClassToMap[T: IsCaseClass]: Injection[T, Map[String, Any]] =
    macro CaseClassToMap.caseClassToMapImpl[T]

  implicit def isCaseClass[T]: IsCaseClass[T] = macro IsCaseClassImpl.isCaseClassImpl[T]
}
