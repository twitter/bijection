// Run this generator like a script:
// scala BufferableGenerator.scala > ../src/main/scala/com/twitter/bijection/GeneratedTupleBufferable.scala
val pkg = "package com.twitter.bijection"

/* Example of the code generated:
  implicit def tuple2[A,B](implicit ba: Bufferable[A], bb: Bufferable[B]): Bufferable[(A,B)] =
    new Bufferable[(A,B)] {
      def put(bytebuf: ByteBuffer, tup: (A,B)) = {
        var nextBb = bytebuf
        nextBb = reallocatingPut(nextBb) { ba.put(_, tup._1) }
        nextBb = reallocatingPut(nextBb) { bb.put(_, tup._2) }
        nextBb
      }
      def get(bytebuf: ByteBuffer) = {
        val a = ba.get(bytebuf)
        val b = bb.get(bytebuf)
        (a, b)
      }
    }
*/

val lowerLetters = ('a' to 'z').toIndexedSeq
val upperLetters = ('A' to 'Z').toIndexedSeq

def bufferableParam(idx: Int) = "b" + lowerLetters(idx) + ": Bufferable[" + upperLetters(idx) + "]"

def typeList(cnt: Int) =
  upperLetters.slice(0, cnt) map { _.toString } mkString(",")

def parensTypeList(cnt: Int) = "(" + typeList(cnt) + ")"

def reallocatingPut(idx: Int) =
  "nextBb = reallocatingPut(nextBb) { b" + lowerLetters(idx) + ".put(_, tup._" + (idx+1) +") }"

def bufferGet(idx: Int) =
  "val " + lowerLetters(idx) + " = b" + lowerLetters(idx) + ".get(bytebuf)"

def bufferableType(idx: Int) = "Bufferable[" + upperLetters(idx) + "]"

// Here we put it all together:
def implicitTuple(cnt: Int): String =
"  implicit def tuple" + cnt + "[" + typeList(cnt) + "](implicit " +
    ((0 until cnt) map { bufferableParam(_) } mkString(", ") ) + "):\n" +
"    Bufferable[" + parensTypeList(cnt) + "] = new Bufferable[" + parensTypeList(cnt) +"] {\n" +
"      def put(bytebuf: ByteBuffer, tup: "+ parensTypeList(cnt) +") = {\n" +
"        var nextBb = bytebuf\n" +
"        " + ((0 until cnt) map { reallocatingPut(_) }).mkString("","\n        ","\n") +
"        nextBb\n" +
"      }\n"+
"      def get(bytebuf: ByteBuffer) = {\n" +
"        " + ((0 until cnt) map { bufferGet(_) }).mkString("","\n        ","\n") +
"        " +(0 until cnt).map { lowerLetters(_) }.mkString("(",", ",")") + "\n" +
"      }\n" +
"    }"

println("// Autogenerated code DO NOT EDIT BY HAND")
println(pkg)
println("import Bufferable.reallocatingPut")
println("import java.nio.ByteBuffer")
println("\ntrait GeneratedTupleBufferable {")
(2 to 22).foreach { cnt => println(implicitTuple(cnt)) }
println("}")
