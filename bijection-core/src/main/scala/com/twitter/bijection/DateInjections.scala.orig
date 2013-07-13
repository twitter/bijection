package com.twitter.bijection

import java.util._
import com.github.nscala_time.time._
import com.github.nscala_time.time.Imports._
import scala.util.control.Exception.allCatch
import Bijection.build
import Inversion.{ attempt, attemptWhen }

trait DateInjections extends GeneratedTupleInjections {

  implicit val date2String: Injection[Date, String] =
    new AbstractInjection[Date, String] {
      def apply(d: Date) = d.toString
      override def invert(s: String) = attempt(s)(new DateTime(_).toDate())

    }

  implicit val joda2String: Injection[DateTime, String] =
    new AbstractInjection[DateTime, String] {
      def apply(d: DateTime) = d.toString
      override def invert(s: String) = attempt(s)(new DateTime(_))
    }

  implicit val joda2Long: Injection[DateTime, Long] =
    new AbstractInjection[DateTime, Long] {
      def apply(d: DateTime) = d.getMillis()
      override def invert(l: Long) = attempt(l)(new DateTime(_))
    }

  implicit val joda2Date: Injection[DateTime, Date] =
    new AbstractInjection[DateTime, Date] {
      def apply(d: DateTime) = d.toDate()
      override def invert(d: Date) = attempt(d)(new DateTime(_))
    }
}