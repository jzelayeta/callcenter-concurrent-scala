import java.util
import java.util.UUID
import java.util.concurrent.{BlockingQueue, PriorityBlockingQueue}
import java.util.logging.Logger

import org.scalatest.{FunSuite, Matchers}

class PriorityTest extends FunSuite with Matchers {

  test("Attendant Priority") {
    Director.compare(Operator) shouldBe 1
    Director.compare(Supervisor) shouldBe 1
    Operator.compare(Supervisor) shouldBe -1
  }

  test("List of Attendant Priority sorted ASC") {
    val list = List(Director, Operator, Supervisor)
    list.sortWith(_ < _) shouldBe List(Operator, Supervisor, Director)
  }

  test("Priority Queue") {
    val q = new PriorityBlockingQueue[Agent](6, Ordering.by(_.priority))
    val director = new Agent(1, Director)
    val operator1 = new Agent(2, Operator)
    val operator2 =  new Agent(3, Operator)
    val supervisor1 = new Agent(4, Supervisor)
    val supervisor2 = new Agent(5, Supervisor)
    q.put(operator1)
    q.put(director)
    q.put(supervisor1)
    q.put(operator2)
    q.put(supervisor2)

   q.take().priority shouldBe operator1.priority
   q.take().priority shouldBe operator2.priority
   q.take().priority shouldBe supervisor1.priority
   q.take().priority shouldBe supervisor2.priority
   q.take().priority shouldBe director.priority
  }

}
