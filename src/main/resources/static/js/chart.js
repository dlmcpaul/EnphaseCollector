function makeChart(target, properties, refreshUrl, interval) {

    // Create Chart
    var chart = new Highcharts.chart(properties);

    // Set initial values
    refreshChart(chart, refreshUrl, target);

    // Refresh every interval
    setInterval(function() { refreshChart(chart, refreshUrl, target) }, interval);
}

function refreshChart(chart, url, target) {

    var request = new XMLHttpRequest();
    request.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            var point = chart.series[0].points[0];
            point.update(Number(request.responseText));

            document.getElementById(target + "_label").innerHTML = request.responseText + " W";
        }
    };
    request.open("GET", url, true);
    request.send();
}

function makePvcChart(target, properties, refreshUrl, interval) {
    // Create Chart
    var chart = new Highcharts.chart(properties);

    // Set initial values
    refreshPvcChart(chart, refreshUrl);

    // Refresh every interval
    setInterval(function() { refreshPvcChart(chart, refreshUrl) }, interval);

}

function refreshPvcChart(chart, url) {

    var request = new XMLHttpRequest();
    request.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            var response = JSON.parse(request.responseText);

            chart.series[0].setData(response.production);
            chart.series[1].setData(response.consumption);
        }
    };
    request.open("GET", url, true);
    request.send();
}

function makeStats(target, refreshUrl, interval) {
    // Refresh every interval
    setInterval(function() { refreshStats(target, refreshUrl) }, interval);
}

function refreshStats(target, url) {
    var request = new XMLHttpRequest();
    request.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            document.getElementById(target).outerHTML = request.responseText;
        }
    };
    request.open("GET", url, true);
    request.send();
}