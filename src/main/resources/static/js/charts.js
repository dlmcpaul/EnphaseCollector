function makeGuageChart(target, properties, refreshUrl, interval) {

    // Create Chart
    var chart = new Highcharts.chart(properties);

    // Set initial values
    refreshGuageChart(chart, refreshUrl, target);

    // Refresh every interval
    setInterval(function() { refreshGuageChart(chart, refreshUrl, target) }, interval);
}

function refreshGuageChart(chart, url, target) {

    var request = new XMLHttpRequest();
    request.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            var point = chart.series[0].points[0];
            point.update(Number(request.responseText));

            element = document.getElementById(target + "_label");
            if (element != null) {
                element.innerHTML = request.responseText + " W";
            }
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
            chart.series[2].setData(response.gridimport);
        }
    };
    request.open("GET", url, true);
    request.send();
}

function makeStatusList(target, refreshUrl, interval) {
    // Refresh every interval
    setInterval(function() { refreshStatusList(target, refreshUrl) }, interval);
}

function refreshStatusList(target, url) {
    var request = new XMLHttpRequest();
    request.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            document.getElementById(target).outerHTML = request.responseText;
        }
    };
    request.open("GET", url, true);
    request.send();
}