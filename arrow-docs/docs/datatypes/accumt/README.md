---
layout: docs-incubator
title: AccumT
permalink: /arrow/mtl/accumt/
---

## AccumT

`AccumT` is a monad transformer, which adds accumulation capabilities to a given monad.

```kotlin:ank
import arrow.core.extensions.eval.monad.monad
import arrow.core.extensions.*
import arrow.core.*
import arrow.mtl.*

val accumT1: AccumT<String, ForEval, Int> = AccumT {
    s: String -> Eval.just("#1" toT 1)
}
val accumT2: AccumT<String, ForEval, Int> = AccumT {
    s: String -> Eval.just("#2" toT 2)
}

accumT1.flatMap(String.monoid(), Eval.monad()) {
    accumT2
}.execAccumT(Eval.monad(), "a")
```
