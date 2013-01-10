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

  implicit val StringHasIntRep = new HasRep[String, Int] {
    def toRep(s: String) = rep(s) { _.toInt }
  }

  implicit val StringHasLongRep = new HasRep[String, Long] {
    def toRep(s: String) = rep(s) { _.toLong }
  }

  implicit val StringHasByteRep = new HasRep[String, Byte] {
    def toRep(s: String) = rep(s) { _.toByte }
  }

  implicit val StringHasShortRep = new HasRep[String, Short]  {
    def toRep(s: String) = rep(s) { _.toShort }
  }

  implicit val StringHasFloatRep = new HasRep[String, Float] {
    def toRep(s: String) = rep(s) { _.toFloat }
  }

  implicit val StringHasDoubleRep = new HasRep[String, Double]  {
    def toRep(s: String) = rep(s) { _.toDouble }
  }

  implicit val StringHasURLRep = new HasRep[String, URL]  {
    def toRep(s: String) = rep(s) { new URL(_) }
  }

  implicit val StringHasUUIDRep = new HasRep[String, UUID]  {
    def toRep(s: String) = rep(s) { UUID.fromString }
  }
  
  private val catching = 
    scala.util.control.Exception.catching(
      classOf[NumberFormatException]
    , classOf[MalformedURLException]
    , classOf[IllegalArgumentException]
    )

  // catch and return option
  private def rep[A, B](a: A)(partial: A => B): Option[A @@ Rep[B]] =
    catching.opt(partial(a)) map (_ => Tag(a))
}

/**
 * Type class for summoning the function that can check whether the instance can be tagged with Rep
 */
trait HasRep[A, B] {
  def toRep(a: A): Option[A @@ Rep[B]]
}
