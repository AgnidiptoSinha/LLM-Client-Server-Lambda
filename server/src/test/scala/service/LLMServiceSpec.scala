package service

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import service.LLMService

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import models.GenerateResponse
import org.scalatest.RecoverMethods.recoverToSucceededIf

class LLMServiceSpec extends AnyWordSpec with Matchers with ScalaFutures {
  val testKit = ActorTestKit()
  implicit val system: ActorSystem[Nothing] = testKit.system
  implicit val ec: ExecutionContext = system.executionContext
  implicit val patience: PatienceConfig = PatienceConfig(5.seconds, 100.milliseconds)

  "LLMService" should {
    val service = new LLMService(system)

    "validate input query length" in {
      val longQuery = "a" * 1000
      recoverToSucceededIf[IllegalArgumentException] {
        service.generateResponse(longQuery)
      }
    }
  }

  def afterAll(): Unit = {
    testKit.shutdownTestKit()
  }
}