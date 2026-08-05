package one.muisnowdevs.apps.anilist.source.youranimes.converter

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull

/**
 * Reads the upstream airing minute, which is absent in the same shapes its weekday is.
 *
 * Paired with [YourAnimesWeekConverter] on purpose: a title the page has not scheduled tends to
 * carry a marker in *both* fields, so leaving this one strict would have kept the decode failing
 * for exactly the titles the nullable weekday was added to admit.
 */
object YourAnimesMinuteConverter : KSerializer<Int?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("WeekMinutes", PrimitiveKind.INT).nullable

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(
        encoder: Encoder,
        value: Int?
    ) {
        if (value == null) encoder.encodeNull() else encoder.encodeInt(value)
    }

    override fun deserialize(decoder: Decoder): Int? {
        val input = decoder as? JsonDecoder ?: error("This method can only be used by Json")

        return (input.decodeJsonElement() as? JsonPrimitive)?.intOrNull
    }
}
