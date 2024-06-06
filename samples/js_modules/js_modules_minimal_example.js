/*
 * Copyright (c) 2024 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 * This work is licensed under the Fraunhofer License (on the basis of the MIT license)
 * that can be found in the LICENSE file.
 */
import trend from "../../watermarker/build/dist/js/productionExecutable/watermarker.js";

const watermarker = trend.de.fraunhofer.isst.trend.watermarker;
const Watermarker = new watermarker.Watermarker();
const TextWatermark = watermarker.watermarks.TextWatermark;

let text = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor \
invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et \
justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum \
dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod \
tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam \
et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem \
ipsum.";

let watermark = TextWatermark.Companion.new("Hello World!");

let result = Watermarker.textAddTrendmarkBuilder(text, watermark);

if (result.isSuccess) {
  console.log(result.value);
} else {
  console.log(result);
}

