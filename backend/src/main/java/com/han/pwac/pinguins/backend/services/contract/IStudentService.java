package com.han.pwac.pinguins.backend.services.contract;

import com.han.pwac.pinguins.backend.domain.DTO.StudentDto;
import com.han.pwac.pinguins.backend.domain.DTO.StudentRepositoryDto;

import java.util.Optional;

public interface IStudentService extends IBaseService<StudentRepositoryDto> {
    StudentDto getStudentById(int userId);
}
