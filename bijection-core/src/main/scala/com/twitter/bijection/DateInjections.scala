package com.twitter.bijection

import java.util._
import scala.util.control.Exception.allCatch

trait DateInjections extends GeneratedTupleInjections {

  implicit val date2String: Injection[Date, String] =
    new AbstractInjection[Date, String] {
      def apply(d: Date) = d.toString
      override def invert(s: String) = allCatch.opt(new DateTime(s).toDate())
    }

  implicit val date2Long: Injection[Date, Long] =
    new AbstractInjection[Date, Long] {
      def apply(d: Date) = new DateTime(d).getMillis()
      override def invert(l: Long) = allCatch.opt(new DateTime(l).toDate())
    }

  implicit val date2joda: Injection[Date, DateTime] =
    new AbstractInjection[Date, DateTime] {
      def apply(d: Date) = new DateTime(d)
      override def invert(dt: DateTime) = allCatch.opt(dt.toDate())
    }  
  

  
    implicit val joda2String: Injection[DateTime, String] =
    new AbstractInjection[DateTime, String] {
      def apply(d: DateTime) = d.toString
      override def invert(s: String) = allCatch.opt(new DateTime(s))
    }


    implicit val joda2Long: Injection[DateTime, Long] =
    new AbstractInjection[DateTime, Long] {
      def apply(d: DateTime) = d.getMillis()
      override def invert(l: Long) = allCatch.opt(new DateTime(l))
    }
  
  implicit val joda2Date: Injection[DateTime, Date] =
    new AbstractInjection[DateTime, Date] {
      def apply(d: DateTime) = d.toDate()
      override def invert(d: Date) = allCatch.opt(new DateTime(d))
    }
}