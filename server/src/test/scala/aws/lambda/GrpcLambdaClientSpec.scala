package aws.lambda

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import aws.lambda.GrpcLambdaClient
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import com.typesafe.config.ConfigFactory
import org.scalatest.RecoverMethods.recoverToSucceededIf

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class GrpcLambdaClientSpec extends AnyWordSpec with Matchers with ScalaFutures {
  val testKit = ActorTestKit()
  implicit val system: ActorSystem[Nothing] = testKit.system
  implicit val patience: PatienceConfig = PatienceConfig(5.seconds, 100.milliseconds)

  "GrpcLambdaClient" should {

    "handle Lambda invocation errors gracefully" in {
      val invalidConfig = ConfigFactory.parseString("""
        llm.aws.lambda.api-gateway-url = "http://invalid-url"
      """)

      val client = new GrpcLambdaClient(invalidConfig, system)
      recoverToSucceededIf[RuntimeException] {
        client.invokeLambda("test prompt", 100)
      }
    }
  }

  def afterAll(): Unit = {
    testKit.shutdownTestKit()
  }
}