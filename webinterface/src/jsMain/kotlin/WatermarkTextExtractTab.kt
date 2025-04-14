/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */

import de.fraunhofer.isst.innamark.watermarker.Watermarker
import de.fraunhofer.isst.innamark.watermarker.fileWatermarker.DefaultTranscoding
import de.fraunhofer.isst.innamark.watermarker.helper.toUnicodeRepresentation
import de.fraunhofer.isst.innamark.watermarker.watermarks.TextWatermark
import de.fraunhofer.isst.innamark.watermarker.watermarks.Watermark
import de.fraunhofer.isst.innamark.watermarker.watermarks.toTextWatermarks
import de.fraunhofer.isst.innamark.watermarker.returnTypes.Result
import io.kvision.collapse.collapse
import io.kvision.collapse.forCollapse
import io.kvision.core.Placement
import io.kvision.core.TooltipOptions
import io.kvision.core.Trigger
import io.kvision.core.enableTooltip
import io.kvision.form.FormMethod
import io.kvision.form.formPanel
import io.kvision.form.text.TextArea
import io.kvision.html.Button
import io.kvision.html.ButtonStyle
import io.kvision.html.br
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.html.li
import io.kvision.html.p
import io.kvision.html.span
import io.kvision.html.strong
import io.kvision.html.ul
import io.kvision.modal.Confirm
import io.kvision.modal.Modal
import io.kvision.panel.HPanel
import io.kvision.panel.SimplePanel
import io.kvision.panel.simplePanel
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

                            val modal = Modal("Result")

                            // Success
                            if (watermarkedResult.status.isSuccess) {
                                if (watermarkedResult.value.isNullOrEmpty()) {
                                    modal.add(
                                        div(
                                            "Could not find any valid watermark in the " +
                                                "text.",
                                            className = "alert alert-secondary",
                                        ),
                                    )
                                } else {
                                    modal.add(
                                        div(
                                            "Successfully extracted the watermark(s)!",
                                            className = "alert alert-success",
                                        ),
                                    )

                                    val countedWatermarkList =
                                        getWatermarkStringList(watermarkedResult)
                                            .groupingBy { it }
                                            .eachCount()

                                    // Most frequent watermark
                                    modal.add(
                                        span(
                                            "<strong>Most frequent " +
                                                "watermark: </strong>" +
                                                countedWatermarkList.maxByOrNull {
                                                    it.value
                                                }?.key + "<br /><br />",
                                            rich = true,
                                        ),
                                    )

                                    // Details section
                                    val watermarkDetailsPanel =
                                        simplePanel {
                                            button(
                                                "More details",
                                                style = ButtonStyle.SECONDARY,
                                            ).forCollapse("watermark-details")

                                            collapse("watermark-details") {
                                                // Print all watermarks
                                                strong("Detailed watermark list:")
                                                ul {
                                                    for ((key, value) in countedWatermarkList) {
                                                        li("$key ($value times)")
                                                    }
                                                }
                                                br()

                                                // Show watermarking type
                                                strong("Watermarking type(s): ")
                                                p(
                                                    watermarkedResult.value!!.map { watermark ->
                                                        watermark.finish().getSource()
                                                    }.toSet().toString(),
                                                )

                                                // Show input text with hidden chars
                                                strong(
                                                    "Raw data with hidden alphabet chars:",
                                                )
                                                br()
                                                span(
                                                    showWatermarkChars(
                                                        extractTextFormPanel.getData().text,
                                                    ),
                                                    rich = true,
                                                    className = "break-all",
                                                )
                                            }
                                        }
                                    modal.add(watermarkDetailsPanel)
                                }
                                // Warning
                            } else if (watermarkedResult.isWarning) {
                                modal.add(
                                    div(
                                        "Some problems occur during the extraction: " +
                                            watermarkedResult.getMessage(),
                                        className = "alert alert-warning",
                                    ),
                                )
                                modal.add(strong("Extracted Data: "))
                                modal.add(p(getWatermarkStringList(watermarkedResult).toString()))
                                // Error
                            } else if (watermarkedResult.isError) {
                                modal.add(
                                    div(
                                        "An error occurs during the extraction: " +
                                            watermarkedResult.getMessage(),
                                        className = "alert alert-danger",
                                    ),
                                )
                            }

                            modal.addButton(
                                Button("Close") {
                                    onClick {
                                        modal.hide()
                                    }
                                },
                            )
                            modal.show()
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
    }

    /**
     * Replaces all whitespaces of the transcoding alphabet of the watermarking library in
     * [watermarkedText] with its Unicode representation. [html] defines if the result is a
     * styled HTML string (true) or a plain text without formatting (false).
     */
    private fun showWatermarkChars(
        watermarkedText: String,
        html: Boolean = true,
    ): String {
        val alphabet = DefaultTranscoding.alphabet + DefaultTranscoding.SEPARATOR_CHAR
        var resultText = watermarkedText
        var className: String

        for (char in alphabet) {
            if (html) {
                className =
                    if (char == DefaultTranscoding.SEPARATOR_CHAR) {
                        "separator-highlight"
                    } else {
                        "whitespace-highlight"
                    }

                resultText =
                    resultText.replace(
                        char.toString(),
                        "<span class=\"$className\">" +
                            "${char.toUnicodeRepresentation()}</span>",
                    )
            } else {
                resultText = resultText.replace(char.toString(), char.toUnicodeRepresentation())
            }
        }
        return resultText
    }

    /** Creates a list of Strings based on a [watermarkedResult] */
    private fun getWatermarkStringList(watermarkedResult: Result<List<TextWatermark>>) =
        watermarkedResult.value!!.map { watermark ->
            watermark.text
        }
}
