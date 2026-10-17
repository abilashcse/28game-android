package com.agames.thuruppugulan.network

import io.ktor.client.engine.HttpClientEngineFactory

expect fun platformHttpEngine(): HttpClientEngineFactory<*>
