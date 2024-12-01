package config

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import com.typesafe.config.ConfigFactory

class ConfigurationSpec extends AnyWordSpec with Matchers {
  "Application Configuration" should {
    val config = ConfigFactory.load()

    "load server configuration correctly" in {
      config.getString("llm.server.host") shouldBe "0.0.0.0"
      config.getInt("llm.server.port") shouldBe 8000
    }

    "load AWS configuration correctly" in {
      config.getString("aws.region") shouldBe "us-east-1"
      config.getString("llm.aws.lambda.function-name") shouldBe "Bedrock-LLM"
    }

    "load Akka configuration correctly" in {
      val akkaConfig = config.getConfig("akka.http.server")
      akkaConfig.getString("interface") shouldBe "0.0.0.0"
      akkaConfig.getDuration("request-timeout").getSeconds shouldBe 120
    }

  }
}