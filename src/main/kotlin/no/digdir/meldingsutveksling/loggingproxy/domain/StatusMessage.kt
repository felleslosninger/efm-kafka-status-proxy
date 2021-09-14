package no.digdir.meldingsutveksling.loggingproxy.domain

import com.fasterxml.jackson.databind.JsonNode
import io.micrometer.core.lang.Nullable
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

data class StatusMessage(
    val status: Status,
    val conversation_id: String,
    val message_id: String = "",
    val orgnr: String,
    val process_identifier: String,
    val document_identifier: String = "",
    val sender: String = "",
    val receiver: String = "",
    var receiver_org_number: String = "",
    var sender_org_number: String = "",
    val service_identifier: String,
    val logger_name: String = "STATUS",
    val loglevel: String = "INFO",
    val timestamp: LocalDateTime = LocalDateTime.now()
)

fun JsonNode.toStatusMessage(): StatusMessage {
    return StatusMessage(Status.valueOf(this["status"].textValue()),
        this["conversation_id"].textValue(),
        this["message_id"].textValue(),
        this["orgnr"].textValue(),
        this["process_identifier"].textValue(),
        //this["document_identifier"].textValue(),
        this["sender"].textValue(),
        this["receiver"].textValue(),
        this["receiver_org_number"].textValue(),
        this["sender_org_number"].textValue(),
        this["service_identifier"].textValue(),
        this["logger_name"].textValue(),
        this["loglevel"].textValue(),
        this["timestamp"].textValue()

    )
}

fun StatusMessage.kafkaKey(): String = "${this.message_id}-${this.status}"

