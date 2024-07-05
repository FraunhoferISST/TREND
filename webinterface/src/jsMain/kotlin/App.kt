/*
 * Copyright (c) 2023-2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */

import io.kvision.Application
import io.kvision.BootstrapCssModule
import io.kvision.BootstrapModule
import io.kvision.BootstrapUploadModule
import io.kvision.CoreModule
import io.kvision.FontAwesomeModule
import io.kvision.html.h1
import io.kvision.html.span
import io.kvision.i18n.I18n.tr
import io.kvision.module
import io.kvision.panel.root
import io.kvision.panel.tab
import io.kvision.panel.tabPanel
import io.kvision.panel.vPanel
import io.kvision.startApplication
import io.kvision.utils.auto
import io.kvision.utils.em
import io.kvision.utils.perc

class App : Application() {
    /** Initial method to load the default watermarker form */
    override fun start() {
        root("trend") {
            vPanel {
                // Styling
                width = 90.perc
                marginLeft = auto
                marginRight = auto
                paddingTop = 1.em

                // Intro text
                h1("TREND")
                span(
                    "This tools allows you to hide or reveal a text-based watermark (word, name, " +
                        "sentence, etc.) in a text of your choice.",
                ) {
                    marginBottom = 1.em
                }

                tabPanel {
                    // border = Border(2.px, BorderStyle.SOLID)
                    tab(tr("Text"), "fas fa-tarp", route = "/watermarkText") {
                        add(WatermarkTextTab())
                    }
                    tab(tr("Extract"), "fas fa-arrow-down", route = "/watermarkExtract") {
                        add(WatermarkExtractTab())
                    }
                }
            }
        }
    }
}

/** Main KVision method to load modules */
fun main() {
    startApplication(
        ::App,
        module.hot,
        BootstrapModule,
        BootstrapCssModule,
        BootstrapUploadModule,
        CoreModule,
        FontAwesomeModule,
    )
}
