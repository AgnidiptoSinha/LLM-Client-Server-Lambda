package conversation

import akka.actor.typed.ActorSystem
import com.typesafe.scalalogging.Logger
import conversation.config.AppConfig
import conversation.models.{Conversation, ConversationTurn}
import conversation.services.{EC2Service, OllamaService}
import conversation.utils.ConversationLogger

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class ConversationalAgent(config: AppConfig)(implicit system: ActorSystem[_], ec: ExecutionContext) {
  private val logger = Logger(getClass.getName)
  private val ec2Service = new EC2Service(config.ec2Config)
  private val ollamaService = new OllamaService(config.ollamaConfig)
  private val conversationLogger = new ConversationLogger()

  def runConversation(initialPrompt: String): Future[String] = {
    val conversationId = UUID.randomUUID().toString
    logger.info(s"Starting new conversation with ID: $conversationId")

    def conversationLoop(
                          turns: List[ConversationTurn],
                          currentPrompt: String,
                          turnsRemaining: Int
                        ): Future[List[ConversationTurn]] = {
      if (turnsRemaining <= 0) {
        logger.info(s"Conversation $conversationId completed with ${turns.size} turns")
        Future.successful(turns)
      } else {
        for {
          response <- ec2Service.query(currentPrompt)
          turn = ConversationTurn(currentPrompt, response)
          _ = logger.debug(s"Turn ${turns.size + 1}: Prompt='$currentPrompt', Response='$response'")
          nextPrompt = ollamaService.generateNextPrompt(response)
          result <- conversationLoop(turns :+ turn, nextPrompt, turnsRemaining - 1)
        } yield result
      }
    }

    val conversationFuture = conversationLoop(List.empty, initialPrompt, config.maxTurns)

    conversationFuture.map { turns =>
      val conversation = Conversation(conversationId, turns)
      logger.info(s"Saving conversation $conversationId with ${turns.size} turns")
      conversationLogger.saveConversation(conversation)
      conversationId
    }
  }
}