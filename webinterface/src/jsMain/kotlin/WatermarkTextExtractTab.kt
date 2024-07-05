/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */

import de.fraunhofer.isst.trend.watermarker.Watermarker
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
                    TextArea(label = "Text") {
                        placeholder = "Text that includes a watermark"
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
                            val watermarkedText =
                                extractWatermark(
                                    extractTextFormPanel.getData().text,
                                )
                            Alert.show("Successful", watermarkedText)
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
    private fun extractWatermark(text: String): String {
        val watermarker = Watermarker()
        val result = watermarker.textGetWatermarks(text)

        return if (result.isSuccess) {
            result.value!!.map { watermark ->
                watermark.watermarkContent.toByteArray().decodeToString()
            }.toString()
        } else {
            // TODO: Proper error handling
            result.toString()
        }
    }
}
