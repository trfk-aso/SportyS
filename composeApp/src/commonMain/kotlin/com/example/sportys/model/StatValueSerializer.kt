package com.example.sportys.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object StatValueSerializer : KSerializer<String?> {
    override val descriptor = PrimitiveSerialDescriptor("StatValue", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String? {
        return try {
            decoder.decodeString()
        } catch (e: Exception) {
            try {
                decoder.decodeInt().toString()
            } catch (e2: Exception) {
                null
            }
        }
    }

    override fun serialize(encoder: Encoder, value: String?) {
        encoder.encodeString(value ?: "")
    }
}