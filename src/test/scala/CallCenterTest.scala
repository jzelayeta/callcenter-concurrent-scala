import com.typesafe.scalalogging.LazyLogging
import org.awaitility.scala.AwaitilitySupport
import org.scalatest.{FunSuite, Matchers}

class CallCenterTest extends FunSuite with Matchers with LazyLogging{


  val operator1 = new Agent(1, Operator)
  val operator2 = new Agent(2, Operator)
  val supervisor1 = new Agent(3, Supervisor)
  val director = new Agent(4, Director)
  val attendants: List[Agent] = List(operator1, operator2, supervisor1, director)
  Dispatcher()
  Dispatcher.addAttendants(attendants)


  test("Dispatch one single call at first should be dispatched by an Operator") {
    val call = Call(1)
    Dispatcher.dispatchCall(call)
    Dispatcher.close()
    Dispatcher.finishedCalls.size shouldBe 1

  }


  test("Dispatch calls should be attended in correct priority") {
    val call = Call(1)
    val call2 = Call(2)
    val call3 = Call(3)
    Dispatcher.dispatchCall(call)
    Dispatcher.dispatchCall(call2)
    Dispatcher.dispatchCall(call3)
    Dispatcher.close()
    Dispatcher.finishedCalls.size shouldBe 3
  }



}
