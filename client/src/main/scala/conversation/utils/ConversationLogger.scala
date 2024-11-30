package conversation.utils

import conversation.models.{Conversation, ConversationTurn}
//import org.slf4j.LoggerFactory
import spray.json._

import java.nio.file.{Files, Path, Paths}
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ConversationLogger {
//  private val logger = LoggerFactory.getLogger(getClass)
  private val conversationsDir = "conversations"

  // Create conversations directory if it doesn't exist
  Files.createDirectories(Paths.get(conversationsDir))

  def saveConversation(conversation: Conversation): Unit = {
    try {
      val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))

      // Ensure conversations directory exists
      val dirPath = Paths.get(conversationsDir)
      if (!Files.exists(dirPath)) {
        Files.createDirectories(dirPath)
      }

      // Save as JSON
      saveFile(s"$timestamp.json", conversation.toJson.prettyPrint)

      // Save as human-readable text
      saveFile(s"$timestamp.txt", formatConversationAsText(conversation))

      println(s"Conversation saved to files: $timestamp.json and $timestamp.txt")
    } catch {
      case e: Exception =>
        println(s"Failed to save conversation: ${e.getMessage}", e)
        throw e
    }
  }

  private def saveFile(filename: String, content: String): Unit = {
    val filePath = Paths.get(conversationsDir, filename)
    Files.write(filePath, content.getBytes(StandardCharsets.UTF_8))
    println(s"Saved file: ${filePath.toAbsolutePath}")
  }

  private def formatConversationAsText(conversation: Conversation): String = {
    val sb = new StringBuilder()
    sb.append("Conversation Log\n")
    sb.append("================\n\n")

    conversation.turns.zipWithIndex.foreach { case (turn, index) =>
      sb.append(s"Turn ${index + 1}\n")
      sb.append("---------\n")
      sb.append(s"Human: ${turn.prompt}\n\n")
      sb.append(s"Assistant: ${turn.response}\n\n")
    }

    sb.toString()
  }
}