package performanceanalysis.base

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
 * Created by Jordi on 9-3-2016.
 */
trait SpecBase extends WordSpecLike with Matchers with BeforeAndAfterAll with ScalaFutures {
}
