package conversation

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import conversation.config.{AppConfig, EC2Config, OllamaConfig}
import conversation.models.{Conversation, ConversationTurn, EC2Request}
import conversation.services.{EC2Service, OllamaService}
import conversation.utils.ConversationLogger
import org.scalatest.BeforeAndAfterAll
import org.scalatest.RecoverMethods.recoverToSucceededIf
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import spray.json._

import java.nio.file.{Files, Paths}
import scala.concurrent.Await
import scala.concurrent.duration._

class ConversationalSystemSpec extends AnyFlatSpec with Matchers with BeforeAndAfterAll {

  val testKit = ActorTestKit()
  implicit val system: ActorSystem[Nothing] = testKit.system
  implicit val ec = system.executionContext

  // Mock configurations
  val mockEC2Config = EC2Config(
    url = "http://localhost:8080",
    endpoint = "/test"
  )

  val mockOllamaConfig = OllamaConfig(
    host = "http://localhost:11434",
    model = "test-model",
    requestTimeoutSeconds = 30,
    temperature = 0.4,
    num_predict = 50
  )

  val mockAppConfig = AppConfig(
    ec2Config = mockEC2Config,
    ollamaConfig = mockOllamaConfig,
    maxTurns = 5
  )

  // Test configs with specific settings
  def configWithMaxTurns(turns: Int) = mockAppConfig.copy(maxTurns = turns)

  val badEC2Config = mockEC2Config.copy(url = "http://invalid-url")
  val badOllamaConfig = mockOllamaConfig.copy(host = "http://invalid-host")

  override def afterAll(): Unit = {
    testKit.shutdownTestKit()
  }

  it should "handle connection failures gracefully" in {
    val service = new EC2Service(badEC2Config)
    recoverToSucceededIf[RuntimeException] {
      service.query("test")
    }
  }

  "OllamaService" should "generate valid follow-up questions" in {
    val service = new OllamaService(mockOllamaConfig)
    val nextPrompt = service.generateNextPrompt("Some LLM response")
    nextPrompt should include("?")
    nextPrompt should not be empty
  }

  it should "provide fallback questions on error" in {
    val service = new OllamaService(badOllamaConfig)
    val fallback = service.generateNextPrompt("")
    fallback should include("Could you tell me more")
    fallback should include("?")
  }

  "ConversationLogger" should "save conversation files" in {
    val logger = new ConversationLogger()
    val conversation = Conversation(
      "test-id",
      List(ConversationTurn("test question", "test response"))
    )
    logger.saveConversation(conversation)

    // Verify directory and files exist
    Files.exists(Paths.get("conversations")) shouldBe true
    // Clean up after test
    Files.walk(Paths.get("conversations"))
      .filter(p => p.toString.contains("test-id"))
      .forEach(Files.delete)
  }

  "ConversationTurn" should "serialize to JSON correctly" in {
    val turn = ConversationTurn("test question", "test response")
    val json = turn.toJson
    json.toString should include("test question")
    json.toString should include("test response")

    // Verify deserialization
    val parsed = json.convertTo[ConversationTurn]
    parsed.prompt shouldBe turn.prompt
    parsed.response shouldBe turn.response
  }

}