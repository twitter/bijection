package com.twitter.bijection

import java.util._
import com.github.nscala_time.time.Imports._

trait DateBijections extends GeneratedTupleBijections {

  implicit val date2Long: Bijection[java.util.Date, Long] =
    new AbstractBijection[java.util.Date, Long] {
      def apply(d: java.util.Date) = d.getTime
      override def invert(l: Long) = new java.util.Date(l)
    }

  implicit val date2String: Bijection[java.util.Date, String] =
    new AbstractBijection[java.util.Date, String] {
      def apply(d: java.util.Date) = d.toString()
      override def invert(s: String) = new DateTime(s).toDate()

    }

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

  implicit val joda2String: Bijection[DateTime, String] =
    new AbstractBijection[DateTime, String] {
      def apply(d: DateTime) = d.toString()
      override def invert(s: String) = new DateTime(s)
    }

  implicit val joda2Date: Bijection[DateTime, java.util.Date] =
    new AbstractBijection[DateTime, java.util.Date] {
      def apply(d: DateTime) = d.toDate()
      override def invert(dt: java.util.Date) = new DateTime(dt)
    }

}