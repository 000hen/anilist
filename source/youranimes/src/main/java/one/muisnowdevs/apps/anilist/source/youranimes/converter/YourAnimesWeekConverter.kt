package one.muisnowdevs.apps.anilist.source.youranimes.converter

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.DayOfWeek

object YourAnimesWeekConverter : KSerializer<DayOfWeek> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Week", PrimitiveKind.INT)

    override fun serialize(
        encoder: Encoder,
        value: DayOfWeek
    ) {
        encoder.encodeInt(value.value)
    }

    override fun deserialize(decoder: Decoder): DayOfWeek {
        return DayOfWeek.of((decoder.decodeInt() + 6) % 7 + 1)
            ?: error("Invalid week number ${decoder.decodeInt()}")
    }
}