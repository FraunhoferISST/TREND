/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package de.fraunhofer.isst.trend.watermarker.files

import de.fraunhofer.isst.trend.watermarker.helper.fromBytesLittleEndian
import de.fraunhofer.isst.trend.watermarker.helper.toBytesLittleEndian
import de.fraunhofer.isst.trend.watermarker.returnTypes.Event
import de.fraunhofer.isst.trend.watermarker.returnTypes.Result
import de.fraunhofer.isst.trend.watermarker.returnTypes.Status

/**
 * | Expected zip File Format:   | Length (bytes)       | Expected Value |
 * |-----------------------------|----------------------|----------------|
 * | local file header signature | 4                    | 0x04034b50     |
 * | version needed to extract   | 2                    |                |
 * | general purpose bit flag    | 2                    |                |
 * | compression method          | 2                    |                |
 * | last mod file time          | 2                    |                |
 * | last mod file date          | 2                    |                |
 * | crc-32                      | 4                    |                |
 * | compressed size             | 4                    |                |
 * | uncompressed size           | 4                    |                |
 * | file name length            | 2                    |                |
 * | extra field length          | 2                    |                |
 * | file name                   | [file name length]   |                |
 * | extra fields                | [extra field length] |                |
 * | unparsed                    | all remaining        |                |
 *
 * | Expected ExtraField Format: | Length (bytes) |
 * |-----------------------------|----------------|
 * | id / signature              | 2              |
 * | length                      | 2              |
 * | content                     | [length]       |
 *
 * All numbers are expected in little endian order.
 */
class ZipFile internal constructor(
    path: String?,
    val header: ZipFileHeader,
    internal val content: List<Byte>,
) : WatermarkableFile(path) {
    /** Converts the ZipFile into raw bytes */
    override fun toBytes(): List<Byte> {
        val bytes = ArrayList<Byte>()

        bytes.addAll(this.header.toBytes())
        bytes.addAll(this.content)

        return bytes
    }

    /** Checks if [this] and [other] are equal */
    override fun equals(other: Any?): Boolean {
        return when (other) {
            is ZipFile -> this.toBytes() == other.toBytes()
            else -> false
        }
    }

    override fun hashCode(): Int {
        var result = header.hashCode()
        result = 31 * result + content.hashCode()
        return result
    }

    companion object {
        fun fromBytes(
            bytes: ByteArray,
            path: String? = null,
        ): Result<ZipFile> {
            val (status, header) =
                with(ZipFileHeader.fromBytes(bytes)) {
                    status to (value ?: return into<_>())
                }

            val content = bytes.slice(header.getHeaderLength() until bytes.size)

            val zipFile = ZipFile(path, header, content)

            check(bytes.asList() == zipFile.toBytes()) {
                "Error: Original and reconstructed header differ. This should never have happened!"
            }

            return status.into(zipFile)
        }
    }
}

