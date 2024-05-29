<!--
 Copyright (c) 2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.

 This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 that can be found in the LICENSE file.
-->

# Trendmark
The watermarker library uses bytes as the watermark content. This approach is very flexible and
allows almost any content to be inserted as a watermark. However, when extracting a watermark, it is
necessary to know how to interpret the bytes. The information needed correctly interpret the bytes
can be included in the watermark at the cost of increasing the size of the watermark. This makes it
possible to extract and interpret watermarks from unknown sources, as long as they follow a
predefined specification. The Trendmark class creates and implements such a specification. The
specification is documented below.

## Overview
Trendmark uses the first byte of a watermark as tag to indicate the format. This allows to parse and
interpret the content of the watermark correctly as long as the watermark follows this
specification. The table below lists all specified tags and explains their purpose. The tags are
counted upwards from `0` for watermarks that add additional content and downwards from `fe` for
watermarks that change the content itself (e.g. compression).

| Tag (in hex) | Class name | Meaning |
| -- | -- | -- |
| 00 | RawWatermark | raw bytes without special encoding |
| 01 | SizedWatermark | size + raw bytes |
| 02 | CRC32Watermark | CRC32 checksum + raw bytes |
| 03 | SizedCRC32Watermark | size + CRC32 checksum + raw bytes |
| 04 | SHA3256Watermark | SHA3-256 hash + raw bytes |
| 05 | SizedSHA3256Watermark | size + SHA3-256 hash + raw bytes |
| -- | -- | -- |
| fe | CompressedRawWatermark | compressed bytes |
| fd | CompressedSizedWatermark | size + compressed bytes |
| fc | CompressedCRC32Watermark | CRC32 checksum + compressed bytes |
| fb | CompressedSizedCRC32Watermark | size + CRC32 checksum + compressed bytes |
| fa | CompressedSHA3256Watermark | SHA3-256 hash + compressed bytes |
| f9 | CompressedSizedSHA3256Watermark | size + SHA3-256 hash + compressed bytes |
| -- | -- | -- |
| ff | Custom | Reserved for custom Trendmark implementations |

## Details
In the following, each watermark is specified with the text ``Lorem Ipsum`` encoded in UTF-8 bytes
(4c 6f 72 65 6d 20 69 70 73 75 6d) as example watermark content. All values are in hexadecimal
format.
The compressed variants use DEFLATE as compression algorithm (see RFC 1951).
Compressing the example watermark content results in the bytes (f3 c9 2f 4a cd 55 f0 2c 28 2e cd 05
00), which is larger because the example content is too small to be effectively compressed.
Only the content is compressed, potentially allowing to use the additional information to increase 
the watermark robustness.

### RawWatermark

| tag | raw bytes |
| -- | -- |
| 00 | 4c 6f 72 65 6d 20 69 70 73 75 6d |

### SizedWatermark
The size is calculated over the entire watermark.

| tag | size in 32 bits little-endian | raw bytes |
| -- | -- | -- |
| 01 | 10 00 00 00 | 4c 6f 72 65 6d 20 69 70 73 75 6d |

