package arrow.fromkotlin

import arrow.kindedj.KatDataclass1
import arrow.kindedj.KatDataclassArrowShow
import arrow.kindedj.fromarrow.KatDataclassKindedJShow
import arrow.kindedj.toKindedJ
import io.kotest.matchers.shouldBe
import org.junit.Test

class KatDataclassTestsKotlin {
  private val kinded = KatDataclass1(0)

  @Test
  fun `Values should be convertible`() {
    KatDataclassArrowShow.show(kinded) shouldBe KatDataclassKindedJShow.INSTANCE.show(kinded.toKindedJ())
  }
}
