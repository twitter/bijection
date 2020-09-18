package bijection

object Generator {
  val pkg = "package com.twitter.bijection"

  /* Example of the code generated:
    implicit def tuple2[A1,B1,A2,B2](implicit ba: Bijection[A1,A2], bb: Bijection[B1,B2]):
      Bijection[(A1,B1),(A2,B2)] = new AbstractBijection[(A1,B1),(A2,B2)] {
        def apply(in: (A1,B1)) = (ba(in._1), bb(in._2))
        override def invert(out: (A2,B2)) = (ba.invert(out._1), bb.invert(out._2))
      }

    // For Injection:
    implicit def tuple2[A1,B1,A2,B2](implicit ba: Injection[A1,A2], bb: Injection[B1,B2]):
      Injection[(A1,B1),(A2,B2)] = new AbstractInjection[(A1,B1),(A2,B2)] {
        def apply(in: (A1,B1)) = (ba(in._1), bb(in._2))
        def invert(out: (A2,B2)) =
          for(a <- ba.invert(out._1);
              b <- bb.invert(out._2)
              ) yield (a,b)
      }

    // Injection of Tuple* to List:
    implicit def tuple2ToList[A,B,C](implicit ba: Injection[A,C], bb: Injection[B,C]):
      Injection[(A,B),List[C]] = new AbstractInjection[(A,B),List[C]] {
        def apply(in: (A,B)) = List(ba(in._1), bb(in._2))
        def invert(out: List[C]) = out match {
          case a :: b :: Nil => for(a <- ba.invert(a); b <- bb.invert(b)) yield (a,b)
          case _ => InversionFailure.failedAttempt(out)
        }
      }
   */

  val lowerLetters = ('a' to 'z').toIndexedSeq
  val upperLetters = ('A' to 'Z').toIndexedSeq

  def bijectionParameter(i: Int, typeStr: String = "Bijection"): String = {
    val l = lowerLetters(i)
    val U = upperLetters(i)
    "b" + l + ": " + typeStr + "[" + U + "1," + U + "2]"
  }

  def singleTypeParameter(typeStr: String, i: Int): String =
    typeStr + "[" + upperLetters(i) + "]"

  def injectionParameter(fromIdx: Int, toIdx: Int, typeStr: String = "Injection"): String = {
    val l = lowerLetters(fromIdx)
    val (fromU, toU) = (upperLetters(fromIdx), upperLetters(toIdx))
    "b" + l + ": " + typeStr + "[" + fromU + "," + toU + "]"
  }

  def typeList(cnt: Int, suffix: String) =
    upperLetters.slice(0, cnt) map { l =>
      l.toString + suffix
    } mkString (",")

  def tupleBijectionType(cnt: Int, typeStr: String = "Bijection"): String =
    typeStr + "[(" + typeList(cnt, "1") + "), (" + typeList(cnt, "2") + ")]"

  def applyPart(i: Int): String = "b" + lowerLetters(i) + "(in._" + (i + 1) + ")"
  def invertPart(i: Int): String = "b" + lowerLetters(i) + ".invert(out._" + (i + 1) + ")"
  def forInvertPart(i: Int): String = lowerLetters(i) + " <- " + invertPart(i)

  def expressionTuple(cnt: Int, sep: String = ", ")(part: (Int) => String) =
    (0 until cnt) map { part(_) } mkString ("(", sep, ")")

  def applyMethod(cnt: Int) =
    "def apply(in: (" + typeList(cnt, "1") + ")) = " + expressionTuple(cnt) { applyPart _ }

  def invertMethod(cnt: Int) =
    "override def invert(out: (" + typeList(cnt, "2") + ")) = " + expressionTuple(cnt) {
      invertPart _
    }

