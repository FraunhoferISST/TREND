/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
package integrationTest.watermarks

import de.fraunhofer.isst.trend.watermarker.watermarks.Trendmark
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.memberProperties
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TrendmarkIntegrationTestJvm {
    @Test
    fun trendmark_tags_consistentTags() {
        check(Trendmark.TAG_SIZE == 1) {
            "Please update the test as the tag type has been modified."
        }

        val classes = Trendmark::class.sealedSubclasses
        val usedTags = Array<Boolean>(256) { false }

        for (c in classes) {
            val companionObjectInstance = c.companionObjectInstance
            assertTrue(
                companionObjectInstance != null,
                "The Trendmark class \"${c.simpleName}\" must have a companion object.",
            )

            val tagProperty =
                companionObjectInstance::class.memberProperties.filter { it.name == "TYPE_TAG" }
            assertTrue(
                tagProperty.size == 1,
                "The companion object of the Trendmark class \"${c.simpleName}\" must contain a " +
                    "\"const val TAG\".",
            )

            val tag = (tagProperty.first().call(companionObjectInstance) as UByte).toInt()
            assertFalse(usedTags[tag], "The tag $tag was used multiple times.")
            usedTags[tag] = true
        }
    }
}
