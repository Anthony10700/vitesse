package com.vitesse.hr.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class CandidateDaoTest {

    private lateinit var db: VitesseDatabase
    private lateinit var dao: CandidateDao

    @Before
    fun setup() {
        // base de données en mémoire : disparaît après chaque test
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, VitesseDatabase::class.java).build()
        dao = db.candidateDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    // candidat de référence utilisé dans plusieurs tests
    private fun makeCandidate(firstName: String = "John") = Candidate(
        firstName = firstName,
        lastName = "Doe",
        photoUri = null,
        phoneNumber = "0612345678",
        emailAddress = "john.doe@example.com",
        dateOfBirth = LocalDate.of(1990, 1, 1),
        expectedSalary = 50000.0,
        notes = "Test"
    )

    @Test
    fun insert_then_getById_returns_candidate() = runBlocking {
        val id = dao.insert(makeCandidate())
        val result = dao.getById(id).first()
        assertNotNull(result)
        assertEquals("John", result?.firstName)
    }

    @Test
    fun update_changes_isFavorite() = runBlocking {
        val id = dao.insert(makeCandidate())
        val candidate = dao.getById(id).first()!!
        dao.update(candidate.copy(isFavorite = true))
        val updated = dao.getById(id).first()
        assertTrue(updated!!.isFavorite)
    }

    @Test
    fun delete_removes_candidate() = runBlocking {
        val id = dao.insert(makeCandidate())
        val candidate = dao.getById(id).first()!!
        dao.delete(candidate)
        val result = dao.getById(id).first()
        assertNull(result)
    }

    @Test
    fun search_filters_by_firstName() = runBlocking {
        dao.insert(makeCandidate(firstName = "Alice"))
        dao.insert(makeCandidate(firstName = "Bob"))
        val results = dao.search("Ali").first()
        assertEquals(1, results.size)
        assertEquals("Alice", results.first().firstName)
    }
}
