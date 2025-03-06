package trik.testsys.webclient.service.entity.user.impl

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import trik.testsys.webclient.entity.impl.Group
import trik.testsys.webclient.entity.impl.Solution
import trik.testsys.webclient.entity.impl.Task
import trik.testsys.webclient.entity.user.impl.Student
import trik.testsys.webclient.repository.user.StudentRepository
import trik.testsys.webclient.service.entity.impl.SolutionVerdictService
import trik.testsys.webclient.service.entity.user.WebUserService
import trik.testsys.webclient.service.token.access.AccessTokenGenerator
import java.util.*

/**
 * @author Roman Shishkin
 * @since 1.0.0
 */
@Service
class StudentService(
    @Qualifier("studentAccessTokenGenerator") private val accessTokenGenerator: AccessTokenGenerator,

    private val solutionVerdictService: SolutionVerdictService
): WebUserService<Student, StudentRepository>() {

    override fun validateName(entity: Student) =
        !entity.name.contains(entity.group.regToken) && super.validateName(entity)

    override fun validateAdditionalInfo(entity: Student) =
        !entity.additionalInfo.contains(entity.group.regToken) && super.validateAdditionalInfo(entity)

    fun generate(count: Long, group: Group): List<Student> {
        val students = mutableListOf<Student>()

        for (i in 1..count) {
            val number = i
            val accessToken = accessTokenGenerator.generate(number.toString() + group.regToken)
            val name = "st-${group.id}-${UUID.randomUUID().toString().substring(4, 18)}-$number"

            val student = Student(name, accessToken)
            student.group = group

            students.add(student)
        }

        repository.saveAll(students)

        return students
    }

    fun export(groups: List<Group>): String {
        val students = groups.asSequence()
            .map { it.students }.flatten()
            .toSet()
            .sortedWith(
                compareBy(
                    { it.group.admin.id },
                    { it.group.id },
                    { it.id }
                )
            )

        val tasks = students.asSequence()
            .map { it.solutions }.flatten()
            .map { it.task }
            .distinct()
            .toSet()
            .sortedBy { it.id }

        val bestScoresByStudents: Map<Student, List<String>> = students.associateWith { student ->
            tasks.map { task ->
                val solution = student.getBestSolutionFor(task) ?: return@map "–"

                val solutionVerdict = solutionVerdictService.findByStudentAndTask(student, task).firstOrNull()
                solutionVerdict?.score?.toString() ?: solution.score.toString()
            }
        }

        val csvHeader = listOf("ID Организатора", "Псевдоним Организатора", "ID Группы", "Псевдоним Группы", "ID Участника", "Псевдоним Участника", *tasks.map { "${it.id}: ${it.name}" }.toTypedArray())
            .joinToString(separator = ";")
            .plus("\n")

        val csvData = students.map { student ->
            listOf(
                student.group.admin.id.toString(),
                student.group.admin.name,
                student.group.id.toString(),
                student.group.name,
                student.id.toString(),
                student.name,
                *bestScoresByStudents[student]!!.toTypedArray()
            )
        }

        val csvDataString = csvData.joinToString(separator = "\n") { it.joinToString(separator = ";") }
        val csv = csvHeader.plus(csvDataString)

        return csv
    }

    companion object {
        fun Student.getBestSolutionFor(task: Task): Solution? {
            return solutions
                .filter { it.task.id == task.id && it.status == Solution.SolutionStatus.PASSED }
                .maxByOrNull { it.score }
        }
    }
}