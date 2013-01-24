/**
Convert integers in the interval [0-1 BILLION] to spoken english & vice versa
examples -
six hundred ninety one million one hundred forty five thousand eight hundred twenty five is 691145825
six hundred ninety one million one hundred fifty eight thousand one hundred seventy is 691158170
six hundred ninety one million one hundred seventy thousand five hundred fifteen is 691170515
2 is two
23 is twenty three
234 is two hundred thirty four

Intention: Can operate on strings as numbers, like so
add("three", "twenty four") = "twenty seven"
mult, div etc.

Author: Krishnan Raman, kraman@twitter.com
*/

package com.twitter.bijection

trait EnglishNumeralsBijection {

  implicit val Int2String: Bijection[Int, String @@ Rep[Int]] =
    new Bijection[Int, String @@ Rep[Int]] {
      def apply(num: Int) = Tag(toEnglish(num).get)
      override def invert(s: String @@ Rep[Int]) = fromEnglish(s).get
  }

  val (t,d,k,m,g) = (10, 100,1000,1000*1000, 1000*1000*1000)
  val units = Map(0->"zero", 1->"one", 2->"two", 3->"three", 4->"four", 5->"five", 6->"six", 7->"seven", 8->"eight", 9->"nine")
  val tens = Map(t->"ten", 20->"twenty", 30->"thirty",40->"forty", 50->"fifty", 60->"sixty", 70->"seventy", 80->"eighty", 90->"ninety")
  val teens = Map(t->"ten", 11->"eleven", 12->"twelve",13->"thirteen", 14->"fourteen", 15->"fifteen", 16->"sixteen", 17->"seventeen", 18->"eighteen", 19->"nineteen")
  val tenmult = Map(d->"hundred", k->"thousand", m->"million",  g->"billion")
  val all = units ++ tens ++ teens ++ tenmult
  val word2num:Map[String,Int] = (units++tens++teens++tenmult).map( kv=> (kv._2, kv._1))
  val s = " "

  def toEnglish(num:Int):Option[String] = {
    num match {
      case num if num < 0 => None
      case num if num > g => None
      case num if num < 20 => Some(all(num))
      case num if num < d => f(num,t)
      case num if num < k => f(num,d)
      case num if num < m => f(num,k)
      case num if num < g => f(num,m)
      case _ => None
    }
  }

  def f(num:Int, div:Int):Option[String] = {
    val (quo, rem) = (num/div, num%div)
    if( div == t) {
        Some(tens(quo*10) + (if (rem>0) (s + units(rem)) else ""))
    } else {
      val quoEng = toEnglish( quo)
      val remEng = toEnglish( rem)
      Some( quoEng.get + s + tenmult(div) + (if (rem >0 ) (s + remEng.get)  else ""))
    }
  }

  def fromEnglish(str:String):Option[Int] = {
    val list = str.split(s).toList // strip spaces
    val valid = list.map(word2num.keySet.contains).foldLeft(true)(_&&_)
    if (valid) {
      val ans = numlist2int(list.map(word2num(_)))
      if( toEnglish(ans).isDefined && (toEnglish(ans).get == str)) Some(ans) else None
    } else None
  }

  def numlist2int(numbers:List[Int]):Int = {
    val (id,ik,im) = (numbers.indexOf(d),numbers.indexOf(k),numbers.indexOf(m))
    val has_100 = id > -1
    val has_higher = ( ik > -1 || im > -1 )
    val hundred_before_higher = has_100 && has_higher && ((id < ik) || (id < im))
    if (hundred_before_higher) {

        val ilist = List(ik,im).filter(x=> x!= -1).filter(x=> x> id)

        val ix = if( ilist.size > 1) {
          math.min(ilist(0),ilist(1))
        } else ilist(0)

        val (hprev,hnext) = numbers.splitAt(id-1)
        val (prev, next) = hnext.splitAt(ix-id+2)

        fold(hprev) + fold100(prev) + numlist2int(next)
    } else {
      fold(numbers)
    }
  }

  def fold(numbers:List[Int]):Int = {
    val res = numbers.foldLeft(0,0) ((adderaccum:(Int,Int), b:Int) => {
      val (adder,accum) = (adderaccum._1, adderaccum._2)
      if( b == 100 || b == 1000 || b == 1000*1000 || b == 1000*1000*1000) {
        (0,accum+(adder*b))
      } else {
        (adder+b, accum)
      }
      })
    res._1 + res._2
  }

// fold Lists where 100 occurs before 1000, or 1000,000
// eg. (7, 100, 1000) = 700,000
// eg. (2,100, 1000000) = 200,000,000
  def fold100(numbers:List[Int]):Int = {
    val res = numbers.foldLeft(0,0) ((adderaccum:(Int,Int), b:Int) => {
      val (adder,accum) = (adderaccum._1, adderaccum._2)
      if( b == 100 ) {
        (0,adder*b)
      } else if( b == 1000 || b == 1000*1000 ) {
        (0,(adder+accum)*b)
      } else
        (adder+b, accum)
      })
    res._1 + res._2
  }

  def add(a:String, b:String):Option[String] = {
    val (aa,bb) = (fromEnglish(a), fromEnglish(b))
    if( aa.isDefined && bb.isDefined) toEnglish(aa.get+bb.get) else None
  }

  def mult(a:String, b:String):Option[String] = {
    val (aa,bb) = (fromEnglish(a), fromEnglish(b))
    if( aa.isDefined && bb.isDefined) toEnglish(aa.get*bb.get) else None
  }

  def div(a:String, b:String):Option[String] = {
    val (aa,bb) = (fromEnglish(a), fromEnglish(b))
    if( aa.isDefined && bb.isDefined) toEnglish(aa.get/bb.get) else None
  }
}

/* belongs in tests
object EnglishNumeralsApp extends App{
  override def main(argv:Array[String]):Unit = {
    new {
      val x = "foo"
      } with EnglishNumerals {

        (0 to 100).foreach( i => printf("%d is %s\n", i, toEnglish(i).get))
        (100 to 10000 by 123).foreach( i => printf("%d is %s\n", i, toEnglish(i).get))
        (10000 to 1000000 by 12345).foreach( i => printf("%d is %s\n", i, toEnglish(i).get))

      printf("2 is %s\n", toEnglish(2))
      printf("23 is %s\n", toEnglish(23))
      printf("234 is %s\n", toEnglish(234))
      printf("2345 is %s\n", toEnglish(2345))
      printf("23456 is %s\n", toEnglish(23456))
      printf("234567 is %s\n", toEnglish(234567))

      (0 to 11000).foreach( i => printf("%s is %d\n", toEnglish(i).get, fromEnglish(toEnglish(i).get).get))
      (11000 to 1000*1000*1000 by 12345).foreach( i => printf("%s is %d\n", toEnglish(i).get, fromEnglish(toEnglish(i).get).get))

      printf("%s plus %s is %s\n", "twenty three", "forty nine", add( "twenty three", "forty nine" ).get)
      printf("%s times %s is %s\n", "fifteen", "six", mult("fifteen", "six" ).get)
      printf("%s divided by %s is %s\n", "fourteen", "two", div("fourteen", "two").get)
    }
  }
}
*/

