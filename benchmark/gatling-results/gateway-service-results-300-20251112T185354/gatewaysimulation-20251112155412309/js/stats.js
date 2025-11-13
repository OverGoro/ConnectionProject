var stats = {
    type: "GROUP",
name: "Global Information",
path: "",
pathFormatted: "group_missing-name-b06d1",
stats: {
    "name": "Global Information",
    "numberOfRequests": {
        "total": "3000",
        "ok": "3000",
        "ko": "0"
    },
    "minResponseTime": {
        "total": "143",
        "ok": "143",
        "ko": "-"
    },
    "maxResponseTime": {
        "total": "8460",
        "ok": "8460",
        "ko": "-"
    },
    "meanResponseTime": {
        "total": "5109",
        "ok": "5109",
        "ko": "-"
    },
    "standardDeviation": {
        "total": "1286",
        "ok": "1286",
        "ko": "-"
    },
    "percentiles1": {
        "total": "5367",
        "ok": "5367",
        "ko": "-"
    },
    "percentiles2": {
        "total": "5944",
        "ok": "5944",
        "ko": "-"
    },
    "percentiles3": {
        "total": "6699",
        "ok": "6699",
        "ko": "-"
    },
    "percentiles4": {
        "total": "7496",
        "ok": "7496",
        "ko": "-"
    },
    "group1": {
    "name": "t < 800 ms",
    "count": 15,
    "percentage": 1
},
    "group2": {
    "name": "800 ms < t < 1200 ms",
    "count": 20,
    "percentage": 1
},
    "group3": {
    "name": "t > 1200 ms",
    "count": 2965,
    "percentage": 99
},
    "group4": {
    "name": "failed",
    "count": 0,
    "percentage": 0
},
    "meanNumberOfRequestsPerSecond": {
        "total": "187.5",
        "ok": "187.5",
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
        "total": "3000",
        "ok": "3000",
        "ko": "0"
    },
    "minResponseTime": {
        "total": "143",
        "ok": "143",
        "ko": "-"
    },
    "maxResponseTime": {
        "total": "8460",
        "ok": "8460",
        "ko": "-"
    },
    "meanResponseTime": {
        "total": "5109",
        "ok": "5109",
        "ko": "-"
    },
    "standardDeviation": {
        "total": "1286",
        "ok": "1286",
        "ko": "-"
    },
    "percentiles1": {
        "total": "5367",
        "ok": "5367",
        "ko": "-"
    },
    "percentiles2": {
        "total": "5944",
        "ok": "5944",
        "ko": "-"
    },
    "percentiles3": {
        "total": "6699",
        "ok": "6699",
        "ko": "-"
    },
    "percentiles4": {
        "total": "7496",
        "ok": "7496",
        "ko": "-"
    },
    "group1": {
    "name": "t < 800 ms",
    "count": 15,
    "percentage": 1
},
    "group2": {
    "name": "800 ms < t < 1200 ms",
    "count": 20,
    "percentage": 1
},
    "group3": {
    "name": "t > 1200 ms",
    "count": 2965,
    "percentage": 99
},
    "group4": {
    "name": "failed",
    "count": 0,
    "percentage": 0
},
    "meanNumberOfRequestsPerSecond": {
        "total": "187.5",
        "ok": "187.5",
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
