package com.twitter.bijection.macros.common

/**
 * This trait is meant to be used exclusively to allow the type system to prove that a class is or is not a case class.
 */
trait IsCaseClass[T]

/**
 * This is a tag trait to allow macros to signal, in a uniform way, that a piece of code was generated.
 */
trait MacroGenerated
