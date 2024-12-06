---
title: Trendmark
---

<!--
 Copyright (c) 2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.

 This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 that can be found in the LICENSE file.
-->

# Trendmark
The watermarker library uses bytes as the watermark content. This approach is very flexible and
allows almost any content to be inserted as a watermark. However, when extracting a watermark, it is
necessary to know how to interpret the bytes. The information needed to correctly interpret the
bytes can be included in the watermark at the cost of increasing the size of the watermark. This
makes it possible to extract and interpret watermarks from unknown sources, as long as they follow a
predefined specification. The Trendmark class creates and implements such a specification. The
specification is documented below.

## Overview
Trendmark uses the first byte of a watermark as a tag composed of 8 flags to indicate the format. 
This allows to parse and interpret the content of the watermark correctly as long as the watermark 
follows this specification. The table below lists all specified tags sorted by the amount and 
positions of set flags and explains their purpose. The flags carry the following meanings:  

(1) Custom  
(2) Compressed  
(3) Sized  
(4) CRC32  
(5) SHA3-256  
(6) Unused  
(7) Unused  
(8) Unused

| Tag (in bin) | Tag (in hex) | Class name                      | Meaning                                       |
|--------------|--------------|---------------------------------|-----------------------------------------------|
| 00000000     | 00           | RawTrendmark                    | raw bytes without special encoding            |
| --           | --           | --                              | --                                            |
| 10000000     | 80           | Custom                          | Reserved for custom Trendmark implementations |
| 01000000     | 40           | CompressedRawTrendmark          | compressed bytes                              |
| 00100000     | 20           | SizedTrendmark                  | size + raw bytes                              |
| 00010000     | 10           | CRC32Trendmark                  | CRC32 checksum + raw bytes                    |
| 00001000     | 08           | SHA3256Trendmark                | SHA3-256 hash + raw bytes                     |
| 00000100     | 04           | ---Unused---                    | flag currently not in use                     |
| 00000010     | 02           | ---Unused---                    | flag currently not in use                     |
| 00000001     | 01           | ---Unused---                    | flag currently not in use                     |
| --           | --           | --                              | --                                            |
| 01100000     | 60           | CompressedSizedTrendmark        | size + compressed bytes                       |
| 01010000     | 50           | CompressedCRC32Trendmark        | CRC32 checksum + compressed bytes             |
| 01001000     | 48           | CompressedSHA3256Trendmark      | SHA3-256 hash + compressed bytes              |
| 00110000     | 30           | SizedCRC32Trendmark             | size + CRC32 checksum + raw bytes             |
| 00101000     | 28           | SizedSHA3256Trendmark           | size + SHA3-256 hash + raw bytes              |
| --           | --           | --                              | --                                            |
| 01110000     | 70           | CompressedSizedCRC32Trendmark   | size + CRC32 checksum + compressed bytes      |
| 01101000     | 68           | CompressedSizedSHA3256Trendmark | size + SHA3-256 hash + compressed bytes       |

## Details
In the following, each watermark is specified with the text ``Lorem Ipsum`` encoded in UTF-8 bytes
(4c 6f 72 65 6d 20 69 70 73 75 6d) as example watermark content. All values are in hexadecimal
format.
The compressed variants use DEFLATE as compression algorithm (see RFC 1951).
Compressing the example watermark content results in the bytes (f3 c9 2f 4a cd 55 f0 2c 28 2e cd 05
00), which is larger because the example content is too small to be effectively compressed.
Only the content is compressed, potentially allowing to use the additional information to increase 
the watermark robustness.

### RawTrendmark

| tag | raw bytes                        |
|-----|----------------------------------|
| 00  | 4c 6f 72 65 6d 20 69 70 73 75 6d |

### SizedTrendmark
The size is calculated over the entire watermark.

| tag | size in 32 bits little-endian | raw bytes                        |
|-----|-------------------------------|----------------------------------|
| 20  | 10 00 00 00                   | 4c 6f 72 65 6d 20 69 70 73 75 6d |

