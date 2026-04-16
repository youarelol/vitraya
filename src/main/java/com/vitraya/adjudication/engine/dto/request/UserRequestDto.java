package com.vitraya.adjudication.engine.dto.request;

import com.vitraya.adjudication.engine.mysql.entity.Corporate;
import com.vitraya.adjudication.engine.mysql.entity.Users;
import lombok.Data;

import java.util.Map;

@Data
public class UserRequestDto {
        private Users userData;
        private Corporate hospital;
}
