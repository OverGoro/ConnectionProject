var stats = {
    type: "GROUP",
name: "Global Information",
path: "",
pathFormatted: "group_missing-name-b06d1",
stats: {
    "name": "Global Information",
    "numberOfRequests": {
        "total": "2000",
        "ok": "2000",
        "ko": "0"
    },
    "minResponseTime": {
        "total": "81",
        "ok": "81",
        "ko": "-"
    },
    "maxResponseTime": {
        "total": "6672",
        "ok": "6672",
        "ko": "-"
    },
    "meanResponseTime": {
        "total": "2533",
        "ok": "2533",
        "ko": "-"
    },
    "standardDeviation": {
        "total": "858",
        "ok": "858",
        "ko": "-"
    },
    "percentiles1": {
        "total": "2421",
        "ok": "2421",
        "ko": "-"
    },
    "percentiles2": {
        "total": "3047",
        "ok": "3047",
        "ko": "-"
    },
    "percentiles3": {
        "total": "4028",
        "ok": "4028",
        "ko": "-"
    },
    "percentiles4": {
        "total": "4876",
        "ok": "4876",
        "ko": "-"
    },
    "group1": {
    "name": "t < 800 ms",
    "count": 41,
    "percentage": 2
},
    "group2": {
    "name": "800 ms < t < 1200 ms",
    "count": 61,
    "percentage": 3
},
    "group3": {
    "name": "t > 1200 ms",
    "count": 1898,
    "percentage": 95
},
    "group4": {
    "name": "failed",
    "count": 0,
    "percentage": 0
},
    "meanNumberOfRequestsPerSecond": {
        "total": "153.846",
        "ok": "153.846",
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
        "total": "2000",
        "ok": "2000",
        "ko": "0"
    },
    "minResponseTime": {
        "total": "81",
        "ok": "81",
        "ko": "-"
    },
    "maxResponseTime": {
        "total": "6672",
        "ok": "6672",
        "ko": "-"
    },
    "meanResponseTime": {
        "total": "2533",
        "ok": "2533",
        "ko": "-"
    },
    "standardDeviation": {
        "total": "858",
        "ok": "858",
        "ko": "-"
    },
    "percentiles1": {
        "total": "2421",
        "ok": "2421",
        "ko": "-"
    },
    "percentiles2": {
        "total": "3047",
        "ok": "3047",
        "ko": "-"
    },
    "percentiles3": {
        "total": "4028",
        "ok": "4028",
        "ko": "-"
    },
    "percentiles4": {
        "total": "4876",
        "ok": "4876",
        "ko": "-"
    },
    "group1": {
    "name": "t < 800 ms",
    "count": 41,
    "percentage": 2
},
    "group2": {
    "name": "800 ms < t < 1200 ms",
    "count": 61,
    "percentage": 3
},
    "group3": {
    "name": "t > 1200 ms",
    "count": 1898,
    "percentage": 95
},
    "group4": {
    "name": "failed",
    "count": 0,
    "percentage": 0
},
    "meanNumberOfRequestsPerSecond": {
        "total": "153.846",
        "ok": "153.846",
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
