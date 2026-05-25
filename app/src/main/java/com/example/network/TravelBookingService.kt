package com.example.network

import android.util.Log
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query
import java.io.IOException
import java.net.URLEncoder
import java.util.*
import kotlin.random.Random

// --- Universal Data Models for Travel Search ---
data class FlightOffer(
    val id: String,
    val airline: String,
    val flightNumber: String,
    val departureTime: String,
    val arrivalTime: String,
    val price: String,
    val currency: String,
    val duration: String,
    val stops: Int,
    val bookingUrl: String,
    val bookingReferencePlaceholder: String
)

data class HotelOffer(
    val id: String,
    val name: String,
    val address: String,
    val rating: Double,
    val pricePerNight: String,
    val currency: String,
    val totalPrice: String,
    val checkInDate: String,
    val checkOutDate: String,
    val bookingUrl: String,
    val bookingReferencePlaceholder: String
)

// --- Retrofit API Interfaces for Amadeus ---
interface AmadeusAuthApi {
    @retrofit2.http.POST("v1/security/oauth2/token")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    suspend fun getAccessToken(
        @retrofit2.http.Field("grant_type") grantType: String,
        @retrofit2.http.Field("client_id") clientId: String,
        @retrofit2.http.Field("client_secret") clientSecret: String
    ): AmadeusTokenResponse
}

data class AmadeusTokenResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "expires_in") val expiresIn: Int
)

interface AmadeusTravelApi {
    @GET("v2/shopping/flight-offers")
    suspend fun searchFlightOffers(
        @Header("Authorization") authHeader: String,
        @Query("originLocationCode") origin: String,
        @Query("destinationLocationCode") destination: String,
        @Query("departureDate") date: String,
        @Query("adults") adults: Int = 1,
        @Query("currencyCode") currency: String = "USD",
        @Query("max") max: Int = 10
    ): AmadeusFlightSearchResponse

    @GET("v1/reference-data/locations/hotels/by-city")
    suspend fun listHotelsByCity(
        @Header("Authorization") authHeader: String,
        @Query("cityCode") cityCode: String
    ): AmadeusHotelSearchResponse
}

// Amadeus flight responses structure (simplified for binding)
data class AmadeusFlightSearchResponse(
    val data: List<AmadeusFlightOffer>?
)

data class AmadeusFlightOffer(
    val id: String,
    val itineraires: List<AmadeusItinerary>?, // Note: some structures use itineraries
    val itineraries: List<AmadeusItinerary>?,
    val price: AmadeusPrice?
)

data class AmadeusItinerary(
    val duration: String?,
    val segments: List<AmadeusSegment>?
)

data class AmadeusSegment(
    val departure: AmadeusDepartureArrival?,
    val arrival: AmadeusDepartureArrival?,
    val carrierCode: String?,
    val number: String?
)

data class AmadeusDepartureArrival(
    val iataCode: String?,
    val at: String?
)

data class AmadeusPrice(
    val currency: String?,
    val total: String?
)

// Amadeus hotel response structure
data class AmadeusHotelSearchResponse(
    val data: List<AmadeusHotel>?
)

data class AmadeusHotel(
    val hotelId: String?,
    val name: String?,
    val address: AmadeusHotelAddress?,
    val rating: String?
)

data class AmadeusHotelAddress(
    val lines: List<String>?
)


class TravelBookingService {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val amadeusBaseUrl = "https://test.api.amadeus.com/"

    // In-memory token storage
    private var cachedToken: String? = null
    private var tokenExpiryTime: Long = 0

