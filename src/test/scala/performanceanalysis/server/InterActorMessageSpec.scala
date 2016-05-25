package performanceanalysis.server

import performanceanalysis.base.SpecBase
import performanceanalysis.server.InterActorMessage.{Action, CheckRuleBreak, Details}

/**
  * Created by janwillem on 25/05/16.
  */
class InterActorMessageSpec extends SpecBase {
  "InterActorMessage" must {
    "correctly implement equals for CheckRuleBreak" in {
      val checkRuleBreakA = CheckRuleBreak("Check!")
      val checkRuleBreakB = CheckRuleBreak("Check!")
      checkRuleBreakA.equals(checkRuleBreakB) shouldBe (true)
    }

    "correctly implement equals for Action" in {
      val actionA = Action("Action", "Jackson")
      val actionB = Action("Michael", "Jackson")
      actionA.equals(actionB) shouldBe (false)
    }

    "correctly implements equals for Details" in {
      val detailsA = Details(List.empty)
      val detailsB = Details(List.empty)
      detailsA.equals(detailsB) shouldBe (true)
    }
  }
}
