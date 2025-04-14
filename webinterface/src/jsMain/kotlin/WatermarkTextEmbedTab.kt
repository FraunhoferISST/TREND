/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */

import de.fraunhofer.isst.innamark.watermarker.Watermarker
import de.fraunhofer.isst.innamark.watermarker.fileWatermarker.TextWatermarker
import de.fraunhofer.isst.innamark.watermarker.returnTypes.Result
import de.fraunhofer.isst.innamark.watermarker.watermarks.TextWatermark
import io.kvision.core.FontWeight
import io.kvision.core.Placement
import io.kvision.core.TooltipOptions
import io.kvision.core.Trigger
import io.kvision.core.enableTooltip
import io.kvision.core.onInput
import io.kvision.form.FormMethod
import io.kvision.form.check.CheckBox
import io.kvision.form.formPanel
import io.kvision.form.text.Text
import io.kvision.form.text.TextArea
import io.kvision.html.Button
import io.kvision.html.ButtonStyle
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.html.span
import io.kvision.i18n.tr
import io.kvision.modal.Confirm
import io.kvision.modal.Modal
import io.kvision.panel.HPanel
import io.kvision.panel.SimplePanel
import io.kvision.panel.fieldsetPanel
import io.kvision.panel.hPanel
import io.kvision.progress.Progress
import io.kvision.progress.progressNumeric
import io.kvision.state.ObservableValue
import io.kvision.state.bind
import io.kvision.toast.Toast
import io.kvision.utils.em
import io.kvision.utils.px
import kotlinx.browser.window
import kotlinx.serialization.Serializable
import kotlin.math.round

@Serializable
data class WatermarkerTextForm(
    val watermark: String,
    val text: String,
    val innamarkCompressed: Boolean,
    val innamarkSized: Boolean,
    val innamarkCRC32: Boolean,
    val innamarkSHA3256: Boolean,
)

class WatermarkTextEmbedTab : SimplePanel() {
    private val textWatermarker = TextWatermarker.default()

    // Input fields
    private val watermarkerInput =
        Text(label = "Watermark &#9432;", rich = true) {
            placeholder = "Enter words that should be hidden in the text"
            enableTooltip(
                TooltipOptions(
                    title = "The watermark is a text that will be hidden in the cover text",
                    placement = Placement.BOTTOM,
                    triggers = listOf(Trigger.HOVER),
                ),
            )
        }
    private val coverTextInput =
        TextArea(label = "Cover Text &#9432;", rich = true) {
            placeholder = "Text that should be watermarked"
            enableTooltip(
                TooltipOptions(
                    "The cover text will be enriched by the watermark above in a hidden way",
                    placement = Placement.BOTTOM,
                    triggers = listOf(Trigger.HOVER),
                ),
            )
        }

    private val compressedCheckBox =
        CheckBox(label = "Compressed &#9432;", rich = true) {
            paddingRight = 20.px
            enableTooltip(
                TooltipOptions(
                    title =
                        "Compress the watermark content before embedding into" +
                            " the text. Good for longer watermarks.",
                    triggers = listOf(Trigger.HOVER),
                ),
            )
        }

    private val sizedCheckBox =
        CheckBox(label = "Sized &#9432;", rich = true) {
            paddingRight = 20.px
            enableTooltip(
                TooltipOptions(
                    title =
                        "Include the size of the watermark for additional " +
                            "verification. Needs a longer cover text content.",
                    triggers = listOf(Trigger.HOVER),
                ),
            )
        }

    @Suppress("ktlint:standard:property-naming")
    private val CRC32CheckBox =
        CheckBox(label = "CRC32 &#9432;", rich = true) {
            paddingRight = 20.px
            enableTooltip(
                TooltipOptions(
                    title =
                        "Uses the Cyclic Redundancy Check (CRC) " +
                            "error-detecting code for additional robustness. " +
                            "Needs a longer cover text content.",
                    triggers = listOf(Trigger.HOVER),
                ),
            )
        }

    @Suppress("ktlint:standard:property-naming")
    private val SHA3256CheckBox =
        CheckBox(label = "SHA3-256 &#9432;", rich = true) {
            paddingRight = 20.px
            enableTooltip(
                TooltipOptions(
                    title =
                        "Include a SHA3-256 hash of the watermark for " +
                            "additional verification. Needs a longer cover text " +
                            "content.",
                    triggers = listOf(Trigger.HOVER),
                ),
            )
        }

