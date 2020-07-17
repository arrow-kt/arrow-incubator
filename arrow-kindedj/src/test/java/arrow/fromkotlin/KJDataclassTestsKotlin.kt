package arrow.fromkotlin

import arrow.kindedj.KJDataclassArrowShow
import arrow.kindedj.fromKindedJ
import arrow.kindedj.fromkindedj.ForKJDataclass.KJDataclass1
import arrow.kindedj.fromkindedj.KJDataclassKindedJShow
import io.kotest.matchers.shouldBe
import org.junit.Test

class KJDataclassTestsKotlin {

  private val kinded = KJDataclass1(0)

  @Test
  fun `Values should be convertible`() {
    KJDataclassKindedJShow.INSTANCE.show(kinded) shouldBe KJDataclassArrowShow.show(kinded.fromKindedJ())
  }
}
