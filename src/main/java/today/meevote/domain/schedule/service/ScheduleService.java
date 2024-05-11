package today.meevote.domain.schedule.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import today.meevote.contextholder.MemberContextHolder;
import today.meevote.domain.schedule.dao.ScheduleDao;
import today.meevote.domain.schedule.dto.request.CreateGroupScheduleDto;
import today.meevote.domain.schedule.dto.request.CreatePersonalScheduleDto;
import today.meevote.domain.schedule.dto.response.GetMyScheduleListDto;
import today.meevote.domain.schedule.dto.response.GetScheduleCategoryDto;
import today.meevote.exception.rest.RestException;
import today.meevote.response.FailureInfo;
import today.meevote.utils.DateUtil;

@Service
@RequiredArgsConstructor
public class ScheduleService {

	private final ScheduleDao scheduleDao;

	@Transactional
	public void createPersonalSchedule(CreatePersonalScheduleDto dto) {
		String email = MemberContextHolder.getEmail();
        if (!scheduleDao.isExistByEmail(email)) {
            throw new RestException(FailureInfo.NOT_EXIST_MEMBER);
        }
        
        if (!scheduleDao.isCategoryExist(dto.getScheduleCategoryId())) {
            throw new RestException(FailureInfo.NOT_EXIST_CATEGORY);
        }

        if (!DateUtil.validateDateOrder(dto.getStartDate(), dto.getEndDate())) {
            throw new RestException(FailureInfo.INVALID_DATE_FORMAT);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("dto", dto);

        scheduleDao.createPersonalSchedule(params);
        Long scheduleId = (Long) params.get("id");
        scheduleDao.createMemberSchedule(email, scheduleId);

        if (StringUtils.hasText(dto.getPlaceName())
                && StringUtils.hasText(dto.getPlaceLatitude())
                && StringUtils.hasText(dto.getPlaceLongitude())) {
            scheduleDao.createSchedulePlace(params);
        }
	}

	@Transactional
	public void deletePersonalSchedule(Long scheduleId) {
		String email = MemberContextHolder.getEmail();
        if (!scheduleDao.isExistScheduleByInfo(email, scheduleId)) {
            throw new RestException(FailureInfo.NOT_EXIST_SCHEDULE);
        }
        scheduleDao.deletePersonalSchedule(scheduleId);
	}

    public List<GetMyScheduleListDto> getMyScheduleList(Boolean isGroup, String year, String month) {
        String email = MemberContextHolder.getEmail();
        return scheduleDao.getMyScheduleList(email, isGroup, year, month);
    }

    public List<GetScheduleCategoryDto> getCategory() {
        return scheduleDao.getCategory();
    }

    @Transactional
    public void createGroupSchedule(CreateGroupScheduleDto createGroupScheduleDto) {
        Map<String, Object> params = new HashMap<>();
        params.put("dto", createGroupScheduleDto);
        scheduleDao.createGroupSchedule(params);
        scheduleDao.createGroupMemberSchedule(params);
    }
}
