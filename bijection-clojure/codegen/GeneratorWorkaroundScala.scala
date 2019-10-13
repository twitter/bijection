// This is a generator to forward to the generated java methods
// as a workaround for scala bug#11770
//
// Run this generator like a script:
// scala GeneratorWorkaroundScala.scala > ../src/main/scala-2.13+/com/twitter/bijection/clojure/GenertedIFnBijections.scala

val letters = (('A' to 'Z').toList.inits.toList.reverse.tail).take(23)
def rot(l: List[Char]) = l.tail :+ l.head
val methods = letters.zipWithIndex.map { case (range, i) => s"""implicit def function${i}ToIFn[${range.mkString(", ")}]:
  Bijection[Function${i}[${rot(range).mkString(", ")}], IFn] =
    Workaround11770.function${i}ToIFn[${range.mkString(", ")}]
""" }

println("// Autogenerated code DO NOT EDIT BY HAND")
println("// Generated by bijection-clojure/codegen/GeneratorWorkaroundScala.scala")
println("package com.twitter.bijection.clojure")
println("import clojure.lang.{ AFn, IFn }")
println("import com.twitter.bijection.{ AbstractBijection, Bijection, CastInjection }")
println("\ntrait GeneratedIFnBijections {")

methods.foreach(method => {
  println(method)
  println
})
println("}")