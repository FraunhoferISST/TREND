import trend from "../../watermarker/build/dist/js/productionExecutable/watermarker.js";

const watermarker = trend.de.fraunhofer.isst.trend.watermarker;
const TextFile = watermarker.files.TextFile;
const TextWatermarker =
  watermarker.fileWatermarker.TextWatermarker.Companion.default();
const TextWatermark = watermarker.fileWatermarker.TextWatermark;

let textFile = TextFile.Companion.fromString(
  "Lorem ipsum dolor sit amet, consetetur sadipscing\
elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam \
voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no \
sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur \
sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, \
sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd \
gubergren, no sea takimata sanctus est Lorem ipsum"
);

let watermark = TextWatermark.Companion.fromText("Hello World!");

TextWatermarker.addWatermark(textFile, watermark);

console.log(textFile.content);
