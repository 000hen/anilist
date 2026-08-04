package one.muisnowdevs.apps.anilist.converter

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import one.muisnowdevs.apps.anilist.source.Week

object WeekConverter : KSerializer<Week> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Week", PrimitiveKind.INT)

    override fun serialize(
        encoder: Encoder,
        value: Week
    ) {
        encoder.encodeInt(value.weekNumber)
    }

    override fun deserialize(decoder: Decoder): Week {
        return Week.fromInt(decoder.decodeInt())
            ?: error("Invalid week number ${decoder.decodeInt()}")
    }
}