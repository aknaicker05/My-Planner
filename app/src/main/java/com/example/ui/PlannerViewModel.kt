package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PlannerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PlannerRepository
    
    // --- Travel Booking API Search State & Integration ---
    private val travelBookingService = com.example.network.TravelBookingService()
    
    val amadeusClientId = MutableStateFlow("")
    val amadeusClientSecret = MutableStateFlow("")
    
    private val _flightOffers = MutableStateFlow<List<com.example.network.FlightOffer>>(emptyList())
    val flightOffers: StateFlow<List<com.example.network.FlightOffer>> = _flightOffers.asStateFlow()
    
    private val _hotelOffers = MutableStateFlow<List<com.example.network.HotelOffer>>(emptyList())
    val hotelOffers: StateFlow<List<com.example.network.HotelOffer>> = _hotelOffers.asStateFlow()
    
    private val _isSearchingFlights = MutableStateFlow(false)
    val isSearchingFlights: StateFlow<Boolean> = _isSearchingFlights.asStateFlow()
    
    private val _isSearchingHotels = MutableStateFlow(false)
    val isSearchingHotels: StateFlow<Boolean> = _isSearchingHotels.asStateFlow()
    
    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()
    
    // --- Session State ---
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    // --- Active Event List State ---
    private val _events = MutableStateFlow<List<EventEntity>>(emptyList())
    val events: StateFlow<List<EventEntity>> = _events.asStateFlow()

    // --- Calendar Filters ---
    private val _showEventsInCalendar = MutableStateFlow(true)
    val showEventsInCalendar: StateFlow<Boolean> = _showEventsInCalendar.asStateFlow()

    // --- Selected Event Details Flows ---
    private val _selectedEventId = MutableStateFlow<Int?>(null)
    val selectedEventId: StateFlow<Int?> = _selectedEventId.asStateFlow()

    val selectedEvent: StateFlow<EventEntity?> = _selectedEventId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else repository.getEventById(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val travelSegments: StateFlow<List<TravelSegmentEntity>> = _selectedEventId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getSegmentsForEvent(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accommodations: StateFlow<List<AccommodationEntity>> = _selectedEventId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getAccommodationsForEvent(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskEntity>> = _selectedEventId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getTasksForEvent(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vendors: StateFlow<List<VendorEntity>> = _selectedEventId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getVendorsForEvent(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val guests: StateFlow<List<GuestEntity>> = _selectedEventId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getGuestsForEvent(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        val database = AppDatabase.getDatabase(application)
        repository = PlannerRepository(database)

        // Generate a standard test account on initial spin-up to guarantee a beautiful cold-start experience
        viewModelScope.launch {
            try {
                val demoEmail = "aknaicker05@gmail.com"
                var demoUser = repository.loginUser(demoEmail, "password")
                if (demoUser == null) {
                    val newDemoId = repository.registerUser(
                        UserEntity(
                            email = demoEmail,
                            passwordHash = "password",
                            name = "Alex Naicker",
                            countryCity = "San Francisco, USA",
                            preferredLanguage = "English (US)",
                            timeZone = "GMT-7",
                            plannerType = "Individual"
                        )
                    )
                    demoUser = repository.loginUser(demoEmail, "password")
                }
                _currentUser.value = demoUser
                observeUserEvents()

                // Seed some gorgeous initial events if the user currently has none
                launch {
                    val currentEvents = repository.getEventsForUser(demoUser!!.id).first()
                    if (currentEvents.isEmpty()) {
                        seedSampleEvents(demoUser.id)
                    }
                }
            } catch (e: Exception) {
                // User already exists or other error, fallback safely
            }
        }
    }

    private fun observeUserEvents() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.getEventsForUser(user.id).collect {
                _events.value = it
            }
        }
    }

    // --- Authentication Actions ---
    fun login(email: String, passwordHash: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val user = repository.loginUser(email, passwordHash)
            if (user != null) {
                _currentUser.value = user
                _loginError.value = null
                observeUserEvents()
                onSuccess()
            } else {
                _loginError.value = "Invalid email or password"
            }
        }
    }

    fun register(
        email: String,
        passwordHash: String,
        name: String,
        countryCity: String,
        language: String,
        timezone: String,
        plannerType: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val newId = repository.registerUser(
                    UserEntity(
                        email = email,
                        passwordHash = passwordHash,
                        name = name,
                        countryCity = countryCity,
                        preferredLanguage = language,
                        timeZone = timezone,
                        plannerType = plannerType
                    )
                )
                // Automatically log inside upon successful registration
                val loggedUser = repository.loginUser(email, passwordHash)
                _currentUser.value = loggedUser
                _loginError.value = null
                observeUserEvents()
                onSuccess()
            } catch (e: Exception) {
                _loginError.value = e.message ?: "Registration failed"
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        _currentUser.value = null
        _events.value = emptyList()
        _selectedEventId.value = null
        onSuccess()
    }

    fun updateProfile(
        name: String,
        countryCity: String,
        language: String,
        timezone: String,
        plannerType: String
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updated = user.copy(
                name = name,
                countryCity = countryCity,
                preferredLanguage = language,
                timeZone = timezone,
                plannerType = plannerType
            )
            repository.updateUser(updated)
            _currentUser.value = updated
        }
    }

    // --- Event Custom Controls ---
    fun selectEvent(eventId: Int?) {
        _selectedEventId.value = eventId
    }

    fun createEvent(
        name: String,
        type: String,
        startDate: String,
        endDate: String,
        city: String,
        country: String,
        venue: String,
        address: String,
        guests: Int,
        budgetMin: Double,
        budgetMax: Double,
        currency: String,
        notes: String,
        isDraft: Boolean = false,
        onSuccess: (Int) -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val newEvent = EventEntity(
                userId = user.id,
                name = name,
                type = type,
                startDate = startDate,
                endDate = endDate,
                locationCity = city,
                locationCountry = country,
                venueName = venue,
                address = address,
                guestsCount = guests,
                budgetMin = budgetMin,
                budgetMax = budgetMax,
                budgetCurrency = currency,
                notes = notes,
                isDraft = isDraft
            )
            val id = repository.insertEvent(newEvent)
            onSuccess(id.toInt())
        }
    }

    fun updateEvent(
        id: Int,
        name: String,
        type: String,
        startDate: String,
        endDate: String,
        city: String,
        country: String,
        venue: String,
        address: String,
        guests: Int,
        budgetMin: Double,
        budgetMax: Double,
        currency: String,
        notes: String,
        isDraft: Boolean
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updated = EventEntity(
                id = id,
                userId = user.id,
                name = name,
                type = type,
                startDate = startDate,
                endDate = endDate,
                locationCity = city,
                locationCountry = country,
                venueName = venue,
                address = address,
                guestsCount = guests,
                budgetMin = budgetMin,
                budgetMax = budgetMax,
                budgetCurrency = currency,
                notes = notes,
                isDraft = isDraft
            )
            repository.updateEvent(updated)
        }
    }

    fun deleteEvent(event: EventEntity, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteEvent(event)
            if (_selectedEventId.value == event.id) {
                _selectedEventId.value = null
            }
            onDeleted()
        }
    }

    fun duplicateEvent(eventId: Int, onDuplicated: (Int) -> Unit) {
        viewModelScope.launch {
            val progressId = repository.duplicateEvent(eventId)
            if (progressId > 0) {
                onDuplicated(progressId.toInt())
            }
        }
    }

    fun toggleCalendarEvents() {
        _showEventsInCalendar.value = !_showEventsInCalendar.value
    }

    // --- Travel Segment Operations ---
    fun addTravelSegment(from: String, to: String, date: String, mode: String, notes: String, bookingReference: String = "") {
        val eventId = _selectedEventId.value ?: return
        viewModelScope.launch {
            repository.insertTravelSegment(
                TravelSegmentEntity(
                    eventId = eventId,
                    fromLocation = from,
                    toLocation = to,
                    travelDate = date,
                    mode = mode,
                    notes = notes,
                    bookingReference = bookingReference
                )
            )
        }
    }

    fun updateTravelSegment(segment: TravelSegmentEntity) {
        viewModelScope.launch {
            repository.updateTravelSegment(segment)
        }
    }

    fun deleteTravelSegment(segment: TravelSegmentEntity) {
        viewModelScope.launch {
            repository.deleteTravelSegment(segment)
        }
    }

    // --- Accommodation Operations ---
    fun addAccommodation(name: String, checkIn: String, checkOut: String, address: String, ref: String) {
        val eventId = _selectedEventId.value ?: return
        viewModelScope.launch {
            repository.insertAccommodation(
                AccommodationEntity(
                    eventId = eventId,
                    name = name,
                    checkInDate = checkIn,
                    checkOutDate = checkOut,
                    address = address,
                    bookingReference = ref
                )
            )
        }
    }

    fun updateAccommodation(accommodation: AccommodationEntity) {
        viewModelScope.launch {
            repository.updateAccommodation(accommodation)
        }
    }

    fun deleteAccommodation(accommodation: AccommodationEntity) {
        viewModelScope.launch {
            repository.deleteAccommodation(accommodation)
        }
    }

    // --- Task Operations ---
    fun getTasksForEvent(eventId: Int): Flow<List<TaskEntity>> {
        return repository.getTasksForEvent(eventId)
    }

    fun addTask(title: String, category: String, dueDate: String, assignedTo: String) {
        val eventId = _selectedEventId.value ?: return
        viewModelScope.launch {
            repository.insertTask(
                TaskEntity(
                    eventId = eventId,
                    title = title,
                    category = category,
                    dueDate = dueDate,
                    status = "Not started",
                    assignedTo = assignedTo
                )
            )
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun toggleTaskDone(task: TaskEntity) {
        val nextStatus = if (task.status == "Done") "Not started" else "Done"
        viewModelScope.launch {
            repository.updateTask(task.copy(status = nextStatus))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // --- Vendor Operations ---
    fun addVendor(
        name: String,
        type: String,
        country: String,
        city: String,
        phone: String,
        email: String,
        website: String,
        notes: String,
        linkedTaskId: Int? = null
    ) {
        val eventId = _selectedEventId.value ?: return
        viewModelScope.launch {
            repository.insertVendor(
                VendorEntity(
                    eventId = eventId,
                    name = name,
                    type = type,
                    country = country,
                    city = city,
                    phone = phone,
                    email = email,
                    website = website,
                    notes = notes,
                    linkedTaskId = linkedTaskId
                )
            )
        }
    }

    fun updateVendor(vendor: VendorEntity) {
        viewModelScope.launch {
            repository.updateVendor(vendor)
        }
    }

    fun deleteVendor(vendor: VendorEntity) {
        viewModelScope.launch {
            repository.deleteVendor(vendor)
        }
    }

    // --- Travel Booking API Searches ---
    fun searchFlights(fromCity: String, toCity: String, date: String) {
        viewModelScope.launch {
            _isSearchingFlights.value = true
            _searchError.value = null
            try {
                val results = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    travelBookingService.searchFlights(
                        clientId = amadeusClientId.value,
                        clientSecret = amadeusClientSecret.value,
                        fromCity = fromCity,
                        toCity = toCity,
                        departureDate = date
                    )
                }
                _flightOffers.value = results
            } catch (e: Exception) {
                _searchError.value = "Flight search failed: ${e.localizedMessage}"
            } finally {
                _isSearchingFlights.value = false
            }
        }
    }

    fun searchHotels(city: String, checkIn: String, checkOut: String) {
        viewModelScope.launch {
            _isSearchingHotels.value = true
            _searchError.value = null
            try {
                val results = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    travelBookingService.searchHotels(
                        clientId = amadeusClientId.value,
                        clientSecret = amadeusClientSecret.value,
                        city = city,
                        checkInDate = checkIn,
                        checkOutDate = checkOut
                    )
                }
                _hotelOffers.value = results
            } catch (e: Exception) {
                _searchError.value = "Hotel search failed: ${e.localizedMessage}"
            } finally {
                _isSearchingHotels.value = false
            }
        }
    }

    fun clearSearchResults() {
        _flightOffers.value = emptyList()
        _hotelOffers.value = emptyList()
        _searchError.value = null
    }

    // --- Guest Operations ---
    fun addGuest(name: String, email: String, phone: String, country: String, rsvp: String, notes: String) {
        val eventId = _selectedEventId.value ?: return
        viewModelScope.launch {
            repository.insertGuest(
                GuestEntity(
                    eventId = eventId,
                    name = name,
                    email = email,
                    phone = phone,
                    country = country,
                    rsvpStatus = rsvp,
                    notes = notes
                )
            )
        }
    }

    fun updateGuest(guest: GuestEntity) {
        viewModelScope.launch {
            repository.updateGuest(guest)
        }
    }

    fun deleteGuest(guest: GuestEntity) {
        viewModelScope.launch {
            repository.deleteGuest(guest)
        }
    }

    // --- Future-Ready AI Suggestion Simulator Engine ---
    fun generateAISuggestedChecklist(eventType: String, country: String) {
        val eventId = _selectedEventId.value ?: return
        viewModelScope.launch {
            val suggestions = getDemoAIChecklist(eventType, country)
            suggestions.forEach { (title, cat) ->
                repository.insertTask(
                    TaskEntity(
                        eventId = eventId,
                        title = title,
                        category = cat,
                        dueDate = "2026-06-15", // dynamic placeholder relative to event dates
                        status = "Not started",
                        assignedTo = "AI Suggestion"
                    )
                )
            }
        }
    }

    fun generateAITravelTips(city: String, country: String): List<String> {
        return getDemoAILogisticsTips(city, country)
    }

    // Seeding sample datasets for Alex's rich review
    private suspend fun seedSampleEvents(userId: Int) {
        val weddingId = repository.insertEvent(
            EventEntity(
                userId = userId,
                name = "Sofia & Matteo's Destination Wedding",
                type = "Wedding",
                startDate = "2026-07-20",
                endDate = "2026-07-25",
                locationCity = "Florence",
                locationCountry = "Italy",
                venueName = "Villa Cora",
                address = "Viale Machiavelli, 18, 50125 Firenze FI",
                guestsCount = 75,
                budgetMin = 30000.0,
                budgetMax = 45000.0,
                budgetCurrency = "EUR",
                notes = "Requires special Italian legal procedures for foreign couples. Highlight traditional tuscan catering and cultural music constraints.",
                isDraft = false
            )
        ).toInt()

        if (weddingId > 0) {
            // Seed segments
            repository.insertTravelSegment(
                TravelSegmentEntity(
                    eventId = weddingId,
                    fromLocation = "San Francisco, USA",
                    toLocation = "Florence, Italy",
                    travelDate = "2026-07-18",
                    mode = "Flight",
                    notes = "Swiss Air via Zurich. Visa: ETIAS required. Luggage limit: 23kg."
                )
            )
            repository.insertAccommodation(
                AccommodationEntity(
                    eventId = weddingId,
                    name = "The Westin Excelsior Florence",
                    checkInDate = "2026-07-18",
                    checkOutDate = "2026-07-26",
                    address = "Piazza Ognissanti, 3, Florence",
                    bookingReference = "WEX-90281"
                )
            )
            repository.insertTask(
                TaskEntity(
                    eventId = weddingId,
                    title = "Submit declaration of intent to Embassy",
                    category = "Documents",
                    dueDate = "2026-06-01",
                    status = "Done",
                    assignedTo = "Sofia"
                )
            )
            repository.insertTask(
                TaskEntity(
                    eventId = weddingId,
                    title = "Finalize Tuscan Olivewood Menu selection",
                    category = "Catering",
                    dueDate = "2026-06-25",
                    status = "In progress",
                    assignedTo = "Matteo"
                )
            )
            repository.insertTask(
                TaskEntity(
                    eventId = weddingId,
                    title = "Confirm floral layout with florist team",
                    category = "Venue",
                    dueDate = "2026-07-05",
                    status = "Not started",
                    assignedTo = "Alex Naicker"
                )
            )
            repository.insertVendor(
                VendorEntity(
                    eventId = weddingId,
                    name = "Tasting Tuscany Catering Co.",
                    type = "Catering",
                    country = "Italy",
                    city = "Florence",
                    phone = "+39 055 123 456",
                    email = "ciao@tastingtuscany.it",
                    website = "www.tastingtuscany.it",
                    notes = "Recommended by Villa Cora events manager. Custom tasting scheduled."
                )
            )
            repository.insertGuest(
                GuestEntity(
                    eventId = weddingId,
                    name = "Malia Thompson",
                    email = "malia@example.com",
                    phone = "555-0101",
                    country = "USA",
                    rsvpStatus = "Confirmed",
                    notes = "Dietary: Gluten-free. Needs airport shuttle support."
                )
            )
            repository.insertGuest(
                GuestEntity(
                    eventId = weddingId,
                    name = "David Rossi",
                    email = "d.rossi@example.it",
                    phone = "555-0102",
                    country = "Italy",
                    rsvpStatus = "Maybe",
                    notes = "Inquiring about children policies."
                )
            )
        }

        // Add an event draft
        repository.insertEvent(
            EventEntity(
                userId = userId,
                name = "Euro-Tech Summit 2026 (Draft)",
                type = "Corporate event",
                startDate = "2026-10-12",
                endDate = "2026-10-14",
                locationCity = "Munich",
                locationCountry = "Germany",
                venueName = "Messe München",
                address = "Am Messesee 2, 81829 München",
                guestsCount = 500,
                budgetMin = 120000.0,
                budgetMax = 150000.0,
                budgetCurrency = "EUR",
                notes = "Requires massive custom AV rigging, multi-lingual interpreters, and sustainable zero-waste certifications.",
                isDraft = true
            )
        )
    }

    private fun getDemoAIChecklist(type: String, country: String): List<Pair<String, String>> {
        return when (type) {
            "Wedding" -> listOf(
                "Arrange native ceremony legal filings in $country" to "Documents",
                "Translate birth certificates and notarize" to "Documents",
                "Book local high-end hair & makeup specialist" to "Services",
                "Source local floral assets fitting $country climate" to "Venue",
                "Schedule sound rehearsal with musical performers" to "Entertainment"
            )
            "Funeral / Memorial" -> listOf(
                "Verify repatriation or state burial permits in $country" to "Documents",
                "Liaise with local memorial home director" to "Services",
                "Coordinate cultural religious service constraints" to "Documents",
                "Establish guest hospitality or lodging blocks" to "Travel"
            )
            "Birthday party" -> listOf(
                "Arrange custom catering and pastry artist in $country" to "Catering",
                "Rent tables, party supplies, and ambient staging" to "Venue",
                "Design cultural theme decor and visual styling" to "Venue"
            )
            "Corporate event" -> listOf(
                "Verify local business event permits in $country" to "Documents",
                "Confirm professional high-speed AV & projector rigging" to "Venue",
                "Book simultaneous interpretation/translation team" to "Services",
                "Arrange VIP airport shuttles and shuttle coordination" to "Travel"
            )
            else -> listOf(
                "Secure venue reservations in $country" to "Venue",
                "Design event invitations and coordinate digital RSVPs" to "Guests",
                "Draft initial catering or culinary vendor schedule" to "Catering"
            )
        }
    }

    private fun getDemoAILogisticsTips(city: String, country: String): List<String> {
        return listOf(
            "✈️ Travel Protocol: Check if your guests need an active visa or pre-travel authorization (like ETIAS for $country)." ,
            "💶 Local Currency: Ensure your budget accounts for local currency fluctuations ($country relies on its national/regional standards)." ,
            "🔌 Power Standard: Keep in mind the local power sockets and voltage configurations in $city for AV planning." ,
            "🗣️ Language: Secure pre-translated event programs and venue signs to coordinate seamlessly with local services." ,
            "📅 Culture & Timing: Be mindful of host-country public holidays or religious high-seasons that might affect vendor rates."
        )
    }
}
