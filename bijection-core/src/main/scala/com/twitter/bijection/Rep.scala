package com.twitter.bijection

/**
 * Type tag used when a type such as String may contain representations of another type, such as Int or URL.
 */
trait Rep[A]

object Rep {
  private val catching = scala.util.control.Exception.catching(classOf[NumberFormatException])

  // for example the following lifts a String into String @@ Rep[Int]
  // could implicitly add toIntRep syntax as well
  def stringToIntRep(s: String): Option[String @@ Rep[Int]] = catching.opt(s.toInt) map (_ => Tag(s))
}