  // Here we put it all together:
  def implicitTuple(cnt: Int): String =
    "  implicit def tuple" + cnt + "[" + typeList(cnt, "1") + "," + typeList(cnt, "2") +
      "](implicit " + ((0 until cnt) map {
        bijectionParameter(_, "ImplicitBijection")
      } mkString (", ")) + "):\n    " +
      tupleBijectionType(cnt) + " = new Abstract" + tupleBijectionType(cnt) + " {\n" +
      "      " + applyMethod(cnt) + "\n" +
      "      " + invertMethod(cnt) + "\n" +
      "    }"

  def invertInj(cnt: Int) =
    "def invert(out: (" + typeList(cnt, "2") + ")) = for" +
      expressionTuple(cnt, "; ") { forInvertPart _ } + " yield (" +
      lowerLetters.slice(0, cnt).mkString(",") + ")"

  // For the Injections:
  def implicitTupleInj(cnt: Int): String =
    "  implicit def tuple" + cnt + "[" + typeList(cnt, "1") + "," + typeList(cnt, "2") +
      "](implicit " + ((0 until cnt) map {
        bijectionParameter(_, "Injection")
      } mkString (", ")) + "):\n    " +
      tupleBijectionType(cnt, "Injection") + " = new Abstract" + tupleBijectionType(
        cnt,
        "Injection"
      ) + " {\n" +
      "      " + applyMethod(cnt) + "\n" +
      "      " + invertInj(cnt) + "\n" +
      "    }"

  def toListInjectionType(cnt: Int): String =
    "Injection[(" + typeList(cnt, "") + ")," + singleTypeParameter("List", cnt) + "]"

  def toListMethod(cnt: Int) =
    "def apply(in: (" + typeList(cnt, "") + ")) = List" + expressionTuple(cnt) { applyPart(_) }

  def fromListInvertPart(i: Int): String = {
    val letter = lowerLetters(i)
    "b" + letter + ".invert(" + letter + ")"
  }

  def forInvertFromListPart(i: Int): String = lowerLetters(i) + " <- " + fromListInvertPart(i)

  def fromListMethod(cnt: Int) = {
    val letters = lowerLetters.slice(0, cnt)
    val someCase = {
      "        case " + letters.mkString(" :: ") + " :: Nil => " + "for" +
        expressionTuple(cnt, "; ") { forInvertFromListPart(_) } + " yield (" +
        letters.mkString(",") + ")\n"
    }
    val noneCase = "        case _ => InversionFailure.failedAttempt(out)\n"
    "def invert(out: " + singleTypeParameter("List", cnt) + ") = out match {\n" +
      someCase + noneCase + "      }"
  }

  def implicitTupleToCollInj(cnt: Int): String = {
    val implicitParams: String = (0 until cnt).map { injectionParameter(_, cnt) }.mkString(", ")
    "  implicit def tuple" + cnt + "ToList[" + typeList(
      cnt + 1,
      ""
    ) + "](implicit " + implicitParams + "):\n    " +
      toListInjectionType(cnt) + " = new Abstract" + toListInjectionType(cnt) + " {\n" +
      "      " + toListMethod(cnt) + "\n" +
      "      " + fromListMethod(cnt) + "\n" +
      "    }"
  }

  def generate = {
    val b = new StringBuffer
    b.append("// Autogenerated code DO NOT EDIT BY HAND\n")
    b.append(pkg).append("\n")

    b.append("\ntrait GeneratedTupleBijections extends LowPriorityBijections {\n")
    (2 to 22).foreach { cnt => b.append(implicitTuple(cnt)).append("\n") }
    b.append("}\n")

    b.append("\ntrait GeneratedTupleCollectionInjections extends LowPriorityInjections {\n")
    (2 to 22).foreach { cnt => b.append(implicitTupleToCollInj(cnt)).append("\n") }
    b.append("}\n")

    b.append("\ntrait GeneratedTupleInjections extends GeneratedTupleCollectionInjections {\n")
    (2 to 22).foreach { cnt => b.append(implicitTupleInj(cnt)).append("\n") }
    b.append("}\n")

    b.toString
  }
}