### CRC32Watermark
The [CRC32](https://en.wikipedia.org/wiki/Cyclic_redundancy_check) checksum is calculated over the
entire watermark, replacing the bytes containing the checksum with null bytes.

| tag | CRC32 checksum little-endian | raw bytes |
| -- | -- | -- |
| 02 | 87 0b 16 35 | 4c 6f 72 65 6d 20 49 70 73 75 6d |

### SizedCRC32Watermark
The size and CRC32 checksum are calculated over the entire watermark, replacing the bytes containing
the checksum with null bytes.

| tag | size in 32 bits little-endian | CRC32 checksum little-endian | raw bytes |
| -- | -- | -- | -- |
| 03 | 14 00 00 00 | 1e 85 5b 04 | 4c 6f 72 65 6d 20 49 70 73 75 6d |

### SHA3256Watermark
The [SHA3-256](https://en.wikipedia.org/wiki/SHA-3) hash is calculated over the entire watermark,
replacing the bytes containing the hash with null bytes.

| tag | SHA3-256 hash | raw bytes |
| -- | -- | -- |
| 04 | de 02 65 dd 6b 16 a0 b4 ab 05 a4 39 36 c0 73 12 4f 66 a2 aa 55 b3 9c 2b 30 b6 19 de 1c 11 c9 50 | 4c 6f 72 65 6d 20 49 70 73 75 6d |

### SizedSHA3256Watermark
The size and SHA3-256 hash are calculated over the entire watermark, replacing the bytes containing the
hash are replaced with zero-bytes.

| tag | size in 32 bits little-endian | SHA3-256 hash | raw bytes |
| -- | -- | -- | -- |
| 05 | 30 00 00 00 | f2 17 a5 ae 43 c5 70 a2 33 2b b5 90 60 23 45 da 6d 35 d3 34 95 5c 17 83 ec ec 2e 49 66 45 c9 1a | 4c 6f 72 65 6d 20 49 70 73 75 6d |

### CompressedRawWatermark
Trendmark uses DEFLATE as compression algorithm (see RFC 1951).

| tag | compressed content |
| -- | -- |
| fe | f3 c9 2f 4a cd 55 f0 2c 28 2e cd 05 00 |

### CompressedSizedWatermark
The size is calculated over the entire watermark.
Only the content is compressed, potentially allowing to use the additional information to increase 
the watermark robustness.
Trendmark uses [DEFLATE](https://en.wikipedia.org/wiki/Deflate) as compression algorithm
(see RFC 1951).

| tag | size in 32 bits little-endian | compressed bytes |
| -- | -- | -- |
| fd | 12 00 00 00 | f3 c9 2f 4a cd 55 f0 2c 28 2e cd 05 00 |

### CompressedCRC32Watermark
The CRC32 checksum is calculated over the entire watermark, replacing the bytes containing the
checksum with null bytes.
Only the content is compressed, potentially allowing to use the additional information to increase 
the watermark robustness.
Trendmark uses DEFLATE as compression algorithm (see RFC 1951).

| tag | CRC32 checksum little-endian | compressed bytes |
| -- | -- | -- |
| fc | 9d 54 46 ff | f3 c9 2f 4a cd 55 f0 2c 28 2e cd 05 00 |

### CompressedSizedCRC32Watermark
The size and CRC32 checksum are calculated over the entire watermark, replacing the bytes containing
the checksum with null bytes.
Only the content is compressed, potentially allowing to use the additional information to increase 
the watermark robustness.
Trendmark uses DEFLATE as compression algorithm (see RFC 1951).

| tag | size in 32 bits little-endian | CRC32 checksum little-endian | compressed bytes |
| -- | -- | -- | -- |
| fb | 16 00 00 00 | 13 07 a7 d2 | f3 c9 2f 4a cd 55 f0 2c 28 2e cd 05 00 |

### CompressedSHA3256Watermark
The SHA3-256 hash is calculated over the entire watermark, replacing the bytes containing the hash
with null bytes.
Only the content is compressed, potentially allowing to use the additional information to increase 
the watermark robustness.
Trendmark uses DEFLATE as compression algorithm (see RFC 1951).

| tag | SHA3-256 hash | compressed bytes |
| -- | -- | -- |
| fa | df 60 19 45 c2 77 98 5d 0e 59 cc f8 9b 27 ed 9f 9c 98 85 a5 b3 3e c7 47 fa 88 68 74 a8 ef 77 5b | f3 c9 2f 4a cd 55 f0 2c 28 2e cd 05 00 |

### CompressedSizedSHA3256Watermark
The size and SHA3-256 hash are calculated over the entire watermark, replacing the bytes containing the
hash are replaced with zero-bytes.
Only the content is compressed, potentially allowing to use the additional information to increase 
the watermark robustness.
Trendmark uses DEFLATE as compression algorithm (see RFC 1951).

| tag | size in 32 bits little-endian | SHA3-256 hash | compressed bytes |
| -- | -- | -- | -- |
| f9 | 32 00 00 00 | cc a9 b4 81 b5 3a 33 a4 b1 ee 7a e4 80 60 45 d2 66 e4 44 8a 41 d4 8d 5e c1 99 88 b2 ef 83 c8 6e | f3 c9 2f 4a cd 55 f0 2c 28 2e cd 05 00 |