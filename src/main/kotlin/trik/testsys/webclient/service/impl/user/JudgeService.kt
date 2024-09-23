package trik.testsys.webclient.service.impl.user

import org.springframework.stereotype.Service
import trik.testsys.core.service.user.AbstractUserService
import trik.testsys.core.utils.marker.TrikService
import trik.testsys.webclient.entity.impl.user.Judge
import trik.testsys.webclient.repository.user.JudgeRepository

@Service
class JudgeService : AbstractUserService<Judge, JudgeRepository>(), TrikService