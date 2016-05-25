package performanceanalysis.rules

import performanceanalysis.rules.ExpressionParser._
import performanceanalysis.server.Protocol.ValueType

import scala.util.Try
import scala.util.parsing.combinator.RegexParsers
import scala.util.parsing.input.CharSequenceReader

object ExpressionParser {

  sealed trait Operator
  case object Less extends Operator
  case object LessOrEqual extends Operator
  case object Greater extends Operator
  case object GreaterOrEquals extends Operator
  case object Equals extends Operator

  sealed trait Operand
  case object Variable extends Operand
  case class Value(value: String) extends Operand

  case class Expression(left: Operand, operator: Operator, right: Operand) {

    def evaluate(valueVariable: Any, valueType: ValueType): Boolean =
      new ExpressionEvaluator {}.evaluate(this, valueVariable, valueType)
  }
}

trait ExpressionParser extends RegexParsers {

  val operator = "<=" ^^^ LessOrEqual | "<" ^^^ Less |
                  ">=" ^^^ GreaterOrEquals | ">" ^^^ Greater |
                  "==" ^^^ Equals

  val variable = "_" ^^^ Variable
  val value = "[_a-zA-Z0-9]+[_a-zA-Z0-9\\s]*".r ^^ { case v => Value(v) }

  val expression = variable ~ operator ~ value ^^ { case v ~ o ~ vl => Expression(v, o, vl) }

  def parse(s: String): Try[Expression] = {
    parseAll(expression, new CharSequenceReader(s)) match {
      case Success(result, _) => util.Success(result)
      case Failure(msg, next) => util.Failure(new IllegalStateException(msg))
      case NoSuccess(msg, next) => util.Failure(new IllegalStateException(msg))
    }
  }
}