package trik.testsys.webclient.entity.impl

import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.core.utils.enums.Enum
import trik.testsys.core.utils.enums.converter.AbstractEnumConverter
import trik.testsys.webclient.entity.AbstractNotedEntity
import trik.testsys.webclient.entity.user.impl.Developer
import javax.persistence.*

@Entity
@Table(name = "${TABLE_PREFIX}_TASK_FILE")
class TaskFile(
    name: String,

    @Column(nullable = false, unique = false, updatable = false)
    val type: TaskFileType
) : AbstractNotedEntity(name) {

    @ManyToOne
    @JoinColumn(
        nullable = false, unique = false, updatable = false,
        name = "developer_id", referencedColumnName = "id"
    )
    lateinit var developer: Developer

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "TASK_FILES_BY_TASKS",
        joinColumns = [JoinColumn(name = "task_file_id")],
        inverseJoinColumns = [JoinColumn(name = "task_id")]
    )
    val tasks: MutableSet<Task> = mutableSetOf()

    @OneToMany(mappedBy = "taskFile", fetch = FetchType.EAGER)
    val taskFileAudits: MutableSet<TaskFileAudit> = mutableSetOf()

    @get:Transient
    val latestAudit: TaskFileAudit
        get() = taskFileAudits.maxBy { it.creationDate!! }

    @get:Transient
    val latestFileName: String
        get() = latestAudit.fileName

    enum class TaskFileType(override val dbkey: String) : Enum {

        POLYGON("PLG"),
        EXERCISE("EXR"),
        SOLUTION("SLN"),
        CONDITION("CND");

        fun canBeRemovedOnTaskTesting() = this == CONDITION || this == EXERCISE

        fun cannotBeRemovedOnTaskTesting() = !canBeRemovedOnTaskTesting()

        companion object {

            @Converter(autoApply = true)
            class TaskFileTypeConverter : AbstractEnumConverter<TaskFileType>()

            fun TaskFileType.localized() = when(this) {
                POLYGON -> "Полигон"
                EXERCISE -> "Упражнение"
                SOLUTION -> "Решение"
                CONDITION -> "Условие"
            }
        }
    }

    companion object {

        fun polygon(name: String) = TaskFile(name, TaskFileType.POLYGON)

        fun exercise(name: String) = TaskFile(name, TaskFileType.EXERCISE)

        fun solution(name: String) = TaskFile(name, TaskFileType.SOLUTION)

        fun condition(name: String) = TaskFile(name, TaskFileType.CONDITION)
    }
}