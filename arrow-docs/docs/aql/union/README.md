---
layout: docs-incubator
title: union
permalink: /aql/union/
---




## union

`union` joins the result of selecting different queries into a single result.

`union` over `List`

{:data-executable='true'}
```kotlin:ank
import arrow.aql.extensions.list.select.*
import arrow.aql.extensions.list.union.union
import arrow.aql.extensions.listk.select.selectAll

data class Student(val name: String, val age: Int)

val john = Student("John", 30)
val jane = Student("Jane", 32)
val jack = Student("Jack", 32)
val chris = Student("Chris", 40)

fun main(args: Array<String>) {
//sampleStart
val queryA = listOf("customer" to john, "customer" to jane).query { selectAll() }
val queryB = listOf("sales" to jack, "sales" to chris).query { selectAll() }
val result = queryA.union(queryB).value()
//sampleEnd
println(result)
}
```


`Union` works with any data type that provides an instance of `Foldable<F>` where `F` is the higher kinded representation of the data type. For example `ForOption` when targeting the `Option<A>` data type or `ForListK` when targeting the `List<A>` data type

Learn more about the `AQL` combinators

- [_select_]({{'/aql/select/' | relative_url }})
- [_from_]({{'/aql/select/' | relative_url }})
- [_where_]({{'/aql/where/' | relative_url }})
- [_groupBy_]({{'/aql/groupby/' | relative_url }})
- [_orderBy_]({{'/aql/orderby/' | relative_url }})
- [_sum_]({{'/aql/sum/' | relative_url }})
- [_union_]({{'/aql/union/' | relative_url }})

### Supported Data types

```kotlin:ank:replace
import arrow.reflect.*
import arrow.aql.*

TypeClass(Union::class).dtMarkdownList()
```




[Adapt AQL to your own _custom data types_]({{'/aql/custom/' | relative_url }})
