/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */

import de.fraunhofer.isst.trend.watermarker.Watermarker
import de.fraunhofer.isst.trend.watermarker.returnTypes.Result
import de.fraunhofer.isst.trend.watermarker.watermarks.Watermark
import de.fraunhofer.isst.trend.watermarker.watermarks.toTextWatermarks
import io.kvision.core.Placement
import io.kvision.core.TooltipOptions
import io.kvision.core.Trigger
import io.kvision.core.enableTooltip
import io.kvision.form.FormMethod
import io.kvision.form.formPanel
import io.kvision.form.text.TextArea
import io.kvision.html.ButtonStyle
import io.kvision.html.button
import io.kvision.html.span
import io.kvision.modal.Alert
import io.kvision.modal.Confirm
import io.kvision.panel.HPanel
import io.kvision.panel.SimplePanel
import io.kvision.utils.em
import kotlinx.serialization.Serializable

@Serializable
data class WatermarkerExtractTextForm(
    val text: String,
)

class WatermarkTextExtractTab : SimplePanel() {
    init {
        marginTop = 1.em
        span("Extract a watermark from a text.")

        // Form to extract a watermark from text
        val extractTextFormPanel =
            formPanel<WatermarkerExtractTextForm> {
                marginTop = 1.em

                method = FormMethod.GET

                add(
                    WatermarkerExtractTextForm::text,
                    TextArea(label = "Watermarked Text &#9432;", rich = true) {
                        placeholder = "Text that includes a watermark"
                        enableTooltip(
                            TooltipOptions(
                                "A text that contain a watermark that needs to be extracted",
                                placement = Placement.BOTTOM,
                                triggers = listOf(Trigger.HOVER),
                            ),
                        )
                    },
                    required = true,
                )
            }

        extractTextFormPanel.add(
            HPanel {
                button(
                    "Extract Watermark",
                    "fas fa-arrow-down",
                    style = ButtonStyle.PRIMARY,
                ) {
                    onClick {
                        if (extractTextFormPanel.validate()) {
                            println("Starting watermark extraction process ...")
                            val watermarkedResult =
                                extractWatermark(
                                    extractTextFormPanel.getData().text,
                                ).toTextWatermarks()

                            var watermarkedStatusHtml = ""
                            var mostFrequentWatermarkHtml = ""
                            var allWatermarksListHtml = ""

                            // Success
                            if (watermarkedResult.status.isSuccess) {
                                if (watermarkedResult.value.isNullOrEmpty()) {
                                    watermarkedStatusHtml += "<div" +
                                        " class=\"alert alert-secondary\" role=\"alert\">Could " +
                                        "not find any valid watermark in the text.</div>"
                                } else {
                                    watermarkedStatusHtml += "<div class=\"alert alert-success\" " +
                                        "role=\"alert\">Successfully extracted the watermark(s)" +
                                        "!</div>"

                                    val watermarkMap =
                                        watermarkedResult.value!!.map { watermark ->
                                            watermark.text
                                        }

                                    val countedWatermarkList =
                                        watermarkMap
                                            .groupingBy { it }
                                            .eachCount()
                                    mostFrequentWatermarkHtml += "<strong>Most frequent " +
                                        "Watermark: </strong>" +
                                        countedWatermarkList.maxByOrNull {
                                            it.value
                                        }?.key + "<br /><br />"

                                    allWatermarksListHtml += "<strong>Detailed " +
                                        "Watermark List:</strong><br />"
                                    for ((key, value) in countedWatermarkList) {
                                        allWatermarksListHtml += "- $key ($value times)<br />"
                                    }
                                }
                                // Warning
                            } else if (watermarkedResult.status.isWarning) {
                                watermarkedStatusHtml += "<div class=\"alert alert-warning\" " +
                                    "role=\"alert\">Some problems occur during the " +
                                    "extraction: " + watermarkedResult.status.getMessage() +
                                    "</div>"
                                // Error
                            } else if (watermarkedResult.status.isError) {
                                watermarkedStatusHtml += "<div class=\"alert alert-danger\" " +
                                    "role=\"alert\">Fatal errors occur during the " +
                                    "extraction: " + watermarkedResult.status.getMessage() +
                                    "</div>"
                            }

                            Alert.show(
                                "Result",
                                watermarkedStatusHtml + mostFrequentWatermarkHtml +
                                    allWatermarksListHtml,
                                rich = true,
                            )
                        }
                    }
                }

                button("Clear data", "fas fa-trash", ButtonStyle.DANGER).onClick {
                    Confirm.show(
                        "Are you sure?",
                        "Do you want to clear your data?",
                        yesTitle = "Yes",
                        noTitle = "No",
                        cancelTitle = "Cancel",
                    ) {
                        extractTextFormPanel.clearData()
                    }
                }
            },
        )
    }

    /** Extracts a watermark from a [text] and returns it */
    private fun extractWatermark(text: String): Result<List<Watermark>> {
        val watermarker = Watermarker()
        return watermarker.textGetWatermarks(text, squash = false)
        /*val result = watermarker.textGetWatermarks(text)


        return if (result.isSuccess) {
            result.value!!.map { watermark ->
                watermark.watermarkContent.toByteArray().decodeToString()
            }.toString()
        } else {
            // TODO: Proper error handling
            result.toString()
        }*/
    }
}
