package com.netsensia.video.controllers

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.netsensia.video.config.NetsensiaConfig
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import io.micronaut.http.HttpResponse
import kotlinx.coroutines.runBlocking

data class VideoIdeaInput(
    val title: String,
    val description: String,
    val videoLength: Int,
    val numImages: Int
)

data class GeneratedContent(
    val script: String,
    val imageDescriptions: List<String>
)

@Controller("/generate")
class GenerationController(private val netsensiaConfig: NetsensiaConfig) {

    val config = OpenAIConfig(
        token = netsensiaConfig.apiKey
        // other configurations...
    )
    val openAI = OpenAI(config)

    @Post("/video-content")
    fun generateVideoContent(@Body input: VideoIdeaInput): HttpResponse<GeneratedContent> = runBlocking {
        // Construct the prompt for OpenAI based on the input
        val prompt = buildString {
            appendLine("Title: ${input.title}")
            appendLine("Description: ${input.description}")
            appendLine("Approximate Video Length: ${input.videoLength} minutes")
            appendLine("Number of Images: ${input.numImages}")
            appendLine("Generate a script for the video and descriptions for ${input.numImages} images:")
        }

        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-4"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = "You are a helpful assistant."
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = prompt
                )
            )
        )

        // Make the request to OpenAI
        val completion: ChatCompletion = openAI.chatCompletion(chatCompletionRequest)

        // Process the response from OpenAI
        // Assume the response contains a single message with the script and image descriptions
        val generatedText = completion.choices.first().message.content ?: ""
        val splitIndex = generatedText.indexOf("\n", generatedText.length / 2)
        val script = generatedText.substring(0, splitIndex).trim()
        val imageDescriptions = generatedText.substring(splitIndex).trim().split("\n")

        val generatedContent = GeneratedContent(script, imageDescriptions)
        return@runBlocking HttpResponse.ok(generatedContent)
    }

}