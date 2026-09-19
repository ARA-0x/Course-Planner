package com.example.data.repository

import com.example.data.importer.ImportItem
import com.example.data.local.CourseDao
import com.example.data.local.CourseDocumentDao
import com.example.data.local.SectionDao
import com.example.data.model.ClassSession
import com.example.data.model.Course
import com.example.data.model.CourseDocument
import com.example.data.model.CourseSection
import com.example.data.model.CourseWithSections
import com.example.data.model.SectionWithDetails
import kotlinx.coroutines.flow.Flow

class CourseRepository(
    private val courseDao: CourseDao,
    private val sectionDao: SectionDao,
    private val documentDao: CourseDocumentDao
) {
    val allCourses: Flow<List<Course>> = courseDao.getAllCourses()
    val coursesWithSections: Flow<List<CourseWithSections>> = courseDao.getCoursesWithSections()
    val selectedForGeneration: Flow<List<CourseWithSections>> = courseDao.getCoursesSelectedForGeneration()
    val enrolledSections: Flow<List<SectionWithDetails>> = sectionDao.getEnrolledSections()
    val allSectionsWithDetails: Flow<List<SectionWithDetails>> = sectionDao.getAllSectionsWithDetails()
    val allDocuments: Flow<List<CourseDocument>> = documentDao.getAllDocuments()

    fun getDocumentsByCourse(courseId: Long): Flow<List<CourseDocument>> {
        return documentDao.getDocumentsByCourse(courseId)
    }

    suspend fun insertDocument(document: CourseDocument): Long {
        return documentDao.insertDocument(document)
    }

    suspend fun updateDocument(document: CourseDocument) {
        documentDao.updateDocument(document)
    }

    suspend fun deleteDocument(id: Long) {
        documentDao.deleteDocumentById(id)
    }

    suspend fun toggleDocumentBookmark(id: Long, isBookmarked: Boolean) {
        documentDao.setBookmarked(id, isBookmarked)
    }

    fun searchCourses(query: String): Flow<List<Course>> {
        return courseDao.searchCourses(query)
    }

    suspend fun getCourseCount(): Int {
        return courseDao.getCourseCount()
    }

    suspend fun importItems(items: List<ImportItem>, clearExisting: Boolean = false) {
        if (clearExisting) {
            clearAllData()
        }

        for (item in items) {
            val courseId = courseDao.insertCourse(item.course)
            for (secItem in item.sections) {
                val secWithCourse = secItem.section.copy(courseId = courseId)
                val sectionId = sectionDao.insertSection(secWithCourse)
                val sessionsWithSec = secItem.sessions.map { it.copy(sectionId = sectionId) }
                sectionDao.insertSessions(sessionsWithSec)
            }
        }
    }

    /**
     * Inserts a manually created course along with its first section and class sessions.
     */
    suspend fun addManualCourse(
        course: Course,
        section: CourseSection,
        sessions: List<ClassSession>
    ): Long {
        val courseId = courseDao.insertCourse(course)
        val sectionId = sectionDao.insertSection(section.copy(courseId = courseId))
        val sessionsWithSec = sessions.map { it.copy(sectionId = sectionId) }
        sectionDao.insertSessions(sessionsWithSec)
        return courseId
    }

    /**
     * Adds an additional section/group to an existing course.
     */
    suspend fun addSectionToCourse(
        courseId: Long,
        section: CourseSection,
        sessions: List<ClassSession>
    ): Long {
        val sectionId = sectionDao.insertSection(section.copy(courseId = courseId))
        val sessionsWithSec = sessions.map { it.copy(sectionId = sectionId) }
        sectionDao.insertSessions(sessionsWithSec)
        return sectionId
    }

    suspend fun deleteCourse(courseId: Long) {
        courseDao.deleteCourseById(courseId)
    }

    suspend fun deleteSection(sectionId: Long) {
        sectionDao.deleteSectionById(sectionId)
    }

    suspend fun toggleCourseSelectedForGeneration(courseId: Long, isSelected: Boolean) {
        courseDao.setCourseSelectedForGeneration(courseId, isSelected)
    }

    suspend fun setSectionEnrolled(section: SectionWithDetails, isEnrolled: Boolean) {
        if (isEnrolled) {
            // Unenroll other sections of the same course so only 1 section is enrolled per course
            sectionDao.unenrollAllForCourse(section.course.id)
            sectionDao.setSectionEnrolled(section.section.id, true)
        } else {
            sectionDao.setSectionEnrolled(section.section.id, false)
        }
    }

    suspend fun applySchedule(sections: List<SectionWithDetails>) {
        sectionDao.clearAllEnrollments()
        for (sec in sections) {
            sectionDao.setSectionEnrolled(sec.section.id, true)
        }
    }

    suspend fun clearEnrollments() {
        sectionDao.clearAllEnrollments()
    }

    suspend fun clearAllData() {
        documentDao.deleteAllDocuments()
        sectionDao.deleteAllSessions()
        sectionDao.deleteAllSections()
        courseDao.deleteAllCourses()
    }
}
