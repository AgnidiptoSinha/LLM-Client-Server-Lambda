package conversation.services

import com.typesafe.scalalogging.Logger
import conversation.config.OllamaConfig
import io.github.ollama4j.OllamaAPI
import io.github.ollama4j.models.OllamaResult
import io.github.ollama4j.utils.Options

import scala.jdk.CollectionConverters._

class OllamaService(config: OllamaConfig) {
  private val logger = Logger(getClass.getName)
  private val ollamaAPI = new OllamaAPI(config.host)
  ollamaAPI.setRequestTimeoutSeconds(config.requestTimeoutSeconds)

  def generateNextPrompt(previousResponse: String): String = {
    try {
      logger.debug(s"Generating next prompt based on response: $previousResponse")

      val prompt = previousResponse.stripMargin
      logger.info(s"Sending prompt to Ollama: $prompt")

      val optionsMap: java.util.Map[String, AnyRef] = Map[String, AnyRef](
        "temperature" -> java.lang.Double.valueOf(config.temperature),
        "num_predict" -> java.lang.Integer.valueOf(config.num_predict)
      ).asJava

      val options = new Options(optionsMap)
      val result: OllamaResult = ollamaAPI.generate(config.model, prompt, false, options)
      val response = result.getResponse.trim

      logger.debug(s"Generated follow-up question: $response")

      if (response.isEmpty || response == "Can you elaborate on that?") {
        logger.warn("Received default or empty response, generating fallback question")
        generateFallbackQuestion(previousResponse)
      } else {
        response
      }
    } catch {
      case e: Exception =>
        logger.error(s"Error generating next prompt: ${e.getMessage}", e)
        generateFallbackQuestion(previousResponse)
    }
  }

  private def generateFallbackQuestion(previousResponse: String): String = {
    logger.debug(s"Generating fallback question for response: $previousResponse")
    val keywords = previousResponse.split("\\W+")
      .filter(_.length > 4)
      .take(2)

    val fallbackQuestion = if (keywords.isEmpty) {
      "Could you tell me more about what you mean?"
    } else {
      s"Could you tell me more about ${keywords.mkString(" and ")}?"
    }

    logger.info(s"Generated fallback question: $fallbackQuestion")
    fallbackQuestion
  }
}