package de.fraunhofer.isst.trend.watermarker.helper

expect object CRC32 {
    fun checksum(bytes: List<Byte>): UInt
}
