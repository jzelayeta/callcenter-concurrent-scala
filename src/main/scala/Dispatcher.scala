import java.util.concurrent.{BlockingQueue, ThreadLocalRandom, _}

import com.typesafe.scalalogging.LazyLogging
import rx.lang.scala._

import scala.collection.mutable.MutableList
import scala.concurrent.duration.FiniteDuration

object Dispatcher extends Observer[Agent] with LazyLogging {
  val agentsQueue: BlockingQueue[Agent] = new PriorityBlockingQueue[Agent](1, Ordering.by(_.priority))
  val incomingCalls = new LinkedBlockingQueue[Call]()
  val finishedCalls: MutableList[Call] = MutableList.empty
  implicit val pooledEc: ExecutorService = Executors.newFixedThreadPool(10)
  implicit val singleEc: ExecutorService = Executors.newSingleThreadExecutor()

  def close(): Unit = {
    Thread.sleep(1000)
    pooledEc.shutdown()
    pooledEc.awaitTermination(Long.MaxValue, TimeUnit.NANOSECONDS)
    singleEc.shutdownNow()
  }

  def apply(): Unit = {
    singleEc.execute(() => {
      while (true) {
        val agent = agentsQueue.take()
        val call = incomingCalls.take()
        agent.attendCall(call)
        pooledEc.execute(agent.attendCall(call))
      }
    })

    def getFinishedCalls: MutableList[Call] = {
      finishedCalls
    }

  }

  def addAttendants(agents: List[Agent]): Unit = agents foreach {
    agentsQueue put _
  }

  def addAttendant(agent: Agent): Boolean = agentsQueue offer agent

  def dispatchCall(call: Call): Unit = {
    incomingCalls.put(call)
  }


  override def onNext(agent: Agent): Unit = {
    logger.info(s"ATTENDANT ${agent.id} ADDED TO QUEUE ")
    agentsQueue.put(agent)
    finishedCalls += agent.someCall.get
  }
}

case class Call(id: Int, start: Long = System.currentTimeMillis()) {
  var end: Long = 0

  def stop(): Call = {
    end = System.currentTimeMillis()
    this
  }

  def duration(): FiniteDuration = {
    FiniteDuration(end - start, TimeUnit.SECONDS)
  }
}

class Agent(val id: Int, val priority: AgentPriority, var someCall: Option[Call] = None) extends Thread with LazyLogging {

  var interrupted = false

  def attendCall(call: Call): Agent = {
    someCall = Some(call)
    this
  }

  override def run(): Unit = {
    logger.info(s"AGENT WITH PRIORITY: $priority AND ID: $id SPEAKING!")
    val callDuration = ThreadLocalRandom.current.nextInt(2000, 7000)
    Thread.sleep(callDuration)
    someCall = someCall.map(_.stop())
    val agentObservable: Observable[Agent] = Observable.just(this)
    agentObservable.subscribe(Dispatcher)
  }
}

sealed trait AgentPriority extends Ordered[AgentPriority] {
  def priority(): Int

  override def compare(x: AgentPriority): Int = priority() compareTo x.priority()
}

case object Operator extends AgentPriority {
  def priority(): Int = 1
}

case object Supervisor extends AgentPriority {
  def priority(): Int = 2
}

case object Director extends AgentPriority {
  def priority(): Int = 3
}