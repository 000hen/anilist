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
import java.time.DayOfWeek

/**
 * Reads the upstream weekday, which is Sunday-based where [DayOfWeek] is Monday-based.
 *
 * A title whose weekday has not been announced arrives as a *string* in the same field rather than
 * as null or an absent key, so that is decoded as "no weekday" instead of the error it used to
 * raise — which took the whole season's decode down with it, not just the one title.
 */
object YourAnimesWeekConverter : KSerializer<DayOfWeek?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Week", PrimitiveKind.STRING).nullable

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(
        encoder: Encoder,
        value: DayOfWeek?
    ) {
        if (value == null) encoder.encodeNull() else encoder.encodeInt(value.value)
    }

    override fun deserialize(decoder: Decoder): DayOfWeek? {
        val input = decoder as? JsonDecoder ?: error("This method can only be used by Json")

        val element = input.decodeJsonElement() as? JsonPrimitive ?: return null

        val intWeek = element.intOrNull ?: return null
        if (intWeek !in 1..7) return null

        return DayOfWeek.of((intWeek + 6) % 7 + 1)
    }
}
