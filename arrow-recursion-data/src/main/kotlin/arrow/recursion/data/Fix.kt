package arrow.recursion.data

/**
 * Type level combinator for obtaining the fixed point of a type.
 * This type is the type level encoding of primitive recursion.
 */
data class Fix<out A>(val unfix: suspend  () -> A)
