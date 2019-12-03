package com.twitter.bijection.jodatime

import org.scalacheck.{Arbitrary, Gen}

import com.twitter.bijection.BaseProperties
import java.util.Date
import org.joda.time.{DateTime, LocalDate, LocalTime, YearMonth, MonthDay}
import com.twitter.bijection._

class DateBijectionsLaws
    extends CheckProperties
    with BaseProperties
    with DateBijections
    with DateInjections {
  implicit val strByte = arbitraryViaBijection[Date, String @@ Rep[Date]]

  /**
    * If we generate times too close to the Max, timezone issues will push us over
    */
  case class Timestamp(ts: Long)
  implicit val arbtimeStamp: Arbitrary[Timestamp] =
    Arbitrary(Gen.choose(Long.MinValue / 4, Long.MaxValue / 4).map(Timestamp(_)))

  implicit val date = arbitraryViaFn { (dtime: Timestamp) =>
    new DateTime(dtime.ts)
  }

  implicit val localDate = arbitraryViaFn { (dtime: Timestamp) =>
    new LocalDate(dtime.ts)
  }

  implicit val localTime = arbitraryViaFn { (dtime: Timestamp) =>
    new LocalTime(dtime.ts)
  }

  implicit val yearMonth = arbitraryViaFn { (dtime: Timestamp) =>
    new YearMonth(dtime.ts)
  }

  implicit val monthDay = arbitraryViaFn { (dtime: Timestamp) =>
    new MonthDay(dtime.ts)
  }

  property("Long <=> Joda") {
    isBijection[Long, DateTime]
  }

  property("Date <=> Joda") {
    isBijection[Date, DateTime]
  }

  property("round trips Date -> String") {
    isLooseInjection[DateTime, String]
  }

  property("round trips Joda -> Date") {
    isLooseInjection[DateTime, Date]
  }

  property("round trips LocalDate -> String") {
    isLooseInjection[LocalDate, String]
  }

  property("round trips LocalTime -> String") {
    isLooseInjection[LocalTime, String]
  }

  property("round trips YearMonth -> String") {
    isLooseInjection[YearMonth, String]
  }

  property("round trips MonthDay -> String") {
    isLooseInjection[MonthDay, String]
  }
}
