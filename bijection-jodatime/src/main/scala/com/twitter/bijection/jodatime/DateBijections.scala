package com.twitter.bijection.jodatime

import com.twitter.bijection.{Bijection, AbstractBijection}
import java.util.Date
import org.joda.time.DateTime

trait DateBijections {

  implicit val date2joda: Bijection[java.util.Date, DateTime] =
    new AbstractBijection[java.util.Date, DateTime] {
      def apply(d: java.util.Date) = new DateTime(d)
      override def invert(joda: DateTime) = joda.toDate()
    }

  implicit val joda2Long: Bijection[DateTime, Long] =
    new AbstractBijection[DateTime, Long] {
      def apply(d: DateTime) = d.getMillis()
      override def invert(l: Long) = new DateTime(l)
    }

}
