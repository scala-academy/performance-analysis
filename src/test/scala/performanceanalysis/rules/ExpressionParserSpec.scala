package performanceanalysis.rules

import performanceanalysis.base.SpecBase
import performanceanalysis.rules.ExpressionParser._

import scala.util.Success

class ExpressionParserSpec extends SpecBase {

  "ExpressionParser" must {

    val expressionParser = new ExpressionParser {}
    val value = "2000 ms"

    s"parse expression _ < $value" in {
      expressionParser.parse(s"_ < $value") shouldBe Success(Expression(Variable, Less, Value(value)))
    }

    s"parse expression _ <= $value" in {
      expressionParser.parse(s"_ <= $value") shouldBe Success(Expression(Variable, LessOrEqual, Value(value)))
    }

    s"parse expression _ > $value" in {
      expressionParser.parse(s"_ > $value") shouldBe Success(Expression(Variable, Greater, Value(value)))
    }

    s"parse expression _ >= $value" in {
      expressionParser.parse(s"_ >= $value") shouldBe Success(Expression(Variable, GreaterOrEquals, Value(value)))
    }

    s"parse expression _ == $value" in {
      expressionParser.parse(s"_ == $value") shouldBe Success(Expression(Variable, Equals, Value(value)))
    }

    s"wrong expression _ <> $value" in {
      expressionParser.parse(s"_ <> $value").isFailure shouldBe true
    }
  }
}
