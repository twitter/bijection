package com.twitter.bijection

/**
 * Type tag used to indicate that an instance of a type such as String
 * contains a valid representation of another type, such as Int or URL.
 */
trait Rep[A]

/**
 * Useful HasRep
 */
object Rep {
  implicit def toRepOpsEnrichment[A](a: A) = new ToRepOps(a)

  /**
   * Adds toRep[B] syntax to elements of type A if there is an implicit HasRep[A, B] in scope.
   * TODO make implicit class in 2.10
   */
  class ToRepOps[A](a: A) extends java.io.Serializable {
    def toRep[B](implicit ev: HasRep[A, B]): Option[A @@ Rep[B]] =
      ev.toRep(a)
  }
}

/**
 * Type class for summoning the function that can check whether the instance can be tagged with Rep
 */
trait HasRep[A, B] {
  def toRep(a: A): Option[A @@ Rep[B]]
}
