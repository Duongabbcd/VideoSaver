package com.example.videosaver.utils

import org.json.JSONObject
import org.jsoup.Jsoup

object VideoSupporter {
    fun getVideoResolutionsFromPageSource(
        pageSourceXmlString: String?,
        finished: (listOfRes: ArrayList<ResolutionDetail>) -> Unit ) {
        println("getVideoResolutionsFromPageSource 0: $pageSourceXmlString")
        val listOfResolutions = arrayListOf<ResolutionDetail>()
        if (!pageSourceXmlString?.isEmpty()!!) {
            val document: org.jsoup.nodes.Document = Jsoup.parse(pageSourceXmlString)
            val sampleDiv = document.getElementsByTag("body")
            println("getVideoResolutionsFromPageSource 1: $sampleDiv")
            if (!sampleDiv.isEmpty()) {
                val bodyDocument: org.jsoup.nodes.Document = Jsoup.parse(sampleDiv.html())
                val dataStoreDiv: org.jsoup.nodes.Element? =
                    bodyDocument.select("div._53mw").first()
                val dataStoreAttr = dataStoreDiv?.attr("data-store")
                val jsonObject = JSONObject(dataStoreAttr)
                println("jsonObject: $jsonObject")
                if (jsonObject.has("dashManifest")) {
                    val dashManifestString: String = jsonObject.getString("dashManifest")
                    val dashManifestDoc: org.jsoup.nodes.Document = Jsoup.parse(dashManifestString)
                    val mdpTagVal = dashManifestDoc.getElementsByTag("MPD")
                    val mdpDoc: org.jsoup.nodes.Document = Jsoup.parse(mdpTagVal.html())
                    val periodTagVal = mdpDoc.getElementsByTag("Period")
                    val periodDocument: org.jsoup.nodes.Document = Jsoup.parse(periodTagVal.html())
                    val subBodyDiv: org.jsoup.nodes.Element? = periodDocument.select("body").first()
                    subBodyDiv?.children()?.forEach {
                        val adaptionSetDiv: org.jsoup.nodes.Element? =
                            it.select("adaptationset").first()
                        adaptionSetDiv?.children()?.forEach {
                            if (it is org.jsoup.nodes.Element) {
                                val representationDiv: org.jsoup.nodes.Element? =
                                    it.select("representation").first()
                                val resolutionDetail = ResolutionDetail()
                                if (representationDiv?.hasAttr("mimetype")!!) {
                                    resolutionDetail.mimetype =
                                        representationDiv?.attr("mimetype").toString()
                                }
                                if (representationDiv?.hasAttr("width")!!) {
                                    resolutionDetail.width =
                                        representationDiv?.attr("width")?.toLong()!!
                                }
                                if (representationDiv?.hasAttr("height")!!) {
                                    resolutionDetail.height =
                                        representationDiv.attr("height").toLong()
                                }
                                if (representationDiv?.hasAttr("FBDefaultQuality")!!) {
                                    resolutionDetail.FBDefaultQuality =
                                        representationDiv.attr("FBDefaultQuality")
                                }
                                if (representationDiv?.hasAttr("FBQualityClass")!!) {
                                    resolutionDetail.FBQualityClass =
                                        representationDiv.attr("FBQualityClass")
                                }
                                if (representationDiv?.hasAttr("FBQualityLabel")!!) {
                                    resolutionDetail.FBQualityLabel =
                                        representationDiv.attr("FBQualityLabel")
                                }
                                val representationDoc: org.jsoup.nodes.Document =
                                    Jsoup.parse(representationDiv.html())
                                val baseUrlTag = representationDoc.getElementsByTag("BaseURL")
                                if (!baseUrlTag.isEmpty() && !resolutionDetail.FBQualityLabel.equals(
                                        "Source",
                                        ignoreCase = true
                                    )
                                ) {
                                    resolutionDetail.videoQualityURL = baseUrlTag[0].text()
                                    listOfResolutions.add(resolutionDetail)
                                }
                            }
                        }
                    }
                }
            }
        }
        finished(listOfResolutions)
    }
}

data class ResolutionDetail(
    var width: Long = 0,
    var height: Long = 0,
    var FBQualityLabel: String = "",
    var FBDefaultQuality: String = "",
    var FBQualityClass: String = "",
    var videoQualityURL: String = "",
    var mimetype: String = "", // [audio/mp4 for audios and video/mp4 for videos] }
)