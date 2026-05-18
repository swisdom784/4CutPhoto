package com.fourcut.photo.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.fourcut.photo.data.local.FourCutDatabase
import com.fourcut.photo.data.local.tag.PersonTagEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PersonTagDaoTest {
    private lateinit var db: FourCutDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, FourCutDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndSearchTagsByName() = runTest {
        db.personTagDao().insert(PersonTagEntity(name = "Hajin", createdAt = 1L, lastUsedAt = 1L))
        db.personTagDao().insert(PersonTagEntity(name = "JungHyun", createdAt = 2L, lastUsedAt = 2L))

        val result = db.personTagDao().searchByName("ha")

        assertEquals(1, result.size)
        assertEquals("Hajin", result.first().name)
    }

    @Test
    fun deleteTagRemovesItFromList() = runTest {
        val id = db.personTagDao().insert(PersonTagEntity(name = "Hajin", createdAt = 1L, lastUsedAt = 1L))

        db.personTagDao().deleteById(id)

        assertTrue(db.personTagDao().getAll().isEmpty())
    }
}
