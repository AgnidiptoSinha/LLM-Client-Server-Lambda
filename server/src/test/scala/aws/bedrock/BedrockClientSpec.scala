package aws.bedrock

import aws.bedrock.BedrockClient
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import scala.util.{Success, Failure}

class BedrockClientSpec extends AnyWordSpec with Matchers with ScalaFutures {
  implicit val patience: PatienceConfig = PatienceConfig(5.seconds, 100.milliseconds)

  "BedrockClient" should {

    "return properly formatted model response" in {
      val config = ConfigFactory.load()
      val client = new BedrockClient(config)
      val result = client.generateText("What is ML?", 100)

      result match {
        case Success(response) =>
          response should include regex("(?i)machine learning")
          response should include regex("[A-Z].*[.!?]")
        case Failure(ex) =>
          fail(s"Failed to generate text: ${ex.getMessage}")
      }
    }
  }
}