/**
 * Convert integers in the interval [0-1 BILLION] to spoken english & vice versa
 * examples -
 * six hundred ninety one million one hundred forty five thousand eight hundred twenty five is 691145825
 * six hundred ninety one million one hundred fifty eight thousand one hundred seventy is 691158170
 * six hundred ninety one million one hundred seventy thousand five hundred fifteen is 691170515
 * 2 is two
 * 23 is twenty three
 * 234 is two hundred thirty four
 *
 * Author: Krishnan Raman, kraman@twitter.com
 */

package com.twitter.bijection

case class EnglishInt(get: String) extends AnyVal

object EnglishInt {
  implicit val bijectionToInt: Bijection[Int, EnglishInt] = new AbstractBijection[Int, EnglishInt] {
    def apply(num: Int): EnglishInt = EnglishInt(toEnglish(num).get)
    override def invert(s: EnglishInt): Int = fromEnglish(s.get).get
  }

  val (t, d, k, m, g) = (10, 100, 1000, 1000 * 1000, 1000 * 1000 * 1000)
  val units = Map(0 -> "zero", 1 -> "one", 2 -> "two", 3 -> "three", 4 -> "four", 5 -> "five", 6 -> "six", 7 -> "seven", 8 -> "eight", 9 -> "nine")
  val tens = Map(t -> "ten", 20 -> "twenty", 30 -> "thirty", 40 -> "forty", 50 -> "fifty", 60 -> "sixty", 70 -> "seventy", 80 -> "eighty", 90 -> "ninety")
  val teens = Map(t -> "ten", 11 -> "eleven", 12 -> "twelve", 13 -> "thirteen", 14 -> "fourteen", 15 -> "fifteen", 16 -> "sixteen", 17 -> "seventeen", 18 -> "eighteen", 19 -> "nineteen")
  val tenmult = Map(d -> "hundred", k -> "thousand", m -> "million", g -> "billion")
  val all = units ++ tens ++ teens ++ tenmult
  val word2num: Map[String, Int] = (units ++ tens ++ teens ++ tenmult).map(kv => (kv._2, kv._1))
  val s = " "

  // a helper function that converts num of type Int to a String
  // num belongs to exactly one of several bins
  // [0,20], [20,d], [d,k], [k,m],[m,g]
  // given the bin, we divide by suitable divisor to obtain quotient & remainder
  // the quotient & remainder are converted to Enmglish recurisively
  private def toEnglish(num: Int): Option[String] = {
    num match {
      case num if num < 0 => None
      case num if num > g => None
      case num if num < 20 => Some(all(num))
      case num if num < d => divide(num, t)
      case num if num < k => divide(num, d)
      case num if num < m => divide(num, k)
      case num if num < g => divide(num, m)
      case _ => None
    }
  }

  // a helper function that recursively converts num of type Int to a string
  private def divide(num: Int, div: Int): Option[String] = {
    val (quo, rem) = (num / div, num % div)
    if (div == t) {
      Some(tens(quo * 10) + (if (rem > 0) (s + units(rem)) else ""))
    } else {
      val quoEng = toEnglish(quo)
      val remEng = toEnglish(rem)
      Some(quoEng.get + s + tenmult(div) + (if (rem > 0) (s + remEng.get) else ""))
    }
  }

  // a helper function that converts valid strings to Int, invalid to None
  private def fromEnglish(str: String): Option[Int] = {
    val list = str.split(s).toList // strip spaces
    val valid = list.map(word2num.keySet.contains).foldLeft(true)(_ && _)
    if (valid) {
      val ans = numlist2int(list.map(word2num(_)))
      if (toEnglish(ans).isDefined && (toEnglish(ans).get == str)) Some(ans) else None
    } else None
  }

  // a helper function that recursively converts List[Int] to Int
  private def numlist2int(numbers: List[Int]): Int = {
    val (id, ik, im) = (numbers.indexOf(d), numbers.indexOf(k), numbers.indexOf(m))
    val has_100 = id > -1
    val has_higher = (ik > -1 || im > -1)
    val hundred_before_higher = has_100 && has_higher && ((id < ik) || (id < im))
    if (hundred_before_higher) {

      val ilist = List(ik, im).filter(x => x != -1).filter(x => x > id)

      val ix = if (ilist.size > 1) {
        math.min(ilist(0), ilist(1))
      } else ilist(0)

      val (hprev, hnext) = numbers.splitAt(id - 1)
      val (prev, next) = hnext.splitAt(ix - id + 2)

      fold(hprev) + fold100(prev) + numlist2int(next)
    } else {
      fold(numbers)
    }
  }

  // folds List[Int] to Int
  private def fold(numbers: List[Int]): Int = {
    val res = numbers.foldLeft(0, 0) ((adderaccum: (Int, Int), b: Int) => {
      val (adder, accum) = (adderaccum._1, adderaccum._2)
      if (b == 100 || b == 1000 || b == 1000 * 1000 || b == 1000 * 1000 * 1000) {
        (0, accum + (adder * b))
      } else {
        (adder + b, accum)
      }
    })
    res._1 + res._2
  }

  // fold Lists where 100 occurs before 1000, or 1000,000
  // eg. (7, 100, 1000) = 700,000
  // eg. (2,100, 1000000) = 200,000,000
  private def fold100(numbers: List[Int]): Int = {
    val res = numbers.foldLeft(0, 0) ((adderaccum: (Int, Int), b: Int) => {
      val (adder, accum) = (adderaccum._1, adderaccum._2)
      if (b == 100) {
        (0, adder * b)
      } else if (b == 1000 || b == 1000 * 1000) {
        (0, (adder + accum) * b)
      } else
        (adder + b, accum)
    })
    res._1 + res._2
  }
}