    // Fetch Token directly via OkHttp to ensure simple form-url-encoded compatibility
    private fun fetchAmadeusTokenDirect(clientId: String, clientSecret: String): String? {
        val formBody = FormBody.Builder()
            .add("grant_type", "client_credentials")
            .add("client_id", clientId)
            .add("client_secret", clientSecret)
            .build()

        val request = Request.Builder()
            .url("${amadeusBaseUrl}v1/security/oauth2/token")
            .post(formBody)
            .build()

        return try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                val adapter = moshi.adapter(AmadeusTokenResponse::class.java)
                val tokenResponse = adapter.fromJson(body)
                tokenResponse?.accessToken
            }
        } catch (e: Exception) {
            Log.e("TravelBookingService", "Failed to get Amadeus token", e)
            null
        }
    }

    /**
     * Search Flight offers. Supports live Amadeus if API keys provided, falls back to custom high-quality dynamic simulation.
     */
    suspend fun searchFlights(
        clientId: String,
        clientSecret: String,
        fromCity: String,
        toCity: String,
        departureDate: String
    ): List<FlightOffer> {
        val useMock = clientId.trim().isEmpty() || clientSecret.trim().isEmpty()

        if (!useMock) {
            try {
                // Fetch dynamic token if needed
                val token = cachedToken ?: fetchAmadeusTokenDirect(clientId, clientSecret)
                if (token != null) {
                    cachedToken = token
                    
                    val retrofit = Retrofit.Builder()
                        .baseUrl(amadeusBaseUrl)
                        .client(okHttpClient)
                        .addConverterFactory(MoshiConverterFactory.create(moshi))
                        .build()

                    val travelApi = retrofit.create(AmadeusTravelApi::class.java)
                    // Call flight offer search
                    val iataFrom = fromCity.take(3).uppercase()
                    val iataTo = toCity.take(3).uppercase()
                    
                    // Run blocking in background thread safely (handled on ViewModel Thread)
                    val response = retrofit2.Response.success(
                        travelApi.searchFlightOffers(
                            authHeader = "Bearer $token",
                            origin = iataFrom,
                            destination = iataTo,
                            date = departureDate
                        )
                    )
                    
                    val body = response.body()
                    val list = body?.data
                    if (list != null && list.isNotEmpty()) {
                        return list.map { amOffer ->
                            val priceStr = amOffer.price?.total ?: "420.0"
                            val currency = amOffer.price?.currency ?: "USD"
                            val itinerary = amOffer.itineraries?.firstOrNull() ?: amOffer.itineraires?.firstOrNull()
                            val segment = itinerary?.segments?.firstOrNull()
                            val carrier = segment?.carrierCode ?: "Sky Alliance"
                            val number = segment?.number ?: "98"
                            val depAt = segment?.departure?.at?.substringAfter("T")?.take(5) ?: "10:30"
                            val arrAt = segment?.arrival?.at?.substringAfter("T")?.take(5) ?: "18:45"
                            
                            val flightRef = "SKY-${carrier}-${number}-${Random.nextInt(100, 999)}"

                            FlightOffer(
                                id = amOffer.id,
                                airline = getCarrierName(carrier),
                                flightNumber = "${carrier} $number",
                                departureTime = depAt,
                                arrivalTime = arrAt,
                                price = "$priceStr",
                                currency = currency,
                                duration = itinerary?.duration ?: "7h 45m",
                                stops = (itinerary?.segments?.size ?: 1) - 1,
                                bookingUrl = "https://www.skyscanner.net/transport/flights/${fromCity.lowercase()}/${toCity.lowercase()}/",
                                bookingReferencePlaceholder = flightRef
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("TravelBookingService", "Failed live Flight search, falling back to rich simulation", e)
            }
        }

        // --- Custom High Quality Responsive Mock Simulator ---
        val cleanFrom = fromCity.trim().ifEmpty { "San Francisco" }
        val cleanTo = toCity.trim().ifEmpty { "Florence" }
        val cleanDate = departureDate.trim().ifEmpty { "2026-07-18" }

        val seed = (cleanFrom.length + cleanTo.length + cleanDate.hashCode()).toLong()
        val random = Random(seed)

        val potentialAirlines = listOf(
            "Swiss International Air Lines" to "LX",
            "Lufthansa" to "LH",
            "Air France" to "AF",
            "British Airways" to "BA",
            "United Airlines" to "UA",
            "Delta Air Lines" to "DL",
            "ITA Airways" to "AZ"
        )

        return List(4) { index ->
            val (airline, code) = potentialAirlines[index % potentialAirlines.size]
            val flightNum = "$code${random.nextInt(100, 999)}"
            val basePrice = random.nextInt(380, 1450) + index * 85
            val durationMin = random.nextInt(360, 900)
            val durationHours = durationMin / 60
            val durationLeving = durationMin % 60
            val durationText = "${durationHours}h ${durationLeving}m"
            val stops = if (durationMin > 500) 1 else 0

            val depHour = 7 + (index * 4) % 15
            val depMin = listOf(0, 15, 30, 45)[index % 4]
            val arrHour = (depHour + durationHours) % 24
            val arrMin = (depMin + durationLeving) % 60

            val depTime = String.format("%02d:%02d", depHour, depMin)
            val arrTime = String.format("%02d:%02d", arrHour, arrMin)
            val flightRef = "SKYS-${code}-${flightNum}-${random.nextInt(1000, 9999)}"

            val originSlug = URLEncoder.encode(cleanFrom, "UTF-8")
            val destSlug = URLEncoder.encode(cleanTo, "UTF-8")

            FlightOffer(
                id = "fl_sim_$index",
                airline = airline,
                flightNumber = flightNum,
                departureTime = depTime,
                arrivalTime = arrTime,
                price = "$basePrice",
                currency = "USD",
                duration = durationText,
                stops = stops,
                bookingUrl = "https://www.skyscanner.net/transport/flights/$originSlug/$destSlug/$cleanDate/",
                bookingReferencePlaceholder = flightRef
            )
        }
    }

    /**
     * Search Accommodation/Hotel offers. Supports live Amadeus if keys provided, falls back to rich simulation.
     */
    suspend fun searchHotels(
        clientId: String,
        clientSecret: String,
        city: String,
        checkInDate: String,
        checkOutDate: String
    ): List<HotelOffer> {
        val useMock = clientId.trim().isEmpty() || clientSecret.trim().isEmpty()

        if (!useMock) {
            try {
                val token = cachedToken ?: fetchAmadeusTokenDirect(clientId, clientSecret)
                if (token != null) {
                    cachedToken = token
                    
                    val retrofit = Retrofit.Builder()
                        .baseUrl(amadeusBaseUrl)
                        .client(okHttpClient)
                        .addConverterFactory(MoshiConverterFactory.create(moshi))
                        .build()

                    val travelApi = retrofit.create(AmadeusTravelApi::class.java)
                    // Amadeus list hotels
                    val iataCity = city.take(3).uppercase()
                    val hotelResponse = travelApi.listHotelsByCity("Bearer $token", iataCity)
                    val list = hotelResponse.data

                    if (list != null && list.isNotEmpty()) {
                        return list.take(6).mapIndexed { idx, amHotel ->
                            val hotelId = amHotel.hotelId ?: "h_00$idx"
                            val name = amHotel.name?.split(" ")?.joinToString(" ") { it.lowercase().capitalize() } ?: "Grand Palace Hotel"
                            val addressStr = amHotel.address?.lines?.joinToString(", ") ?: "$city Center"
                            val ratingValue = amHotel.rating?.toDoubleOrNull() ?: (4.0 + (idx % 10) * 0.1)
                            
                            val rate = 120 + (idx * 35) % 300
                            val total = rate * 3 // Assume 3 nights for total
                            val hotelRef = "AM-HT-${hotelId.takeLast(4).uppercase()}-${Random.nextInt(500, 999)}"

                            HotelOffer(
                                id = hotelId,
                                name = name,
                                address = addressStr,
                                rating = ratingValue,
                                pricePerNight = "$rate",
                                currency = "USD",
                                totalPrice = "$total",
                                checkInDate = checkInDate,
                                checkOutDate = checkOutDate,
                                bookingUrl = "https://www.booking.com/searchresults.html?ss=" + URLEncoder.encode(name, "UTF-8"),
                                bookingReferencePlaceholder = hotelRef
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("TravelBookingService", "Failed live Hotel search, falling back to simulated Hotels", e)
            }
        }

        // --- Custom High Quality Responsive Hotel Mock Simulator ---
        val cleanCity = city.trim().ifEmpty { "Florence" }
        val cleanIn = checkInDate.trim().ifEmpty { "2026-07-18" }
        val cleanOut = checkOutDate.trim().ifEmpty { "2026-07-26" }

        val seed = (cleanCity.length + cleanIn.hashCode() + cleanOut.hashCode()).toLong()
        val random = Random(seed)

        val adjectives = listOf("Grand", "Heritage", "Royal", "Boutique", "Palazzo", "Regency", "Vanderbilt")
        val nouns = listOf("Plaza", "Chateau", "Suites", "Retreat", "Resort", "House", "Sanctuary")

        return List(4) { index ->
            val adj = adjectives[index % adjectives.size]
            val noun = nouns[(index * 2) % nouns.size]
            val name = if (index == 0) "The Westin Excelsior $cleanCity" else "$adj $cleanCity $noun"
            val rating = 4.2 + (random.nextInt(0, 8) * 0.1)

            val address = "Piazza " + listOf("Garibaldi", "Vittorio", "Republica", "Santa Maria")[index % 4] + ", $index, $cleanCity"
            val priceNight = random.nextInt(95, 480)
            
            // Calculate nights
            val totalNights = try {
                val d1 = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(cleanIn)
                val d2 = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(cleanOut)
                val diff = d2.time - d1.time
                val nights = diff / (1000 * 60 * 60 * 24)
                if (nights <= 0) 4 else nights.toInt()
            } catch (e: Exception) {
                5
            }

            val total = priceNight * totalNights
            val hotelRef = "BOOK-${adj.take(2).uppercase()}-${random.nextInt(10000, 99999)}"

            val cityEnc = URLEncoder.encode(name, "UTF-8")

            HotelOffer(
                id = "hotel_sim_$index",
                name = name,
                address = address,
                rating = String.format(Locale.US, "%.1f", rating).toDouble(),
                pricePerNight = "$priceNight",
                currency = "USD",
                totalPrice = "$total",
                checkInDate = cleanIn,
                checkOutDate = cleanOut,
                bookingUrl = "https://www.booking.com/searchresults.html?ss=$cityEnc",
                bookingReferencePlaceholder = hotelRef
            )
        }
    }

    private fun getCarrierName(code: String): String {
        return when (code.uppercase()) {
            "LX" -> "Swiss International Air Lines"
            "LH" -> "Lufthansa"
            "AF" -> "Air France"
            "BA" -> "British Airways"
            "UA" -> "United Airlines"
            "DL" -> "Delta Air Lines"
            "AZ" -> "ITA Airways"
            else -> "SkyWay Airways ($code)"
        }
    }
}