    // Progress bar
    private var min: Int = -1
    private val capacityObservable = ObservableValue(-1)
    private val progressBar =
        Progress(-1, 0) {
            marginBottom = 0.px
            width = 300.px
            progressNumeric(this.bounds.value.min) {
                striped = true
            }
            enableTooltip(
                TooltipOptions(
                    "The Percentage gives an overview if the watermark will fit in the " +
                        "cover text: <br />- >=100 %: The watermark fits at least one time in " +
                        "the cover text <br />- <100%: The watermark doesn't fit into the cover " +
                        "text. Try to increase the length of the cover text or decrease the " +
                        "length of the watermark",
                    rich = true,
                    placement = Placement.BOTTOM,
                    triggers = listOf(Trigger.HOVER),
                ),
            )
        }
    private val watermarkCapacityLowHint =
        div(
            "The watermark is too long for your cover text. " +
                "Try to increase the cover text length (with more whitespaces) or decrease the " +
                "length of the watermark so that it fits in.",
            className = "alert alert-primary",
        ) {
            marginTop = 1.em
            hide()
        }
    private val watermarkCapacitySuccessHint =
        div(
            "Great, your watermark fits into the cover " +
                "text! Click the \"Add Watermark\" button, copy the result and use the " +
                "watermarked text.",
            className = "alert alert-success",
        ) {
            marginTop = 1.em
            hide()
        }

    // Submit button
    private val submitButton =
        Button(
            "Add Watermark",
            "fas fa-tarp",
            style = ButtonStyle.PRIMARY,
            disabled = true,
        )

