package com.hexyoungs.pegasocks

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class PegasConfig(
    val servers: List<PegasServer>,
    val local_address: String?,
    val local_port: Int?,
    val control_port: Int?,

    val log_level: Int?,
    val ping_interval: Int?,
    var dns_servers: List<String>?,
    var acl_file: String?,
    val ssl: SSLConfig?,
    val android: AndroidConfig?,
)

@JsonClass(generateAdapter = true)
class SSLConfig (
    val verify: Boolean
)

@JsonClass(generateAdapter = true)
class AndroidConfig(
    val protect_address: String,
    val protect_port: Int,
)

@JsonClass(generateAdapter = true)
class PegasServer(
    val server_address: String,
    val server_type: String,
    val server_port: Int,
    val password: String,

    val secure: String?,
    val ssl: ServerSSLConfig?,
    val websocket: ServerWSConfig?,
)

@JsonClass(generateAdapter = true)
class ServerSSLConfig(
    val sni: String?,
)

@JsonClass(generateAdapter = true)
class ServerWSConfig(
    val path: String?,
    val hostname: String?,
)
