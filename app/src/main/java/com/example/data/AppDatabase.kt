package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    fun getById(id: Int): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Update
    suspend fun update(user: UserEntity)
}

@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE userId = :userId ORDER BY startDate ASC")
    fun getEventsForUser(userId: Int): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id")
    fun getEventById(id: Int): Flow<EventEntity?>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEventByIdOneShot(id: Int): EventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity): Long

    @Update
    suspend fun update(event: EventEntity)

    @Delete
    suspend fun delete(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteById(id: Int)
}

@Dao
interface TravelSegmentDao {
    @Query("SELECT * FROM travel_segments WHERE eventId = :eventId ORDER BY travelDate ASC")
    fun getSegmentsForEvent(eventId: Int): Flow<List<TravelSegmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(segment: TravelSegmentEntity): Long

    @Update
    suspend fun update(segment: TravelSegmentEntity)

    @Delete
    suspend fun delete(segment: TravelSegmentEntity)
}

@Dao
interface AccommodationDao {
    @Query("SELECT * FROM accommodations WHERE eventId = :eventId ORDER BY checkInDate ASC")
    fun getAccommodationsForEvent(eventId: Int): Flow<List<AccommodationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(accommodation: AccommodationEntity): Long

    @Update
    suspend fun update(accommodation: AccommodationEntity)

    @Delete
    suspend fun delete(accommodation: AccommodationEntity)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE eventId = :eventId ORDER BY dueDate ASC")
    fun getTasksForEvent(eventId: Int): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)
}

@Dao
interface VendorDao {
    @Query("SELECT * FROM vendors WHERE eventId = :eventId ORDER BY name ASC")
    fun getVendorsForEvent(eventId: Int): Flow<List<VendorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vendor: VendorEntity): Long

    @Update
    suspend fun update(vendor: VendorEntity)

    @Delete
    suspend fun delete(vendor: VendorEntity)
}

@Dao
interface GuestDao {
    @Query("SELECT * FROM guests WHERE eventId = :eventId ORDER BY name ASC")
    fun getGuestsForEvent(eventId: Int): Flow<List<GuestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(guest: GuestEntity): Long

    @Update
    suspend fun update(guest: GuestEntity)

    @Delete
    suspend fun delete(guest: GuestEntity)
}

@Database(
    entities = [
        UserEntity::class,
        EventEntity::class,
        TravelSegmentEntity::class,
        AccommodationEntity::class,
        TaskEntity::class,
        VendorEntity::class,
        GuestEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun eventDao(): EventDao
    abstract fun travelSegmentDao(): TravelSegmentDao
    abstract fun accommodationDao(): AccommodationDao
    abstract fun taskDao(): TaskDao
    abstract fun vendorDao(): VendorDao
    abstract fun guestDao(): GuestDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "global_moments_planner_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