### CRC32Trendmark
The [CRC32](https://en.wikipedia.org/wiki/Cyclic_redundancy_check) checksum is calculated over the
entire watermark, replacing the bytes containing the checksum with null bytes.

| tag | CRC32 checksum little-endian | raw bytes                        |
|-----|------------------------------|----------------------------------|
| 10  | 7a 10 91 54                  | 4c 6f 72 65 6d 20 49 70 73 75 6d |

### SizedCRC32Trendmark
The size and CRC32 checksum are calculated over the entire watermark, replacing the bytes containing
the checksum with null bytes.

| tag | size in 32 bits little-endian | CRC32 checksum little-endian | raw bytes                        |
|-----|-------------------------------|------------------------------|----------------------------------|
| 30  | 14 00 00 00                   | fa 33 c8 51                  | 4c 6f 72 65 6d 20 49 70 73 75 6d |

### SHA3256Trendmark
The [SHA3-256](https://en.wikipedia.org/wiki/SHA-3) hash is calculated over the entire watermark,
replacing the bytes containing the hash with null bytes.

| tag | SHA3-256 hash                                                                                   | raw bytes                        |
|-----|-------------------------------------------------------------------------------------------------|----------------------------------|
| 08  | 64 02 85 b2 7b 00 49 00 8b 05 f4 f5 ad 52 fe de 18 13 b4 3d 2f 71 79 dc b4 38 9c a0 1c 15 be b0 | 4c 6f 72 65 6d 20 49 70 73 75 6d |

### SizedSHA3256Trendmark
The size and SHA3-256 hash are calculated over the entire watermark, replacing the bytes containing the
hash are replaced with zero-bytes.

| tag | size in 32 bits little-endian | SHA3-256 hash                                                                                   | raw bytes                        |
|-----|-------------------------------|-------------------------------------------------------------------------------------------------|----------------------------------|
| 28  | 30 00 00 00                   | 10 28 25 27 0b e9 43 10 cb 90 ed 27 93 1e 09 cb e6 13 ba bc f5 3b 08 fa 97 2a 1b 6b 1e e1 3b 8d | 4c 6f 72 65 6d 20 49 70 73 75 6d |

### CompressedRawTrendmark
Trendmark uses DEFLATE as compression algorithm (see RFC 1951).

| tag | compressed content                     |
|-----|----------------------------------------|
| 40  | f3 c9 2f 4a cd 55 f0 2c 28 2e cd 05 00 |

### CompressedSizedTrendmark
The size is calculated over the entire watermark.
Only the content is compressed, potentially allowing to use the additional information to increase 
the watermark robustness.
Trendmark uses [DEFLATE](https://en.wikipedia.org/wiki/Deflate) as compression algorithm
(see RFC 1951).

| tag | size in 32 bits little-endian | compressed bytes                       |
|-----|-------------------------------|----------------------------------------|
| 60  | 12 00 00 00                   | f3 c9 2f 4a cd 55 f0 2c 28 2e cd 05 00 |

### CompressedCRC32Trendmark
The CRC32 checksum is calculated over the entire watermark, replacing the bytes containing the
checksum with null bytes.
Only the content is compressed, potentially allowing to use the additional information to increase 
the watermark robustness.
Trendmark uses DEFLATE as compression algorithm (see RFC 1951).

| tag | CRC32 checksum little-endian | compressed bytes                       |
|-----|------------------------------|----------------------------------------|
| 50  | 26 73 92 10                  | f3 c9 2f 4a cd 55 f0 2c 28 2e cd 05 00 |

### CompressedSizedCRC32Trendmark
The size and CRC32 checksum are calculated over the entire watermark, replacing the bytes containing
the checksum with null bytes.
Only the content is compressed, potentially allowing to use the additional information to increase 
the watermark robustness.
Trendmark uses DEFLATE as compression algorithm (see RFC 1951).

| tag | size in 32 bits little-endian | CRC32 checksum little-endian | compressed bytes                       |
|-----|-------------------------------|------------------------------|----------------------------------------|
| 70  | 16 00 00 00                   | 9c cd 71 1d                  | f3 c9 2f 4a cd 55 f0 2c 28 2e cd 05 00 |

### CompressedSHA3256Trendmark
The SHA3-256 hash is calculated over the entire watermark, replacing the bytes containing the hash
with null bytes.
Only the content is compressed, potentially allowing to use the additional information to increase 
the watermark robustness.
Trendmark uses DEFLATE as compression algorithm (see RFC 1951).

| tag | SHA3-256 hash                                                                                   | compressed bytes                       |
|-----|-------------------------------------------------------------------------------------------------|----------------------------------------|
| 48  | 53 b6 c1 d7 0f 09 5a c9 b8 4a 52 c1 b4 85 48 f0 d6 09 88 aa 7f 78 dd fd 54 c2 21 df 8f 9f b1 29 | f3 c9 2f 4a cd 55 f0 2c 28 2e cd 05 00 |

### CompressedSizedSHA3256Trendmark
The size and SHA3-256 hash are calculated over the entire watermark, replacing the bytes containing the
hash are replaced with zero-bytes.
Only the content is compressed, potentially allowing to use the additional information to increase 
the watermark robustness.
Trendmark uses DEFLATE as compression algorithm (see RFC 1951).

| tag | size in 32 bits little-endian | SHA3-256 hash                                                                                   | compressed bytes                       |
|-----|-------------------------------|-------------------------------------------------------------------------------------------------|----------------------------------------|
| 68  | 32 00 00 00                   | df bc 29 21 6d 94 3d 73 6b 73 19 86 4c a8 b7 58 00 23 7a 2e 53 ba 6c ea 49 4f d0 8e f9 47 9d 1b | f3 c9 2f 4a cd 55 f0 2c 28 2e cd 05 00 |