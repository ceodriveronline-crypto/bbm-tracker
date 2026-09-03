package com.example.bbmtracker.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,
    val endTime: Long? = null,
    val totalDistanceMeters: Double = 0.0,
    val totalFuelConsumedLiters: Double = 0.0,
    val totalCostRp: Double = 0.0,
    val status: String = "COMPLETED"
)

data class PeriodSummary(
    val totalDistanceMeters: Double?,
    val totalFuelLiters: Double?,
    val totalCostRp: Double?,
    val totalTrips: Int
)

@Dao
interface ReportDao {
    @Query("""
        SELECT 
            SUM(totalDistanceMeters) AS totalDistanceMeters,
            SUM(totalFuelConsumedLiters) AS totalFuelLiters,
            SUM(totalCostRp) AS totalCostRp,
            COUNT(id) AS totalTrips
        FROM trips 
        WHERE startTime >= :startTimestamp AND startTime <= :endTimestamp
    """)
    fun getSummaryBetween(startTimestamp: Long, endTimestamp: Long): Flow<PeriodSummary>

    @Insert
    suspend fun insertTrip(trip: TripEntity)
}

@Database(entities = [TripEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bbm_tracker_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

object DateUtils {
    fun getStartOfToday(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun getStartOfWeek(): Long {
        return Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun getCurrentTimestamp(): Long = System.currentTimeMillis()
}
