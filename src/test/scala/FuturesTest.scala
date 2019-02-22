import org.scalatest.{FunSuite, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
class FuturesTest extends FunSuite with Matchers {

  test("futures") {
    val futureInt: Future[Int] = Future({
      Thread.sleep(60000)
      3
    })

  }

}
