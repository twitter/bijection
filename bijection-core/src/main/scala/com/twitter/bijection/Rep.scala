package com.twitter.bijection

import java.net.{ MalformedURLException, URL }
import java.util.UUID

/**
 * Type tag used to indicate that an instance of a type such as String 
 * contains a valid representation of another type, such as Int or URL.
 */
trait Rep[A]

/**
 * Useful HasRep 
 */
object Rep {
  // TODO make implicit class in 2.10
  implicit def ToRepOpsPimp[A](a: A) = new ToRepOps(a)

  /**
   * Adds toRep[B] syntax to elements of type A if there is an implicit HasRep[A, B] in scope.
   */
  class ToRepOps[A](a: A) {
    def toRep[B](implicit ev: HasRep[A, B]): Option[A @@ Rep[B]] =
      ev.toRep(a)
  }

  sealed class CanonicalStringHasRep[B](f: String => B) extends HasRep[String, B] {
    final def toRep(s: String) = rep(s)(f)
  } 
  
  implicit val StringHasIntRep = new CanonicalStringHasRep[Int](_.toInt)
  implicit val StringHasLongRep = new CanonicalStringHasRep[Long](_.toLong)
  implicit val StringHasByteRep = new CanonicalStringHasRep[Byte](_.toByte)
  implicit val StringHasShortRep = new CanonicalStringHasRep[Short](_.toShort)
  implicit val StringHasFloatRep = new CanonicalStringHasRep[Float](_.toFloat)
  implicit val StringHasDoubleRep = new CanonicalStringHasRep[Double](_.toDouble)
  implicit val StringHasURLRep = new CanonicalStringHasRep[URL](new URL(_))
  implicit val StringHasUUIDRep = new CanonicalStringHasRep[UUID](UUID.fromString)
  
  private val catching = 
    scala.util.control.Exception.catching(
      classOf[NumberFormatException]
    , classOf[MalformedURLException]
    , classOf[IllegalArgumentException]
    )

  // catch and return option
  private def rep[B](a: String)(partial: String => B): Option[String @@ Rep[B]] =
    catching.opt(partial(a)) map (b => Tag(b.toString))
}

/**
 * Type class for summoning the function that can check whether the instance can be tagged with Rep
 */
trait HasRep[A, B] {
  def toRep(a: A): Option[A @@ Rep[B]]
}
