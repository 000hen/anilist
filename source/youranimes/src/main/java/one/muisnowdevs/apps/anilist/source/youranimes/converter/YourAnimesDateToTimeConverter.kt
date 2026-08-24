package one.muisnowdevs.apps.anilist.source.youranimes.converter

import android.util.Log
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder

object YourAnimesDateToTimeConverter : KSerializer<Int?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("OnAirTime", PrimitiveKind.STRING).nullable

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: Int?) {
        if (value == null) encoder.encodeNull()
        else {
            require(value in 0..1439)

            val hour = value / 60
            val minute = value % 60

            encoder.encodeString(
                "${hour.toString().padStart(2, '0')}:${
                    minute.toString().padStart(2, '0')
                }"
            )
        }
    }

    override fun deserialize(decoder: Decoder): Int? {
        val input = decoder as? JsonDecoder ?: error("This method can only be used by Json")
        val element = input.decodeJsonElement()
        val string = element.toString()

        if (string == "null") return null
        val (hour, minute) = string.removeSurrounding("\"", "\"")
            .split(" ")
            .getOrNull(1)
            ?.split(':') ?: return null

        return hour.toInt() * 60 + minute.toInt()
    }
}