class ZipFileHeader internal constructor(
    internal val magicBytes: UInt,
    internal val minimumVersion: UShort,
    internal val generalPurposeBitFlags: UShort,
    internal val compressionMethod: UShort,
    internal val lastModificationTime: UShort,
    internal val lastModificationData: UShort,
    internal val crc32UncompressedData: UInt,
    internal val compressedSize: UInt,
    internal val uncompressedSize: UInt,
    internal val fileNameLength: UShort,
    internal val extraFieldOffset: UShort,
    internal var extraFieldLength: UShort,
    internal var headerLength: UShort,
    internal val fileName: String,
    internal val extraFields: MutableList<ExtraField>,
) {
    companion object {
        internal val source = ZipFileHeader::class.simpleName!!

        /** Parses the ZipFile header from [bytes] */
        fun fromBytes(bytes: ByteArray): Result<ZipFileHeader> {
            if (bytes.size < 30) {
                return NotEnoughBytesError().into<_>()
            }

            val magicBytes = UInt.fromBytesLittleEndian(bytes.slice(0 until 4))
            if (magicBytes != 0x04034b50u) {
                return InvalidMagicBytesError().into<_>()
            }

            val minimumVersion = UShort.fromBytesLittleEndian(bytes.slice(4 until 6))
            val generalPurposeBitFlags = UShort.fromBytesLittleEndian(bytes.slice(6 until 8))
            val compressionMethod = UShort.fromBytesLittleEndian(bytes.slice(8 until 10))
            val lastModificationTime = UShort.fromBytesLittleEndian(bytes.slice(10 until 12))
            val lastModificationData = UShort.fromBytesLittleEndian(bytes.slice(12 until 14))
            val crc32UncompressedData = UInt.fromBytesLittleEndian(bytes.slice(14 until 18))
            val compressedSize = UInt.fromBytesLittleEndian(bytes.slice(18 until 22))
            val uncompressedSize = UInt.fromBytesLittleEndian(bytes.slice(22 until 26))
            val fileNameLength = UShort.fromBytesLittleEndian(bytes.slice(26 until 28))
            val extraFieldOffset = (30u + fileNameLength).toUShort()
            val extraFieldLength = UShort.fromBytesLittleEndian(bytes.slice(28 until 30))
            val headerLength = (extraFieldOffset + extraFieldLength).toUShort()

            if (bytes.size < headerLength.toInt()) {
                return NotEnoughBytesError().into<_>()
            }

            val fileName =
                bytes.slice(30 until extraFieldOffset.toInt()).toByteArray()
                    .decodeToString()

            val (status, extraFields) =
                with(
                    parseExtraFields(
                        bytes.slice(extraFieldOffset.toInt() until headerLength.toInt()),
                    ),
                ) { status to (value ?: return into<_>()) }

            val header =
                ZipFileHeader(
                    magicBytes,
                    minimumVersion,
                    generalPurposeBitFlags,
                    compressionMethod,
                    lastModificationTime,
                    lastModificationData,
                    crc32UncompressedData,
                    compressedSize,
                    uncompressedSize,
                    fileNameLength,
                    extraFieldOffset,
                    extraFieldLength,
                    headerLength,
                    fileName,
                    extraFields,
                )

            return status.into(header)
        }

        /** Parses all ExtraFields in [bytes] */
        private fun parseExtraFields(bytes: List<Byte>): Result<MutableList<ExtraField>> {
            val extraFields = ArrayList<ExtraField>()
            var pointer = 0

            while (pointer + 4 <= bytes.size) {
                val id = UShort.fromBytesLittleEndian(bytes.slice(pointer until pointer + 2))
                val length =
                    UShort.fromBytesLittleEndian(bytes.slice(pointer + 2 until pointer + 4))
                pointer += 4
                val lengthInt = length.toInt()

                if (pointer + lengthInt > bytes.size) {
                    return ExtraField.NotEnoughBytesError().into<_>()
                }

                val data = bytes.slice(pointer until (pointer + lengthInt))
                pointer += lengthInt

                extraFields.add(ExtraField(id, data))
            }

            return Result.success(extraFields)
        }
    }

    data class ExtraField(val id: UShort, val data: List<Byte>) {
        // Debug helper - TODO: remove later
        override fun toString(): String {
            var rep = "Id: 0x${id.toInt().toString(16).padStart(4, '0')}, "
            rep += "Data: ["
            rep +=
                data.joinToString(separator = ", ") {
                    it.toInt().toString(16).padStart(2, '0')
                }
            rep += "]\n"
            return rep
        }

        /** Converts the ExtraField to raw bytes */
        fun toBytes(): List<Byte> {
            val bytes = ArrayList<Byte>()

            bytes.addAll(this.id.toBytesLittleEndian())
            bytes.addAll(this.data.size.toUShort().toBytesLittleEndian())
            bytes.addAll(this.data)

            return bytes
        }

        companion object {
            const val SOURCE: String = "ZipFileExtraField"
        }

        class NotEnoughBytesError : Event.Error(SOURCE) {
            /** Returns a String explaining the event */
            override fun getMessage(): String = "Ran out of data trying to parse extra fields."
        }

        class OversizedHeaderError(val size: Int) : Event.Error("$SOURCE.addExtraField") {
            /** Returns a String explaining the event */
            override fun getMessage(): String =
                "The new header size ($size Bytes) would exceed the maximum header size " +
                    "(${UShort.MAX_VALUE} Bytes)!"
        }
    }

    /** Inserts an ExtraField([id], [content]) */
    fun addExtraField(
        id: UShort,
        content: List<Byte>,
    ): Status {
        val newLength = this.extraFieldLength.toInt() + content.size + 4

        // Ensure that the header does not exceed the max size of an u16
        if (newLength > UShort.MAX_VALUE.toInt() || newLength < 0) {
            return ExtraField.OversizedHeaderError(newLength).into()
        }

        val extraField = ExtraField(id, content)
        this.extraFields.add(extraField)

        this.extraFieldLength = (this.extraFieldLength + 4u + content.size.toUInt()).toUShort()
        this.headerLength = (this.headerLength + 4u + content.size.toUInt()).toUShort()

        return Status.success()
    }

    /** Removes all ExtraFields with id = [id] and returns them */
    fun removeExtraFields(id: UShort): List<ExtraField> {
        val removedFields = ArrayList<ExtraField>()

        var i = 0
        while (i < this.extraFields.size) {
            if (this.extraFields[i].id == id) {
                val removed = this.extraFields.removeAt(i)
                this.extraFieldLength =
                    (this.extraFieldLength - 4u - removed.data.size.toUInt()).toUShort()
                removedFields.add(removed)
            } else {
                i++
            }
        }

        return removedFields
    }

    /** Converts the ZipFileHeader to raw bytes */
    fun toBytes(): List<Byte> {
        val bytes = ArrayList<Byte>()

        bytes.addAll(this.magicBytes.toBytesLittleEndian())
        bytes.addAll(this.minimumVersion.toBytesLittleEndian())
        bytes.addAll(this.generalPurposeBitFlags.toBytesLittleEndian())
        bytes.addAll(this.compressionMethod.toBytesLittleEndian())
        bytes.addAll(this.lastModificationTime.toBytesLittleEndian())
        bytes.addAll(this.lastModificationData.toBytesLittleEndian())
        bytes.addAll(this.crc32UncompressedData.toBytesLittleEndian())
        bytes.addAll(this.compressedSize.toBytesLittleEndian())
        bytes.addAll(this.uncompressedSize.toBytesLittleEndian())
        bytes.addAll(this.fileNameLength.toBytesLittleEndian())
        bytes.addAll(this.extraFieldLength.toBytesLittleEndian())
        bytes.addAll(this.fileName.encodeToByteArray().asList())

        this.extraFields.forEach { extraField -> bytes.addAll(extraField.toBytes()) }

        return bytes
    }

    /** Returns the length of the header in bytes */
    fun getHeaderLength(): Int = this.headerLength.toInt()

    class NotEnoughBytesError : Event.Error(source) {
        /** Returns a String explaining the event */
        override fun getMessage(): String = "Not enough bytes."
    }

    class InvalidMagicBytesError : Event.Error(source) {
        /** Returns a String explaining the event */
        override fun getMessage(): String = "Invalid magic bytes."
    }
}
