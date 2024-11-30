import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
//import ch.qos.logback.classic.{Logger, LoggerContext}
import com.typesafe.scalalogging.Logger
import conversation.config.AppConfig
import conversation.ConversationalAgent

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object Main extends App {
  // Initialize Logback context directly
//  private val loggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
  private val logger = Logger(getClass.getName)

  implicit val system: ActorSystem[Nothing] = ActorSystem(
    Behaviors.empty,
    "ConversationSystem"
  )

  implicit val ec = system.executionContext

  try {
    val config = AppConfig.load()
    val initialPrompt = args.headOption.getOrElse("Tell me about cats")
    logger.info(s"Starting conversation with initial prompt: $initialPrompt")

    val agent = new ConversationalAgent(config)
    val future = agent.runConversation(initialPrompt)

    val result = Await.result(future.transformWith {
      case Success(conversationId) =>
        logger.info(s"Conversation completed successfully. Conversation ID: $conversationId")
        Future.successful(0)

      case Failure(exception) =>
        logger.error("Conversation failed", exception)
        Future.successful(1)
    }, 10.minutes)

    System.exit(result)
  } catch {
    case e: Exception =>
      logger.error("Fatal error occurred", e)
      System.exit(1)
  } finally {
    system.terminate()
  }
}