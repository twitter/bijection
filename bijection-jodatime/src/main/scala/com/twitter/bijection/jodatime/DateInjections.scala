package com.twitter.bijection.jodatime

import java.util.Date
import org.joda.time.DateTime
import com.twitter.bijection.Inversion.attempt
import com.twitter.bijection.{ Injection, InversionFailure, AbstractInjection }

trait DateInjections {

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

  implicit val joda2Date: Injection[DateTime, Date] =
    new AbstractInjection[DateTime, Date] {
      def apply(d: DateTime) = d.toDate()
      override def invert(d: Date) = attempt(d)(new DateTime(_))
    }
}
