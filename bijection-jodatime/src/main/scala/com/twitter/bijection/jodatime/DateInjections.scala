package com.twitter.bijection.jodatime

import java.util.Date
import org.joda.time.{ DateTime, LocalDate, LocalTime, YearMonth, MonthDay }
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

  implicit val jodaLocalDate2String: Injection[LocalDate, String] =
    new AbstractInjection[LocalDate, String] {
      def apply(d: LocalDate) = d.toString
      override def invert(s: String) = attempt(s)(LocalDate.parse(_))
    }

  implicit val jodaLocalTime2String: Injection[LocalTime, String] =
    new AbstractInjection[LocalTime, String] {
      def apply(d: LocalTime) = d.toString
      override def invert(s: String) = attempt(s)(LocalTime.parse(_))
    }

  implicit val jodaYearMonth2String: Injection[YearMonth, String] =
    new AbstractInjection[YearMonth, String] {
      def apply(d: YearMonth) = d.toString
      override def invert(s: String) = attempt(s)(YearMonth.parse(_))
    }

  implicit val jodaMonthDay2String: Injection[MonthDay, String] =
    new AbstractInjection[MonthDay, String] {
      def apply(d: MonthDay) = d.toString
      override def invert(s: String) = attempt(s)(MonthDay.parse(_))
    }
}
