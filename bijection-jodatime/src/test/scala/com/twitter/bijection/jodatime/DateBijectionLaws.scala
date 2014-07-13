package com.twitter.bijection.jodatime

import org.scalacheck.Properties
import org.scalacheck.Gen._
import org.scalacheck.Arbitrary
import org.scalacheck.Prop._
import com.twitter.bijection.{ Bijection, BaseProperties, ImplicitBijection }
import java.util.Date
import org.joda.time.{ DateTime, LocalDate, LocalTime, YearMonth, MonthDay }
import com.twitter.bijection._

object DateBijectionsLaws extends Properties("DateBijections") with BaseProperties with DateBijections with DateInjections {

  import Rep._

  implicit val strByte = arbitraryViaBijection[Date, String @@ Rep[Date]]

  implicit val date = arbitraryViaFn { (dtime: Long) => new DateTime(dtime) }

  implicit val localDate = arbitraryViaFn { (dtime: Long) => new LocalDate(dtime) }

  implicit val localTime = arbitraryViaFn { (dtime: Long) => new LocalTime(dtime) }

  implicit val yearMonth = arbitraryViaFn { (dtime: Long) => new YearMonth(dtime) }

  implicit val monthDay = arbitraryViaFn { (dtime: Long) => new MonthDay(dtime) }

  property("Long <=> Joda") = isBijection[Long, DateTime]

  property("Date <=> Joda") = isBijection[Date, DateTime]

  property("round trips Date -> String") = isLooseInjection[DateTime, String]

  property("round trips Joda -> Date") = isLooseInjection[DateTime, Date]

  property("round trips LocalDate -> String") = isLooseInjection[LocalDate, String]

  property("round trips LocalTime -> String") = isLooseInjection[LocalTime, String]

  property("round trips YearMonth -> String") = isLooseInjection[YearMonth, String]

  property("round trips MonthDay -> String") = isLooseInjection[MonthDay, String]

}
