package com.zenmo.vallum

import com.zenmo.zummon.companysurvey.Survey
import com.zenmo.bag.Pand
import com.zenmo.bag.byPandIds

private fun allPandIdStrings(surveys: List<Survey>): Set<String> = surveys
    .flatMap { it.allPandIds() }
    .map { it.value }
    .toSet()

fun fetchBagPanden(surveys: List<Survey>): Map<String, Pand> {
    val pandIs = allPandIdStrings(surveys)

    return byPandIds(pandIs)
}
