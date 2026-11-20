package com.example.data.repository

import com.example.data.catalog.HormozganCatalog
import com.example.data.local.AudioSettingsStore
import com.example.data.local.CountyProgressEntity
import com.example.data.local.GameDao
import com.example.data.local.PlayerSaveEntity
import com.example.model.AudioSettings
import com.example.model.CountyProgressState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepository(
    private val gameDao: GameDao,
    private val audioSettingsStore: AudioSettingsStore,
) {
    val audioSettings: Flow<AudioSettings> = audioSettingsStore.audioSettings

    val countyProgressMap: Flow<Map<String, CountyProgressState>> =
        gameDao.observeAllCountyProgress().map { entities ->
            val byId = entities.associateBy { it.countyId }
            HormozganCatalog.allCounties.associate { county ->
                val e = byId[county.id]
                val missions = e?.completedMissionIdsCsv
                    ?.split(",")
                    ?.filter { it.isNotBlank() }
                    ?.toSet()
                    ?: emptySet()
                county.id to CountyProgressState(
                    countyId = county.id,
                    isUnlocked = e?.isUnlocked ?: (county.id == "bashagard"),
                    isCompleted = e?.isCompleted ?: false,
                    completedMissionIds = missions,
                    treasureCollected = e?.treasureCollected ?: false,
                    npcMet = e?.npcMet ?: false,
                )
            }
        }

    val playerSave: Flow<PlayerSaveEntity> =
        gameDao.observePlayerSave().map { save ->
            save ?: PlayerSaveEntity(
                id = 1,
                activeCountyId = "bashagard",
                lastPlayerX = 320f,
                hasSeenIntro = false,
                encyclopediaUnlocked = false,
            )
        }

    suspend fun ensureSeeded() {
        val existing = gameDao.getAllCountyProgressOnce()
        if (existing.isEmpty()) {
            val initial = HormozganCatalog.allCounties.map { county ->
                CountyProgressEntity(
                    countyId = county.id,
                    isUnlocked = county.id == "bashagard",
                    isCompleted = false,
                    completedMissionIdsCsv = "",
                    treasureCollected = false,
                    npcMet = false,
                    lastVisitedAt = 0L,
                )
            }
            gameDao.upsertAllCountyProgress(initial)
        }
        if (gameDao.getPlayerSaveOnce() == null) {
            gameDao.upsertPlayerSave(
                PlayerSaveEntity(
                    id = 1,
                    activeCountyId = "bashagard",
                    lastPlayerX = 320f,
                    hasSeenIntro = false,
                    encyclopediaUnlocked = false,
                ),
            )
        }
    }

    suspend fun markIntroSeen() {
        ensureSeeded()
        val current = gameDao.getPlayerSaveOnce() ?: return
        gameDao.upsertPlayerSave(current.copy(hasSeenIntro = true))
    }

    suspend fun savePlayerLocation(countyId: String, playerX: Float) {
        ensureSeeded()
        val current = gameDao.getPlayerSaveOnce() ?: return
        gameDao.upsertPlayerSave(
            current.copy(
                activeCountyId = countyId,
                lastPlayerX = playerX,
            ),
        )
    }

    suspend fun markNpcMet(countyId: String) {
        ensureSeeded()
        val current = gameDao.getCountyProgressOnce(countyId) ?: return
        if (!current.npcMet) {
            gameDao.upsertCountyProgress(
                current.copy(
                    npcMet = true,
                    lastVisitedAt = System.currentTimeMillis(),
                ),
            )
        }
    }

    suspend fun completeMission(countyId: String, missionId: String): Boolean {
        ensureSeeded()
        val county = HormozganCatalog.getById(countyId)
        val current = gameDao.getCountyProgressOnce(countyId) ?: return false
        val missions = current.completedMissionIdsCsv
            .split(",")
            .filter { it.isNotBlank() }
            .toMutableSet()
        missions.add(missionId)

        val allMissionsDone = county.missions.all { it.id in missions }
        val newlyCompletedCounty = allMissionsDone && !current.treasureCollected

        gameDao.upsertCountyProgress(
            current.copy(
                completedMissionIdsCsv = missions.joinToString(","),
                isCompleted = allMissionsDone,
                treasureCollected = allMissionsDone,
                npcMet = true,
                lastVisitedAt = System.currentTimeMillis(),
            ),
        )

        if (allMissionsDone) {
            for (nextId in county.unlocksNextIds) {
                val nextEntity = gameDao.getCountyProgressOnce(nextId)
                if (nextEntity != null && !nextEntity.isUnlocked) {
                    gameDao.upsertCountyProgress(nextEntity.copy(isUnlocked = true))
                }
            }
            val allAfter = gameDao.getAllCountyProgressOnce()
            val allThirteenDone = allAfter.count { it.treasureCollected } >= HormozganCatalog.allCounties.size
            if (allThirteenDone) {
                val save = gameDao.getPlayerSaveOnce()
                if (save != null) {
                    gameDao.upsertPlayerSave(save.copy(encyclopediaUnlocked = true))
                }
            }
        }
        return newlyCompletedCounty
    }

    suspend fun updateAudioSettings(transform: (AudioSettings) -> AudioSettings) {
        audioSettingsStore.updateSettings(transform)
    }

    suspend fun resetAllProgress() {
        gameDao.clearAllCountyProgress()
        gameDao.clearPlayerSave()
        ensureSeeded()
    }
}
