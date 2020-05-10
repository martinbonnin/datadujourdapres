package net.mbonnin.data.du.jourdapres

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.coroutines.toDeferred
import com.opencsv.CSVWriter
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import net.mbonnin.data.du.jour.dapres.ComponentsQuery
import net.mbonnin.data.du.jour.dapres.ProposalsQuery
import org.junit.Test
import java.io.File
import java.io.FileWriter


class MainTest {
    val apolloClient = ApolloClient.builder()
        .serverUrl("https://lejourdapres.parlement-ouvert.fr/api")
        .build()


    @Test
    fun downloadAll() {
        runBlocking {
            val response = apolloClient.query(ComponentsQuery()).toDeferred().await()

            response.data()?.participatoryProcess?.components?.filterNotNull()
                //?.take(1)
                ?.forEach {
                    downloadComponent(it.id, it.name.translation!!)
                }
        }
    }

    val force = false

    suspend fun downloadComponent(id: String, name: String) {
        val list = mutableListOf<Map<String, String>>()

        println("downloading $name")

        if (!force && File("$name.csv").exists()) {
            println("skipping $name")
            return
        }

        var after = Input.absent<String>()
        while (true) {
            val response = apolloClient.query(ProposalsQuery(id, after)).toDeferred().await()
            val proposals = response.data()!!.component!!.asProposals!!.proposals!!

            proposals.edges?.map { it?.node }?.filterNotNull()?.map {
                mapOf(
                    "endorsementCount" to it.endorsementsCount?.toString()!!,
                    "id" to it.id,
                    "title" to it.title,
                    "url" to "https://lejourdapres.parlement-ouvert.fr/processes/lejourdapres/f/$id/proposals/${it.id}"
                )
            }?.let {
                list.addAll(
                    it
                )
            }
            if (proposals.pageInfo.hasNextPage == false) {
                break
            }

            after = Input.optional(proposals.pageInfo.endCursor!!)
        }

        list.sortByDescending { it["endorsementCount"]!!.toInt() }

        val writer = CSVWriter(FileWriter("$name.csv"))
        list.forEach {
            writer.writeNext(it.entries.sortedBy { it.key }.map { it.value }.toTypedArray())
        }

        writer.close()

        JsonArray(list.map {
            val jsonObject = JsonObject(it.mapValues {
                JsonPrimitive(it.value)
            })
            jsonObject
        }).let {
            File("$name.json").writeText(it.toString())
        }
    }

    @Test
    fun numberOfVotes() {
        var count = 0
        val all = File(".").listFiles().filter { it.extension == "json" }.map {
            val json = Json.parseJson(it.readText())

            val total = json.jsonArray.map { it.jsonObject.getPrimitive("endorsementCount").int }.sum()

            count += json.jsonArray.size

            it.name to total
        }

        all.sortedByDescending { it.second }
            .forEach {
                println("${it.first}: ${it.second}")
            }

        println("total proposals: $count")
        println("total endorsements: ${all.map { it.second }.sum()}")
    }
}