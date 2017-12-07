
var findButton = document.getElementById("find-bluetooth");
var info = document.getElementById("info");
var printButton = document.getElementById("print-bluetooth");
var closeButton = document.getElementById("close-bluetooth");
var printable = document.getElementById("printable");

findButton.addEventListener("click", function() {
    AppInterface.connectPrinter();
    console.log("after printer connected");
});

printButton.addEventListener("click", function() {
    AppInterface.printText(printable.value);
});

closeButton.addEventListener("click", function() {
    AppInterface.closePrinterConnection();
});