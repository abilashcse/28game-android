package com.agames.thuruppugulan.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

actual fun platformHttpEngine(): HttpClientEngineFactory<*> = Darwin
