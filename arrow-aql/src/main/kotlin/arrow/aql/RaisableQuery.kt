package arrow.aql

data class RaisableQuery<out F, A, out Z>(
  val select: Selection<A, Z>,
  val from: Source<F, A>,
  val predicate: A.() -> Boolean
)
