package performanceanalysis.rules

import performanceanalysis.rules.ExpressionParser._
import performanceanalysis.server.Protocol.ValueType
import performanceanalysis.server._

trait ExpressionEvaluator {

  def evaluate(logicalExpression: Expression, valueVariable: Any, valueType: ValueType): Boolean = logicalExpression match {

    case Expression(_, operator, Value(value) ) =>
      val valueVarExpression = value.toType(valueType)
      (valueVariable, operator, valueVarExpression) match {
        case (v1: Ordered[Any] @unchecked, Less, v2: Ordered[Any] @unchecked) => v1 < v2
        case (v1: Ordered[Any] @unchecked, LessOrEqual, v2: Ordered[Any] @unchecked)  => v1 <= v2
        case (v1: Ordered[Any] @unchecked, Greater, v2: Ordered[Any] @unchecked) => v1 > v2
        case (v1: Ordered[Any] @unchecked, GreaterOrEquals, v2: Ordered[Any] @unchecked) => v1 >= v2
        case (v1: Any, Equals, v2: Any) => v1 == v2
        case _ => false
      }
    case _ => false
  }

}