    init {
        marginTop = 1.em
        span(
            "Embed a watermark into a cover text. Keep in mind that the cover text " +
                "needs a specific length (number of whitespaces), depending on the length of " +
                "your watermark. The progress bar will indicate if the watermark fits into the " +
                "cover text.",
        )

        // Form to watermark a text/string
        val textFormPanel =
            formPanel<WatermarkerTextForm> {
                marginTop = 1.em

                method = FormMethod.GET

                add(
                    WatermarkerTextForm::watermark,
                    watermarkerInput,
                    required = true,
                )
                add(
                    WatermarkerTextForm::text,
                    coverTextInput,
                    required = true,
                )
                fieldsetPanel(tr("Advanced Settings")) {
                    hPanel {
                        div("Innamark: &#9432;", rich = true) {
                            paddingRight = 10.px
                            fontWeight = FontWeight.BOLD
                            enableTooltip(
                                TooltipOptions(
                                    title =
                                        "A special type of watermarks using additional " +
                                            "features.",
                                    triggers = listOf(Trigger.HOVER),
                                ),
                            )
                        }

                        add(compressedCheckBox.bind(WatermarkerTextForm::innamarkCompressed))

                        add(sizedCheckBox.bind(WatermarkerTextForm::innamarkSized))

                        add(CRC32CheckBox.bind(WatermarkerTextForm::innamarkCRC32))

                        add(SHA3256CheckBox.bind(WatermarkerTextForm::innamarkSHA3256))
                    }
                }
                add(progressBar)
                span().bind(capacityObservable) {
                    val percentage =
                        round(progressBar.getFirstProgressBar()?.width?.first?.toDouble() ?: 0.0)

                    +"$percentage %"
                }
            }

        // Check if watermark fits into the cover text
        watermarkerInput.onInput { updateCapacity() }
        coverTextInput.onInput { updateCapacity() }
        compressedCheckBox.onInput { updateCapacity() }
        sizedCheckBox.onInput { updateCapacity() }
        CRC32CheckBox.onInput { updateCapacity() }
        SHA3256CheckBox.onInput { updateCapacity() }

        textFormPanel.add(
            HPanel {
                // Submit button
                add(submitButton)
                submitButton.onClick {
                    if (textFormPanel.validate()) {
                        // Modal
                        val modal = Modal("Result")

                        val watermarkedResult =
                            addWatermarkToText(
                                textFormPanel.getData().watermark,
                                textFormPanel.getData().text,
                                textFormPanel.getData().innamarkCompressed,
                                textFormPanel.getData().innamarkSized,
                                textFormPanel.getData().innamarkCRC32,
                                textFormPanel.getData().innamarkSHA3256,
                            )

                        if (watermarkedResult.isSuccess) {
                            val watermarkedText = watermarkedResult.value ?: ""

                            modal.add(
                                div(
                                    "Successfully embedded your watermark in the " +
                                        "cover text",
                                    className = "alert alert-success",
                                ),
                            )
                            modal.add(span("The following text includes your watermark:"))
                            modal.add(div(watermarkedText, className = "selectable card-text"))
                            modal.addButton(
                                Button("Copy to Clipboard") {
                                    onClick {
                                        window.navigator.clipboard.writeText(watermarkedText)
                                        Toast.success("Successfully copied to clipboard!")
                                    }
                                },
                            )
                        } else {
                            modal.add(
                                div(
                                    "An error occurs during the watermarking " +
                                        "process: <br />" + watermarkedResult.getMessage(),
                                    rich = true,
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

                // Clear data button
                button("Clear data", "fas fa-trash", ButtonStyle.DANGER).onClick {
                    Confirm.show(
                        "Are you sure?",
                        "Do you want to clear your data?",
                        yesTitle = "Yes",
                        noTitle = "No",
                        cancelTitle = "Cancel",
                    ) {
                        textFormPanel.clearData()
                        capacityObservable.value = -1
                        min = -1
                        progressBar.getFirstProgressBar()?.value = -1
                        progressBar.setBounds(-1, 0)
                    }
                }
            },
        )

        textFormPanel.add(watermarkCapacityLowHint)
        textFormPanel.add(watermarkCapacitySuccessHint)
    }

    /**
     * Calculates on every user input if the watermark fits into the cover text
     */
    private fun updateCapacity() {
        capacityObservable.value =
            watermarkFitsInText(
                watermarkerInput.value.toString(),
                coverTextInput.value.toString(),
                compressedCheckBox.value,
                sizedCheckBox.value,
                CRC32CheckBox.value,
                SHA3256CheckBox.value,
            )

        // Enable or disable the submit form button and user hint
        if (capacityObservable.value < 0) {
            submitButton.disabled = true
            watermarkCapacityLowHint.show()
            watermarkCapacitySuccessHint.hide()
        } else {
            submitButton.disabled = false
            watermarkCapacityLowHint.hide()
            watermarkCapacitySuccessHint.show()
        }

        // Update progress bar
        min = if (capacityObservable.value < min) capacityObservable.value else min
        progressBar.setBounds(min, 0)
        progressBar.getFirstProgressBar()?.value =
            (if (watermarkerInput.value == null) min else capacityObservable.value)
    }

    /** Adds a [watermarkString] string to [text] and returns the watermarked text result */
    private fun addWatermarkToText(
        watermarkString: String,
        text: String,
        innamarkCompressed: Boolean = false,
        innamarkSized: Boolean = false,
        innamarkCRC32: Boolean = false,
        innamarkSHA3256: Boolean = false,
    ): Result<String> {
        val watermarker = Watermarker()
        val watermark =
            TextWatermark.raw(watermarkString).apply {
                if (innamarkCompressed) compressed()
                if (innamarkSized) sized()
                if (innamarkCRC32) CRC32()
                if (innamarkSHA3256) SHA3256()
            }
        return watermarker.textAddWatermark(text, watermark)
    }

    /**
     * Calculates if the [watermark] fits into the [text]
     *
     * Returns:
     *  - a negative value with the number of missing insert positions ([watermark] doesn't fit)
     *  - a zero if the [watermark] perfectly fits in the [text] one time
     *  - a positive value with the number of remaining insert positions ([watermark] fits)
     */
    private fun watermarkFitsInText(
        watermark: String,
        text: String,
        innamarkCompressed: Boolean,
        innamarkSized: Boolean,
        innamarkCRC32: Boolean,
        innamarkSHA3256: Boolean,
    ): Int {
        val parsedWatermark =
            TextWatermark.raw(watermark).apply {
                if (innamarkCompressed) compressed()
                if (innamarkSized) sized()
                if (innamarkCRC32) CRC32()
                if (innamarkSHA3256) SHA3256()
            }

        val numberOfInsertPositions = textWatermarker.placement(text).count()
        val numberOfNeededPositions = textWatermarker.getMinimumInsertPositions(parsedWatermark)

        return numberOfInsertPositions - numberOfNeededPositions
    }
}
