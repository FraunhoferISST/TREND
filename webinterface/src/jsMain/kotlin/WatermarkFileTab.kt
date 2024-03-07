/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */

import io.kvision.form.FormMethod
import io.kvision.form.formPanel
import io.kvision.form.text.Text
import io.kvision.form.upload.BootstrapUpload
import io.kvision.html.ButtonStyle
import io.kvision.html.button
import io.kvision.html.span
import io.kvision.modal.Alert
import io.kvision.modal.Confirm
import io.kvision.panel.HPanel
import io.kvision.panel.SimplePanel
import io.kvision.types.KFile
import io.kvision.utils.em
import kotlinx.serialization.Serializable

@Serializable
data class WatermarkerFileForm(
    val watermark: String,
    val upload: List<KFile>,
)

class WatermarkFileTab : SimplePanel() {
    init {
        marginTop = 1.em
        span("Embed a custom watermark into a .txt or .zip file.")

        // Form to watermark a file
        val fileFormPanel =
            formPanel<WatermarkerFileForm> {
                marginTop = 1.em

                method = FormMethod.GET

                add(
                    WatermarkerFileForm::watermark,
                    Text(label = "Watermark") {
                        placeholder = "Enter words that should be hidden in the file"
                    },
                    required = true,
                )

                add(
                    WatermarkerFileForm::upload,
                    BootstrapUpload("/", multiple = false, label = "File") {
                        explorerTheme = true
                        dropZoneEnabled = false
                        allowedFileExtensions = setOf("txt", "zip")
                    },
                    required = true,
                )
            }

        fileFormPanel.add(
            HPanel {
                button("Add Watermark", "fas fa-tarp", style = ButtonStyle.PRIMARY) {
                    onClick {
                        if (fileFormPanel.validate()) {
                            println("Starting file watermark process ...")
                            val watermarkedFile =
                                addWatermarkToFile(
                                    fileFormPanel.getData().watermark,
                                    fileFormPanel.getData().upload.first(),
                                )
                            Alert.show("Successful", watermarkedFile)
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
                        fileFormPanel.clearData()
                    }
                }
            },
        )
    }

    /** Adds a [watermark] string to [file] and returns the watermarked file */
    fun addWatermarkToFile(
        watermark: String,
        file: KFile,
    ): String {
        // TODO: Add watermarker library
        return "Added $watermark in ${file.name}"
    }
}
