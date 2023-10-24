package com.netsensia.video.config

import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("openai")
class NetsensiaConfig {
    lateinit var apiKey: String
}
