package one.muisnowdevs.apps.anilist.source.youranimes.converter

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import java.time.DayOfWeek

object YourAnimesWeekConverter : KSerializer<DayOfWeek> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Week", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: DayOfWeek
    ) {
        encoder.encodeInt(value.value)
    }

    override fun deserialize(decoder: Decoder): DayOfWeek {
        val input = decoder as? JsonDecoder ?: error("This method can only be used by Json")

        val element = input.decodeJsonElement() as? JsonPrimitive ?: error("Expected primitive")
        if (element.isString) error("Expected int, got string") // TODO: This should be undetermined

        val intWeek = element.intOrNull ?: error("Unexpected value ${element.content}")
        return DayOfWeek.of((intWeek + 6) % 7 + 1)
            ?: error("Invalid week number ${decoder.decodeInt()}")
    }
}