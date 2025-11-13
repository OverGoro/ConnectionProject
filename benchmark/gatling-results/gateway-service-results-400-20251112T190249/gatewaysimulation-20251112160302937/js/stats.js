var stats = {
    type: "GROUP",
name: "Global Information",
path: "",
pathFormatted: "group_missing-name-b06d1",
stats: {
    "name": "Global Information",
    "numberOfRequests": {
        "total": "4000",
        "ok": "4000",
        "ko": "0"
    },
    "minResponseTime": {
        "total": "66",
        "ok": "66",
        "ko": "-"
    },
    "maxResponseTime": {
        "total": "10859",
        "ok": "10859",
        "ko": "-"
    },
    "meanResponseTime": {
        "total": "6592",
        "ok": "6592",
        "ko": "-"
    },
    "standardDeviation": {
        "total": "1968",
        "ok": "1968",
        "ko": "-"
    },
    "percentiles1": {
        "total": "7146",
        "ok": "7147",
        "ko": "-"
    },
    "percentiles2": {
        "total": "7942",
        "ok": "7942",
        "ko": "-"
    },
    "percentiles3": {
        "total": "9096",
        "ok": "9096",
        "ko": "-"
    },
    "percentiles4": {
        "total": "9923",
        "ok": "9923",
        "ko": "-"
    },
    "group1": {
    "name": "t < 800 ms",
    "count": 16,
    "percentage": 0
},
    "group2": {
    "name": "800 ms < t < 1200 ms",
    "count": 18,
    "percentage": 0
},
    "group3": {
    "name": "t > 1200 ms",
    "count": 3966,
    "percentage": 99
},
    "group4": {
    "name": "failed",
    "count": 0,
    "percentage": 0
},
    "meanNumberOfRequestsPerSecond": {
        "total": "210.526",
        "ok": "210.526",
        "ko": "-"
    }
},
contents: {
"req_health-check-9583d": {
        type: "REQUEST",
        name: "Health Check",
path: "Health Check",
pathFormatted: "req_health-check-9583d",
stats: {
    "name": "Health Check",
    "numberOfRequests": {
        "total": "4000",
        "ok": "4000",
        "ko": "0"
    },
    "minResponseTime": {
        "total": "66",
        "ok": "66",
        "ko": "-"
    },
    "maxResponseTime": {
        "total": "10859",
        "ok": "10859",
        "ko": "-"
    },
    "meanResponseTime": {
        "total": "6592",
        "ok": "6592",
        "ko": "-"
    },
    "standardDeviation": {
        "total": "1968",
        "ok": "1968",
        "ko": "-"
    },
    "percentiles1": {
        "total": "7146",
        "ok": "7147",
        "ko": "-"
    },
    "percentiles2": {
        "total": "7942",
        "ok": "7942",
        "ko": "-"
    },
    "percentiles3": {
        "total": "9096",
        "ok": "9096",
        "ko": "-"
    },
    "percentiles4": {
        "total": "9923",
        "ok": "9923",
        "ko": "-"
    },
    "group1": {
    "name": "t < 800 ms",
    "count": 16,
    "percentage": 0
},
    "group2": {
    "name": "800 ms < t < 1200 ms",
    "count": 18,
    "percentage": 0
},
    "group3": {
    "name": "t > 1200 ms",
    "count": 3966,
    "percentage": 99
},
    "group4": {
    "name": "failed",
    "count": 0,
    "percentage": 0
},
    "meanNumberOfRequestsPerSecond": {
        "total": "210.526",
        "ok": "210.526",
        "ko": "-"
    }
}
    }
}

}

function fillStats(stat){
    $("#numberOfRequests").append(stat.numberOfRequests.total);
    $("#numberOfRequestsOK").append(stat.numberOfRequests.ok);
    $("#numberOfRequestsKO").append(stat.numberOfRequests.ko);

    $("#minResponseTime").append(stat.minResponseTime.total);
    $("#minResponseTimeOK").append(stat.minResponseTime.ok);
    $("#minResponseTimeKO").append(stat.minResponseTime.ko);

    $("#maxResponseTime").append(stat.maxResponseTime.total);
    $("#maxResponseTimeOK").append(stat.maxResponseTime.ok);
    $("#maxResponseTimeKO").append(stat.maxResponseTime.ko);

    $("#meanResponseTime").append(stat.meanResponseTime.total);
    $("#meanResponseTimeOK").append(stat.meanResponseTime.ok);
    $("#meanResponseTimeKO").append(stat.meanResponseTime.ko);

    $("#standardDeviation").append(stat.standardDeviation.total);
    $("#standardDeviationOK").append(stat.standardDeviation.ok);
    $("#standardDeviationKO").append(stat.standardDeviation.ko);

    $("#percentiles1").append(stat.percentiles1.total);
    $("#percentiles1OK").append(stat.percentiles1.ok);
    $("#percentiles1KO").append(stat.percentiles1.ko);

    $("#percentiles2").append(stat.percentiles2.total);
    $("#percentiles2OK").append(stat.percentiles2.ok);
    $("#percentiles2KO").append(stat.percentiles2.ko);

    $("#percentiles3").append(stat.percentiles3.total);
    $("#percentiles3OK").append(stat.percentiles3.ok);
    $("#percentiles3KO").append(stat.percentiles3.ko);

    $("#percentiles4").append(stat.percentiles4.total);
    $("#percentiles4OK").append(stat.percentiles4.ok);
    $("#percentiles4KO").append(stat.percentiles4.ko);

    $("#meanNumberOfRequestsPerSecond").append(stat.meanNumberOfRequestsPerSecond.total);
    $("#meanNumberOfRequestsPerSecondOK").append(stat.meanNumberOfRequestsPerSecond.ok);
    $("#meanNumberOfRequestsPerSecondKO").append(stat.meanNumberOfRequestsPerSecond.ko);
}
