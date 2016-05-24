package performanceanalysis.rules

import performanceanalysis.base.SpecBase
import performanceanalysis.rules.ExpressionParser._
import performanceanalysis.server.Protocol.ValueType

import scala.concurrent.duration.Duration


class ExpressionEvaluatorSpec extends SpecBase {

  "ExpressionEvaluator" must {

    val expressionEvaluator = new ExpressionEvaluator {}

    "evaluate expression 100 ms < 200 ms" in {
      expressionEvaluator.evaluate(Expression(Variable, Less, Value("200 ms")), Duration("100 ms"), ValueType(classOf[Duration])) shouldBe true
    }

    "evaluate expression 100 ms < 100 ms" in {
      expressionEvaluator.evaluate(Expression(Variable, Less, Value("100 ms")), Duration("100 ms"), ValueType(classOf[Duration])) shouldBe false
    }

    "evaluate expression 100 ms == 100 ms" in {
      expressionEvaluator.evaluate(Expression(Variable, Equals, Value("100 ms")), Duration("100 ms"), ValueType(classOf[Duration])) shouldBe true
    }

    "evaluate expression true == true" in {
      expressionEvaluator.evaluate(Expression(Variable, Equals, Value("true")), true, ValueType(classOf[Boolean])) shouldBe true
    }

    "evaluate expression false == true" in {
      expressionEvaluator.evaluate(Expression(Variable, Equals, Value("true")), false, ValueType(classOf[Boolean])) shouldBe false
    }

    "evaluate false not supported expression" in {
      expressionEvaluator.evaluate(Expression(Variable, Less, Value("true")), true, ValueType(classOf[Boolean])) shouldBe false
    }
  }
}
